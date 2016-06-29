/*
 * Copyright (C) 2016 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ART. If not, see <http://www.gnu.org/licenses/>.
 */
package art.jobrunners;

import art.connectionpool.DbConnections;
import art.dbutils.DatabaseUtils;
import art.dbutils.DbService;
import art.enums.JobType;
import art.enums.ReportFormat;
import art.enums.ReportType;
import art.job.JobService;
import art.jobparameter.JobParameter;
import art.jobparameter.JobParameterService;
import art.mail.Mailer;
import art.report.Report;
import art.report.ReportService;
import art.reportparameter.ReportParameter;
import art.runreport.ParameterProcessor;
import art.runreport.ParameterProcessorResult;
import art.runreport.ReportOutputGenerator;
import art.runreport.ReportRunner;
import art.servlets.Config;
import art.user.User;
import art.utils.ArtUtils;
import art.utils.CachedResult;
import art.utils.FilenameHelper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.MessagingException;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

/**
 * Runs report jobs
 *
 * @author Timothy Anyona
 */
public class ReportJob implements org.quartz.Job {

	private static final Logger logger = LoggerFactory.getLogger(ReportJob.class);
	private DbService dbService;
	private String fileName;
	private String jobAuditKey;
	private art.job.Job job;
	private Timestamp jobStartDate;
	private JobType jobType;
	private int jobId;
	private String runDetails;
	private String runMessage;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		//clearing the jobs cache from this method either using a CacheEvict annotation
		//or calling CacheHelper.clearJobs() doesn't seem to clear the cache.
		//so don't use a cache for jobs at all? 
		//or only use jobs cache for job entity details but not when need run details
		if (!Config.getSettings().isSchedulingEnabled()) {
			return;
		}

		JobDataMap dataMap = context.getMergedJobDataMap();
		int tempJobId = dataMap.getInt("jobId");

		JobService jobService = new JobService();

		try {
			job = jobService.getJob(tempJobId);
		} catch (SQLException ex) {
			logger.error("Error", ex);
		}

		if (job == null) {
			logger.info("Job not found: {}", tempJobId);
			return;
		}

		jobType = job.getJobType();
		jobId = job.getJobId();

		fileName = "";
		runDetails = "";
		runMessage = "";

		dbService = new DbService();

		//get next run date	for the job for updating the jobs table. only update if it's a scheduled run and not an interactive, temporary job
		boolean tempJob = dataMap.getBooleanValue("tempJob");
		Date nextRunDate;
		if (tempJob) {
			//temp job. use existing next run date
			nextRunDate = job.getNextRunDate();
		} else {
			//not a temp job. set new next run date
			nextRunDate = context.getTrigger().getFireTimeAfter(new Date());
		}

		try {
			//set overall job start time in the jobs table
			beforeExecution(nextRunDate);

			if (!job.isActive()) {
				runMessage = "jobs.message.jobDisabled";
			} else if (!job.getReport().isActive()) {
				runMessage = "jobs.message.reportDisabled";
			} else if (!job.getUser().isActive()) {
				runMessage = "jobs.message.ownerDisabled";
			} else {
				if (job.getRecipientsReportId() > 0) {
					//job has dynamic recipients
					runDynamicRecipientsJob();
				} else {
					//job doesn't have dynamic recipients
					runNormalJob();
				}
			}

			afterCompletion();
		} catch (SQLException ex) {
			logger.error("Error", ex);
		}
	}

	/**
	 * Sends an email
	 *
	 * @param mailer the mailer to use
	 * @throws MessagingException
	 * @throws IOException
	 */
	private void sendEmail(Mailer mailer) throws MessagingException, IOException {
		logger.debug("Entering sendEmail");

		if (Config.isEmailServerConfigured()) {
			mailer.send();
		} else {
			logger.info("Email server not configured. Job Id: {}", jobId);
		}
	}

	/**
	 * Prepares a mailer object for sending an alert job
	 *
	 * @param mailer the mailer to use
	 * @param msg the message of the email
	 * @param value the alert value
	 */
	private void prepareAlertMailer(Mailer mailer, String msg, int value) {
		logger.debug("Entering prepareAlertMailer");

		String from = job.getMailFrom();

		String subject = job.getMailSubject();
		// compatibility with Art pre 1.8 where subject was not editable
		if (subject == null) {
			subject = "ART Alert: (Job " + jobId + ")";
		}

		mailer.setSubject(subject);
		mailer.setFrom(from);

		String mainMessage;
		if (StringUtils.isBlank(msg)) {
			mainMessage = "&nbsp;"; //if message is blank, ensure there's a space before the hr
		} else {
			mainMessage = msg;

			//replace value placeholder in the message if it exists
			String searchString = Pattern.quote("#value#"); //quote in case it contains special regex characters
			String replaceString = Matcher.quoteReplacement(String.valueOf(value)); //quote in case it contains special regex characters
			mainMessage = mainMessage.replaceAll("(?iu)" + searchString, replaceString); //(?iu) makes replace case insensitive across unicode characters
		}

		Context ctx = new Context(Locale.getDefault());
		ctx.setVariable("mainMessage", mainMessage);
		ctx.setVariable("job", job);

		SpringTemplateEngine templateEngine = getTemplateEngine();
		String finalMessage = templateEngine.process("emailTemplate.html", ctx);
		mailer.setMessage(finalMessage);
	}

	/**
	 * Prepares a mailer object for sending an alert job based on a freemarker
	 * report
	 *
	 * @param mailer the mailer to use
	 * @param value the alert value
	 */
	private void prepareFreeMarkerAlertMailer(Mailer mailer, int value)
			throws TemplateException, IOException {

		prepareFreeMarkerAlertMailer(mailer, value, null);
	}

	/**
	 * Prepares a mailer object for sending an alert job based on a freemarker
	 * report
	 *
	 * @param mailer the mailer to use
	 * @param value the alert value
	 * @param recipientColumns the recipient column details
	 */
	private void prepareFreeMarkerAlertMailer(Mailer mailer, int value,
			Map<String, String> recipientColumns) throws TemplateException, IOException {

		logger.debug("Entering prepareFreeMarkerAlertMailer");

		String from = job.getMailFrom();

		String subject = job.getMailSubject();
		// compatibility with Art pre 1.8 where subject was not editable
		if (subject == null) {
			subject = "ART Alert: (Job " + jobId + ")";
		}

		mailer.setSubject(subject);
		mailer.setFrom(from);

		Report report = job.getReport();
		String templateFileName = report.getTemplate();
		String templatesPath = Config.getTemplatesPath();
		String fullTemplateFileName = templatesPath + templateFileName;

		//check if template file exists
		File templateFile = new File(fullTemplateFileName);
		if (!templateFile.exists()) {
			throw new IllegalStateException("Template file not found: " + templateFileName);
		}

		Configuration cfg = Config.getFreemarkerConfig();
		Template template = cfg.getTemplate(templateFileName);

		//set objects to be passed to freemarker
		Map<String, Object> data = new HashMap<>();

		if (recipientColumns != null) {
			for (Map.Entry<String, String> entry : recipientColumns.entrySet()) {
				String columnName = entry.getKey();
				String columnValue = entry.getValue();
				data.put(columnName, columnValue);
			}
		}

		data.put("value", value);

		//create output
		Writer writer = new StringWriter();
		template.process(data, writer);

		String finalMessage = writer.toString();
		mailer.setMessage(finalMessage);
	}

	/**
	 * Prepares a mailer object for sending an email job
	 *
	 * @param mailer the mailer to use
	 * @param msg the message of the email
	 * @param outputFileName the full path of a file to include with the email
	 */
	private void prepareMailer(Mailer mailer, String msg, String outputFileName)
			throws FileNotFoundException, IOException {

		logger.debug("Entering prepareEmailMailer: outputFileName='{}'", outputFileName);

		String from = job.getMailFrom();

		String subject = job.getMailSubject();
		// compatibility with Art pre 1.8 where subject was not editable
		if (subject == null) {
			subject = "ART: (Job " + jobId + ")";
		}

		mailer.setSubject(subject);
		mailer.setFrom(from);

		Report report = job.getReport();
		ReportType reportType = report.getReportType();

		String messageData = "";

		if (jobType.isEmailAttachment()) {
			// e-mail output as attachment
			List<File> attachments = new ArrayList<>();
			attachments.add(new File(outputFileName));
			mailer.setAttachments(attachments);
		} else if (jobType.isEmailInline()) {
			// inline html within email
			// read the file and include it in the HTML message
			try (FileInputStream fis = new FileInputStream(outputFileName)) {
				byte fileBytes[] = new byte[fis.available()];

				int result = fis.read(fileBytes);
				if (result == -1) {
					logger.warn("EOF reached for inline email file: '{}'", outputFileName);
				}

				// convert the file to a string and get only the html table
				messageData = new String(fileBytes, "UTF-8");

				if (reportType != ReportType.FreeMarker) {
					messageData = messageData.substring(messageData.indexOf("<body>") + 6, messageData.indexOf("</body>")); //html plain output now has head and body sections
				}

			}
		}

		if (reportType == ReportType.FreeMarker) {
			mailer.setMessage(messageData);
		} else {
			String mainMessage;
			if (StringUtils.isBlank(msg)) {
				mainMessage = "&nbsp;"; //if message is blank, ensure there's a space before the hr
			} else {
				mainMessage = msg;
			}

			Context ctx = new Context(Locale.getDefault());
			ctx.setVariable("mainMessage", mainMessage);
			ctx.setVariable("job", job);
			ctx.setVariable("data", messageData);

			SpringTemplateEngine templateEngine = getTemplateEngine();
			String finalMessage = templateEngine.process("emailTemplate.html", ctx);
			mailer.setMessage(finalMessage);
		}
	}

	/**
	 * Returns a spring template engine
	 *
	 * @return a spring template engine
	 */
	private SpringTemplateEngine getTemplateEngine() {
		logger.debug("Entering getTemplateEngine");

		//http://blog.zenika.com/2013/01/18/introducing-the-thymeleaf-template-engine/
		//https://stackoverflow.com/questions/14723310/using-thymeleaf-template-for-sending-mail-with-spring
		ClassLoaderTemplateResolver emailResolver = new ClassLoaderTemplateResolver();
		emailResolver.setPrefix("mail/");
		emailResolver.setTemplateMode("HTML");
		emailResolver.setCharacterEncoding("UTF-8");
		emailResolver.setOrder(1);

		SpringTemplateEngine engine = new SpringTemplateEngine();
		engine.addTemplateResolver(emailResolver);

		return engine;
	}

	/**
	 * Performs database updates required before the job is run. This includes
	 * updating the last start date and next run date columns.
	 *
	 * @param nextRunDate the next run date for the job
	 * @throws SQLException
	 */
	private void beforeExecution(Date nextRunDate) throws SQLException {
		logger.debug("Entering beforeExecution: nextRunDate={}", nextRunDate);

		//update last start date and next run date on art jobs table
		String sql = "UPDATE ART_JOBS SET LAST_START_DATE = ?,"
				+ " LAST_FILE_NAME='', LAST_RUN_MESSAGE='', LAST_RUN_DETAILS='',"
				+ " NEXT_RUN_DATE = ? WHERE JOB_ID = ?";

		Object[] values = {
			DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
			DatabaseUtils.toSqlTimestamp(nextRunDate),
			jobId
		};

		dbService.update(sql, values);
	}

	/**
	 * Performs database updates required after the job completes. This includes
	 * updating the last end date and last run details columns.
	 *
	 * @throws SQLException
	 */
	private void afterCompletion() throws SQLException {
		logger.debug("Entering afterCompletion");

		//update job details
		String sql = "UPDATE ART_JOBS SET LAST_END_DATE = ?, LAST_FILE_NAME = ?,"
				+ " LAST_RUN_MESSAGE=?, LAST_RUN_DETAILS=? WHERE JOB_ID = ?";

		Object[] values = {
			DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
			fileName,
			runMessage,
			runDetails,
			jobId
		};

		dbService.update(sql, values);
	}

	/**
	 * Runs a dynamic recipients job
	 *
	 */
	private void runDynamicRecipientsJob() {
		logger.debug("Entering runDynamicRecipientsJob");

		String tos = job.getMailTo();
		User jobUser = job.getUser();
		int recipientsReportId = job.getRecipientsReportId();
		String cc = job.getMailCc();
		String bcc = job.getMailBcc();

		ReportRunner recipientsReportRunner = null;
		ResultSet rs = null;

		try {
			recipientsReportRunner = prepareReportRunner(jobUser, recipientsReportId);

			recipientsReportRunner.execute();

			rs = recipientsReportRunner.getResultSet();
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();

			if (columnCount == 1) {
				//only email column. add dynamic recipient emails to Tos and run like normal job
				ArrayList<String> emailsList = new ArrayList<>();
				while (rs.next()) {
					String email = rs.getString(1); //first column has email addresses
					if (StringUtils.length(email) > 4) {
						emailsList.add(email);
					}
				}

				if (emailsList.size() > 0) {
					String emails = StringUtils.join(emailsList, ";");
					runNormalJob(emails);
				}
			} else if (columnCount > 1) {
				//personalization fields present
				//Get the column names. column indices start from 1
				ArrayList<String> columnList = new ArrayList<>();
				for (int i = 1; i < columnCount + 1; i++) {
					String columnName = rsmd.getColumnLabel(i); //use alias if available

					//store column names in lowercase to ensure special columns are found by list.contains()
					//some RDBMSs make all column names uppercase					
					columnList.add(columnName.toLowerCase(Locale.ENGLISH));
				}

				if (columnList.contains(ArtUtils.RECIPIENT_COLUMN) && columnList.contains(ArtUtils.RECIPIENT_ID)) {
					//separate emails, different email message, different report data
					while (rs.next()) {
						String email = rs.getString(1); //first column has email addresses
						if (StringUtils.length(email) > 4) {
							Map<String, String> recipientColumns = new HashMap<>();
							String columnName;
							String columnValue;
							for (int i = 1; i <= columnCount; i++) { //column numbering starts from 1 not 0
								columnName = rsmd.getColumnLabel(i); //use column alias if available

								if (rs.getString(columnName) == null) {
									columnValue = "";
								} else {
									columnValue = rs.getString(columnName);
								}
								recipientColumns.put(columnName.toLowerCase(Locale.ENGLISH), columnValue); //use lowercase so that special columns are found
							}

							Map<String, Map<String, String>> recipient = new HashMap<>();
							recipient.put(email, recipientColumns);

							//run job for this recipient
							boolean splitJob = true;
							boolean recipientFilterPresent = true;
							runJob(splitJob, jobUser, tos, recipient, recipientFilterPresent);
						}
					}

					//run normal job in case tos, cc etc configured
					if (StringUtils.length(tos) > 4 || StringUtils.length(cc) > 4
							|| StringUtils.length(bcc) > 4) {
						runNormalJob();
					}
				} else {
					//separate emails, different email message, same report data
					Map<String, Map<String, String>> recipients = new HashMap<>();
					while (rs.next()) {
						String email = rs.getString(1); //first column has email addresses
						if (StringUtils.length(email) > 4) {
							Map<String, String> recipientColumns = new HashMap<>();
							String columnName;
							String columnValue;
							for (int i = 1; i <= columnCount; i++) { //column numbering starts from 1 not 0
								columnName = rsmd.getColumnLabel(i); //use column alias if available

								if (rs.getString(columnName) == null) {
									columnValue = "";
								} else {
									columnValue = rs.getString(columnName);
								}
								recipientColumns.put(columnName, columnValue);
							}
							recipients.put(email, recipientColumns);
						}
					}

					//run job for all recipients
					boolean splitJob = true;
					runJob(splitJob, jobUser, tos, recipients);
				}
			}
		} catch (SQLException ex) {
			logger.error("Error", ex);
		} finally {
			DatabaseUtils.close(rs);

			if (recipientsReportRunner != null) {
				recipientsReportRunner.close();
			}
		}
	}

	/**
	 * Runs a normal job (not a dynamic recipients job)
	 *
	 * @param conn a connection to the art database
	 */
	private void runNormalJob() {
		logger.debug("Entering runNormalJob");
		runNormalJob(null);
	}

	/**
	 * Runs a normal job (not a dynamic recipients job)
	 *
	 * @param conn a connection the the art database
	 * @param dynamicRecipientEmails a list of dynamic recipient emails, each
	 * separated by ;
	 */
	private void runNormalJob(String dynamicRecipientEmails) {
		logger.debug("Entering runNormalJob: dynamicRecipientEmails='{}'", dynamicRecipientEmails);

		//run job. if job isn't shared, generate single output
		//if job is shared and doesn't use rules, generate single output to be used by all users
		//if job is shared and uses rules, generate multiple, individualized output for each shared user
		try {
			int userCount = 0; //number of shared users
			String ownerFileName = null; //for shared jobs, ensure the jobs table has the job owner's file

			boolean splitJob = false; //flag to determine if job will generate one file or multiple individualized files. to know which tables to update

			User jobUser = job.getUser();

			if (job.isAllowSharing()) {
				if (job.isSplitJob()) {
					//generate individualized output for all shared users

					//update art_user_jobs table with users who have access through group membership. so that users newly added to a group can get their own output
					addSharedJobUsers();

					//get users to generate output for
					String usersSql = "SELECT AUJ.USERNAME, AUJ.USER_ID, AU.EMAIL"
							+ " FROM ART_USER_JOBS AUJ"
							+ " INNER JOIN ART_USERS AU ON"
							+ " AUJ.USER_ID = AU.USER_ID"
							+ " WHERE AUJ.JOB_ID=? AND AU.ACTIVE=1";

					ResultSetHandler<List<Map<String, Object>>> h = new MapListHandler();
					List<Map<String, Object>> records = dbService.query(usersSql, h, jobId);

					for (Map<String, Object> record : records) {
						userCount += 1;
						//map list handler uses a case insensitive map, so case of column names doesn't matter
						String username = (String) record.get("USERNAME");
						Integer userId = (Integer) record.get("USER_ID");
						String email = (String) record.get("EMAIL");

						User user = new User();
						user.setUserId(userId);
						user.setUsername(username);

						runJob(splitJob, user, email);

						//ensure that the job owner's output version is saved in the jobs table
						String jobUsername = jobUser.getUsername();
						if (jobUsername.equals(username)) {
							ownerFileName = fileName;
						}
					}

					if (userCount == 0) {
						//no shared users defined yet. generate one file for the job owner
						String emails = job.getMailTo();
						if (dynamicRecipientEmails != null) {
							emails = emails + ";" + dynamicRecipientEmails;
						}
						runJob(splitJob, jobUser, emails);
					}
				} else {
					//generate one single output to be used by all users
					String emails = job.getMailTo();
					if (dynamicRecipientEmails != null) {
						emails = emails + ";" + dynamicRecipientEmails;
					}
					runJob(splitJob, jobUser, emails);
				}
			} else {
				//job isn't shared. generate one file for the job owner
				String emails = job.getMailTo();
				if (dynamicRecipientEmails != null) {
					emails = emails + ";" + dynamicRecipientEmails;
				}
				runJob(splitJob, jobUser, emails);
			}

			//ensure jobs table always has job owner's file, or a note if no output was produced for the job owner
			if (ownerFileName != null) {
				fileName = ownerFileName;
			} else if (splitJob && userCount > 0) {
				//job is shared with other users but the owner doesn't have a copy. save note in the jobs table
				runMessage = "jobs.message.jobShared";
			}
		} catch (SQLException ex) {
			logger.error("Error", ex);
		}
	}

	/**
	 * Adds records to the art_user_jobs table so that the users can have access
	 * to the job
	 *
	 * @throws SQLException
	 */
	public void addSharedJobUsers() throws SQLException {
		logger.debug("Entering addSharedJobUsers");

		String sql;

		//get users who should have access to the job through group membership but don't already have it
		sql = "SELECT AU.USERNAME, AUGA.USER_GROUP_ID"
				+ " FROM ART_USERS AU, ART_USER_GROUP_ASSIGNMENT AUGA, ART_USER_GROUP_JOBS AUGJ"
				+ " WHERE AU.USERNAME = AUGA.USERNAME AND AUGA.USER_GROUP_ID = AUGJ.USER_GROUP_ID"
				+ " AND AUGJ.JOB_ID = ?"
				+ " AND NOT EXISTS"
				+ " (SELECT * FROM ART_USER_JOBS AUJ"
				+ " WHERE AUJ.USERNAME = AU.USERNAME AND AUJ.JOB_ID = ?)";

		ResultSetHandler<List<Map<String, Object>>> h = new MapListHandler();
		List<Map<String, Object>> records = dbService.query(sql, h, jobId, jobId);

		sql = "INSERT INTO ART_USER_JOBS (JOB_ID, USERNAME, USER_GROUP_ID) VALUES (?,?,?)";

		for (Map<String, Object> record : records) {
			//map list handler uses a case insensitive map, so case of column names doesn't matter
			String username = (String) record.get("USERNAME");
			Integer userGroupId = (Integer) record.get("USER_GROUP_ID");

			//insert records into the art_user_jobs table so that the users can have access to the job
			Object[] values = {
				jobId,
				username,
				userGroupId
			};

			dbService.update(sql, values);
		}
	}

	/**
	 * Runs a job
	 *
	 * @param splitJob whether this is a split job
	 * @param user the user under which the job is run
	 * @param userEmail the email address to include in the to section of the
	 * email
	 * @throws SQLException
	 */
	private void runJob(boolean splitJob, User user, String userEmail) throws SQLException {
		runJob(splitJob, user, userEmail, null, false);
	}

	/**
	 * Runs a job
	 *
	 * @param splitJob whether this is a split job
	 * @param user the user under which the job is run
	 * @param userEmail the email address to include in the to section of the
	 * email
	 * @param recipientDetails dynamic recipient details
	 * @throws SQLException
	 */
	private void runJob(boolean splitJob, User user, String userEmail,
			Map<String, Map<String, String>> recipientDetails) throws SQLException {

		runJob(splitJob, user, userEmail, recipientDetails, false);
	}

	/**
	 * Runs a job
	 *
	 * @param splitJob whether this is a split job
	 * @param user the user under which the job is run
	 * @param userEmail the email address to include in the to section of the
	 * email
	 * @param recipientDetails dynamic recipient details
	 * @param recipientFilterPresent whether the recipient filter is present
	 * @throws SQLException
	 */
	private void runJob(boolean splitJob, User user, String userEmail,
			Map<String, Map<String, String>> recipientDetails, boolean recipientFilterPresent)
			throws SQLException {

		logger.debug("Entering runJob: splitJob={}, user={}, userEmail='{}', recipientFilterPresent={}",
				splitJob, user, userEmail, recipientFilterPresent);

		//set job start date. relevant for split jobs
		jobStartDate = new Timestamp(System.currentTimeMillis());

		ReportRunner reportRunner = null;

		fileName = ""; //reset file name

		//create job audit record if auditing is enabled
		createAuditRecord(user);

		try {
			reportRunner = prepareReportRunner(user);

			if (recipientFilterPresent) {
				//enable report data to be filtered/different for each recipient
				reportRunner.setRecipientFilterPresent(recipientFilterPresent);
				for (Map.Entry<String, Map<String, String>> entry : recipientDetails.entrySet()) {
					//map should only have one value if filter present
					Map<String, String> recipientColumns = entry.getValue();
					reportRunner.setRecipientColumn(recipientColumns.get(ArtUtils.RECIPIENT_COLUMN));
					reportRunner.setRecipientId(recipientColumns.get(ArtUtils.RECIPIENT_ID));
					reportRunner.setRecipientIdType(recipientColumns.get(ArtUtils.RECIPIENT_ID_TYPE));
				}
			}

			//prepare report parameters
			Report report = job.getReport();
			int reportId = report.getReportId();

			ParameterProcessorResult paramProcessorResult = buildParameters(reportId, jobId);
			Map<String, ReportParameter> reportParamsMap = paramProcessorResult.getReportParamsMap();
			reportRunner.setReportParamsMap(reportParamsMap);

			ReportType reportType = report.getReportType();

			//jobs don't show record count so generally no need for scrollable resultsets
			int resultSetType;
			if (reportType.isChart()) {
				//need scrollable resultset for charts for show data option
				resultSetType = ResultSet.TYPE_SCROLL_INSENSITIVE;
			} else {
				resultSetType = ResultSet.TYPE_FORWARD_ONLY;
			}

			//run report
			reportRunner.execute(resultSetType);

			//get email message fields
			String message = job.getMailMessage();

			String to = job.getMailTo();
			String cc = job.getMailCc();
			String bcc = job.getMailBcc();

			//trim address fields. to aid in checking if emails are configured
			to = StringUtils.trim(to);
			cc = StringUtils.trim(cc);
			bcc = StringUtils.trim(bcc);

			userEmail = StringUtils.trim(userEmail);

			//determine if emailing is required and emails are configured
			boolean generateEmail = isGenerateEmail(to, userEmail, cc, bcc);

			//set email fields to be used by the mailer
			String[] tos = null;
			String[] ccs = null;
			String[] bccs = null;

			if (generateEmail) {
				tos = StringUtils.split(userEmail, ";");
				ccs = StringUtils.split(cc, ";");
				bccs = StringUtils.split(bcc, ";");

				logger.debug("Job Id {}. to: {}", jobId, userEmail);
				logger.debug("Job Id {}. cc: {}", jobId, cc);
				logger.debug("Job Id {}. bcc: {}", jobId, bcc);
			}

			if (jobType == JobType.Alert) {
				runAlertJob(generateEmail, recipientDetails, reportRunner, message, recipientFilterPresent, tos, ccs, bccs);
			} else if (jobType.isPublish() || jobType.isEmail() || jobType == JobType.Print) {
				//determine if the query returns records. to know if to generate output for conditional jobs
				boolean generateOutput = isGenerateOutput(user, reportParamsMap);

				//for emailing jobs, only generate output if some emails are configured
				if (jobType.isEmail()) {
					//email attachment, email inline, conditional email attachment, conditional email inline
					if (!generateEmail && recipientDetails == null) {
						generateOutput = false;
						runMessage = "jobs.message.noEmailsConfigured";
					}
				}

				if (generateOutput) {
					//generate output
					String outputFileName = generateOutputFile(reportRunner, paramProcessorResult);

					if (jobType == JobType.Print) {
						File file = new File(outputFileName);
						Desktop desktop = Desktop.getDesktop();
						if (desktop.isSupported(Desktop.Action.PRINT)) {
							desktop.print(file);
						} else {
							logger.warn("Desktop print not supported. Job Id: {}", jobId);
						}
					} else if (generateEmail || recipientDetails != null) {
						//some kind of emailing required
						processAndSendEmail(recipientDetails, message, outputFileName, recipientFilterPresent, generateEmail, tos, ccs, bccs, userEmail, cc, bcc);
					}
				}
			} else if (jobType.isCache()) {
				runCacheJob(reportRunner);
			} else if (jobType == JobType.JustRun) {
				// do nothing
				// This is used Used to start batch jobs at db level via calls to stored procs
				// or just to run update statements.
			}

			logger.debug("Job Id {} ...finished", jobId);
		} catch (Exception ex) {
			runDetails = "<b>Error:</b> " + ex.toString();
			logger.error("Error", ex);
		} finally {
			if (reportRunner != null) {
				reportRunner.close();
			}
			// set audit timestamp and update archives
			afterExecution(splitJob, user);
		}
	}

	/**
	 * Finalizes the email message to send and sends it
	 *
	 * @param recipientDetails
	 * @param message
	 * @param outputFileName
	 * @param recipientFilterPresent
	 * @param generateEmail
	 * @param tos
	 * @param ccs
	 * @param bccs
	 * @param userEmail
	 * @param cc
	 * @param bcc
	 * @throws IOException
	 */
	private void processAndSendEmail(Map<String, Map<String, String>> recipientDetails,
			String message, String outputFileName, boolean recipientFilterPresent,
			boolean generateEmail, String[] tos, String[] ccs, String[] bccs,
			String userEmail, String cc, String bcc) throws IOException {

		logger.debug("Entering processAndSendEmail");

		//send customized emails to dynamic recipients
		if (recipientDetails != null) {
			Mailer mailer = getMailer();

			String email;

			for (Map.Entry<String, Map<String, String>> entry : recipientDetails.entrySet()) {
				email = entry.getKey();
				Map<String, String> recipientColumns = entry.getValue();

				//customize message by replacing field labels with values for this recipient
				String customMessage = prepareCustomMessage(message, recipientColumns); //message for a particular recipient. may include personalization e.g. Dear Jane
				prepareMailer(mailer, customMessage, outputFileName);

				mailer.setTo(email);

				//send email for this recipient
				try {
					sendEmail(mailer);
					runMessage = "jobs.message.fileEmailed";
				} catch (MessagingException ex) {
					logger.debug("Error", ex);
					runMessage = "jobs.message.errorSendingSomeEmails";
					runDetails = "<b>Error: </b>"
							+ " <p>" + ex.toString() + "</p>";

					String msg = "Error when sending some emails."
							+ " \n" + ex.toString()
							+ " \n To: " + email;
					logger.warn(msg);
				}
			}

			if (recipientFilterPresent) {
				//don't run normal email job after filtered email sent
				generateEmail = false;

				//delete file since email has been sent
				File f = new File(outputFileName);
				boolean deleted = f.delete();
				if (!deleted) {
					logger.warn("Email attachment file not deleted: {}", outputFileName);
				}
			}
		}

		//send email to normal recipients
		if (generateEmail) {
			Mailer mailer = getMailer();

			prepareMailer(mailer, message, outputFileName);

			//set recipients
			mailer.setTo(tos);
			mailer.setCc(ccs);
			mailer.setBcc(bccs);

			try {
				sendEmail(mailer);
				runMessage = "jobs.message.fileEmailed";

				if (jobType.isEmail()) {
					// delete the file since it has
					// been sent via email
					File file = new File(outputFileName);
					file.delete();
					fileName = "";
				} else {
					runMessage = "jobs.message.reminderSent";
				}
			} catch (MessagingException ex) {
				logger.debug("Error", ex);
				runMessage = "jobs.message.errorSendingSomeEmails";
				runDetails = "<b>Error: </b>"
						+ " <p>" + ex.toString() + "</p>";

				String msg = "Error when sending some emails."
						+ " \n" + ex.toString()
						+ " \n Complete address list:\n To: " + userEmail + "\n Cc: " + cc + "\n Bcc: " + bcc;
				logger.warn(msg);
			}
		}
	}

	/**
	 * Generates report output to file
	 *
	 * @param reportRunner the report runner to use
	 * @param paramProcessorResult the parameter processor result
	 * @return the full path to the output file used
	 * @throws Exception
	 */
	private String generateOutputFile(ReportRunner reportRunner,
			ParameterProcessorResult paramProcessorResult) throws Exception {

		logger.debug("Entering generateOutputFile");

		Report report = job.getReport();
		ReportType reportType = report.getReportType();
		ReportFormat reportFormat = ReportFormat.toEnum(job.getOutputFormat());

		//generate file name to use
		FilenameHelper filenameHelper = new FilenameHelper();
		String baseFileName = filenameHelper.getFileName(job);
		String exportPath = Config.getJobsExportPath();
		String extension;

		if (reportType.isJxls()) {
			String jxlsFilename = report.getTemplate();
			extension = FilenameUtils.getExtension(jxlsFilename);
		} else {
			extension = reportFormat.getFilenameExtension();
		}

		fileName = baseFileName + "." + extension;
		String outputFileName = exportPath + fileName;

		//create html file to output to as required
		FileOutputStream fos = null;
		PrintWriter writer = null;

		if (reportFormat.isHtml() || reportFormat == ReportFormat.xml
				|| reportFormat == ReportFormat.rss20) {
			fos = new FileOutputStream(outputFileName);
			writer = new PrintWriter(new OutputStreamWriter(fos, "UTF-8")); // make sure we make a utf-8 encoded text
		}

		//generate output
		ReportOutputGenerator reportOutputGenerator = new ReportOutputGenerator();

		reportOutputGenerator.setJobId(jobId);
		Locale locale = Locale.getDefault();

		try {
			reportOutputGenerator.generateOutput(report, reportRunner,
					reportFormat, locale, paramProcessorResult, writer, outputFileName);

		} finally {
			if (writer != null) {
				writer.close();
			}

			if (fos != null) {
				fos.close();
			}
		}

		return outputFileName;
	}

	/**
	 * Returns <code>true</code> if emailing is required and emails are
	 * configured
	 *
	 * @param tos to mail to setting
	 * @param userEmail the user email
	 * @param cc the mail cc setting
	 * @param bcc the mail bcc setting
	 * @return
	 */
	private boolean isGenerateEmail(String to, String userEmail, String cc, String bcc) {
		logger.debug("Entering isGenerateEmail: to='{}', userEmail='{}', cc='{}', bcc='{}'",
				to, userEmail, cc, bcc);

		boolean generateEmail = false;

		if (jobType.isPublish()) {
			//for split published jobs, tos should have a value to enable confirmation email for individual users
			if (!StringUtils.equals(to, userEmail) && (StringUtils.length(to) > 4
					|| StringUtils.length(cc) > 4 || StringUtils.length(bcc) > 4)
					&& StringUtils.length(userEmail) > 4) {
				generateEmail = true;
			} else if (StringUtils.equals(to, userEmail) && (StringUtils.length(to) > 4
					|| StringUtils.length(cc) > 4 || StringUtils.length(bcc) > 4)) {
				generateEmail = true;
			}
		} else {
			//for non-publish jobs, if an email address is available, generate email
			if (StringUtils.length(userEmail) > 4 || StringUtils.length(cc) > 4
					|| StringUtils.length(bcc) > 4) {
				generateEmail = true;
			}
		}

		return generateEmail;
	}

	/**
	 * Returns <code>true</code> if the job should generate some output
	 *
	 * @param user the user under whose access the job is running
	 * @param reportParamsMap the report parameters
	 * @return <code>true</code> if the job should generate some output
	 * @throws SQLException
	 */
	private boolean isGenerateOutput(User user, Map<String, ReportParameter> reportParamsMap) throws SQLException {
		logger.debug("Entering isGenerateOutput: user={}", user);

		//determine if the query returns records. to know if to generate output for conditional jobs
		boolean generateOutput = true;

		if (jobType.isConditional()) {
			//conditional job. check if resultset has records. no "recordcount" method so we have to execute query again
			ReportRunner countReportRunner = null;
			ResultSet rsCount = null;
			try {
				countReportRunner = prepareReportRunner(user);
				countReportRunner.setReportParamsMap(reportParamsMap);

				countReportRunner.execute();

				rsCount = countReportRunner.getResultSet();
				if (!rsCount.next()) {
					//no records
					generateOutput = false;
					runMessage = "jobs.message.noRecords";
				}
			} finally {
				DatabaseUtils.close(rsCount);

				if (countReportRunner != null) {
					countReportRunner.close();
				}
			}
		}

		return generateOutput;
	}

	/**
	 * Runs cache insert and cache append jobs
	 *
	 * @param reportRunner the report runner to use
	 * @throws Exception
	 */
	private void runCacheJob(ReportRunner reportRunner) throws SQLException {
		logger.debug("Entering runCacheJob");

		Connection cacheDatabaseConnection = null;
		ResultSet rs = null;

		try {
			int targetDatabaseId = job.getCachedDatasourceId();
			cacheDatabaseConnection = DbConnections.getConnection(targetDatabaseId);
			rs = reportRunner.getResultSet();

			CachedResult cr = new CachedResult();
			cr.setTargetConnection(cacheDatabaseConnection);
			cr.setResultSet(rs);

			String cachedTableName = job.getCachedTableName();
			if (StringUtils.isBlank(cachedTableName)) {
				String reportName = job.getReport().getName();
				cachedTableName = reportName + "_J" + jobId;
			}
			cr.setCachedTableName(cachedTableName);

			if (jobType == JobType.CacheAppend) {
				// 1 = append 2 = drop/insert (3 = update (not implemented))
				cr.setCacheMode(1);
			} else if (jobType == JobType.CacheInsert) {
				cr.setCacheMode(2);
			}

			cr.cacheIt();

			runDetails = "Table Name (rows inserted):  <code>"
					+ cr.getCachedTableName() + "</code> (" + cr.getRowsCount() + ")"
					+ "<br />Columns Names:<br /><code>"
					+ cr.getCachedTableColumnsName() + "</code>";
		} finally {
			DatabaseUtils.close(rs, cacheDatabaseConnection);
		}
	}

	/**
	 * Runs an alert job type
	 *
	 * @param generateEmail
	 * @param recipientDetails
	 * @param reportRunner
	 * @param message
	 * @param recipientFilterPresent
	 * @param tos
	 * @param ccs
	 * @param bccs
	 * @throws IOException
	 * @throws SQLException
	 */
	private void runAlertJob(boolean generateEmail, Map<String, Map<String, String>> recipientDetails,
			ReportRunner reportRunner, String message, boolean recipientFilterPresent,
			String[] tos, String[] ccs, String[] bccs)
			throws IOException, SQLException, TemplateException {
		/*
		 * ALERT if the resultset is not null and the first column is a
		 * positive integer => send the alert email
		 */

		//only run alert query if we have some emails configured
		if (generateEmail || recipientDetails != null) {
			runMessage = "jobs.message.noAlert";

			ResultSet rs = null;
			try {
				rs = reportRunner.getResultSet();
				if (rs.next()) {
					int value = rs.getInt(1);
					if (value > 0) {
						runMessage = "jobs.message.alertExists";

						logger.debug("Job Id {} - Raising Alert. Value is {}", jobId, value);

						Report report = job.getReport();
						ReportType reportType = report.getReportType();

						//send customized emails to dynamic recipients
						if (recipientDetails != null) {
							Mailer mailer = getMailer();

							for (Map.Entry<String, Map<String, String>> entry : recipientDetails.entrySet()) {
								String email = entry.getKey();
								Map<String, String> recipientColumns = entry.getValue();

								if (reportType == ReportType.FreeMarker) {
									prepareFreeMarkerAlertMailer(mailer, value, recipientColumns);
								} else {
									String customMessage = prepareCustomMessage(message, recipientColumns);
									prepareAlertMailer(mailer, customMessage, value);
								}

								mailer.setTo(email);

								//send email for this recipient
								try {
									sendEmail(mailer);
									runMessage = "jobs.message.alertSent";
								} catch (MessagingException ex) {
									logger.debug("Error", ex);
									runMessage = "jobs.message.errorSendingAlert";
									runDetails = "<b>Error: </b> <p>" + ex.toString() + "</p>";
								}
							}

							if (recipientFilterPresent) {
								//don't run normal email job after filtered email sent
								generateEmail = false;
							}
						}

						//send email to normal recipients
						if (generateEmail) {
							Mailer mailer = getMailer();

							if (reportType == ReportType.FreeMarker) {
								prepareFreeMarkerAlertMailer(mailer, value);
							} else {
								prepareAlertMailer(mailer, message, value);
							}

							//set recipients
							mailer.setTo(tos);
							mailer.setCc(ccs);
							mailer.setBcc(bccs);

							try {
								sendEmail(mailer);
								runMessage = "jobs.message.alertSent";
							} catch (MessagingException ex) {
								logger.debug("Error", ex);
								runMessage = "jobs.message.errorSendingAlert";
								runDetails = "<b>Error: </b> <p>" + ex.toString() + "</p>";
							}
						}
					} else {
						logger.debug("Job Id {} - No Alert. Value is {}", jobId, value);
					}
				} else {
					logger.debug("Job Id {} - Empty resultset for alert", jobId);
				}
			} finally {
				DatabaseUtils.close(rs);
			}
		} else {
			//no emails configured
			runMessage = "jobs.message.noEmailsConfigured";
		}
	}

	/**
	 * Customizes the email message for a dynamic recipient by replacing field
	 * labels with values for this recipient
	 *
	 * @param message the original email message
	 * @param recipientColumns the recipient details
	 * @return the customized message with field labels replaced
	 */
	private String prepareCustomMessage(String message, Map<String, String> recipientColumns) {
		String customMessage = message; //message for a particular recipient. may include personalization e.g. Dear Jane

		if (StringUtils.isNotBlank(customMessage)) {
			for (Map.Entry<String, String> entry : recipientColumns.entrySet()) {
				String columnName = entry.getKey();
				String columnValue = entry.getValue();

				String searchString = Pattern.quote("#" + columnName + "#"); //quote in case it contains special regex characters
				String replaceString = Matcher.quoteReplacement(columnValue); //quote in case it contains special regex characters
				customMessage = customMessage.replaceAll("(?iu)" + searchString, replaceString); //(?iu) makes replace case insensitive across unicode characters
			}
		}

		return customMessage;
	}

	/**
	 * Prepares a report runner, including setting the user and report. The
	 * report used will be that of the executing job
	 *
	 * @param user the user to set
	 */
	private ReportRunner prepareReportRunner(User user) throws SQLException {
		logger.debug("Entering prepareReportRunner: user={}", user);

		return prepareReportRunner(user, job.getReport());
	}

	/**
	 * Prepares a report runner, including setting the user and report
	 *
	 * @param user the user to set
	 * @param reportId the report id of the report to set
	 */
	private ReportRunner prepareReportRunner(User user, int reportId) throws SQLException {
		logger.debug("Entering prepareReportRunner: user={}, reportId={}", user, reportId);

		ReportService reportService = new ReportService();
		Report report = reportService.getReport(reportId);

		return prepareReportRunner(user, report);
	}

	/**
	 * Prepares a report runner, including setting the user and report
	 *
	 * @param user the user to set
	 * @param report the report to set
	 */
	private ReportRunner prepareReportRunner(User user, Report report) throws SQLException {
		logger.debug("Entering prepareReportRunner: user={}, report={}", user, report);

		ReportRunner reportRunner = new ReportRunner();
		reportRunner.setUsername(user.getUsername());
		reportRunner.setReport(report);

		return reportRunner;

	}

	/**
	 * Builds parameters for a job
	 *
	 * @param reportId the report id
	 * @param jId the job id
	 * @return parameter processor result
	 * @throws SQLException
	 */
	public ParameterProcessorResult buildParameters(int reportId, int jId) throws SQLException {
		logger.debug("Entering buildParameters: reportId={}, jId={}", reportId, jId);

		ParameterProcessorResult paramProcessorResult = null;

		JobParameterService jobParameterService = new JobParameterService();
		List<JobParameter> jobParams = jobParameterService.getJobParameters(jId);
		Map<String, List<String>> paramValues = new HashMap<>();
		for (JobParameter jobParam : jobParams) {
			String name = jobParam.getName();
			String paramTypeString = jobParam.getParamTypeString();
			if (!StringUtils.equalsIgnoreCase(paramTypeString, "O")
					&& !StringUtils.startsWith(name, "p-")) {
				name = "p-" + name;
			}
			jobParam.setName(name);
			List<String> values = paramValues.get(name);
			if (values == null) {
				paramValues.put(name, new ArrayList<String>());
			}
		}

		for (JobParameter jobParam : jobParams) {
			String name = jobParam.getName();
			String value = jobParam.getValue();
			List<String> values = paramValues.get(name);
			values.add(value);
		}

		Map<String, String[]> finalValues = new HashMap<>();

		for (JobParameter jobParam : jobParams) {
			String name = jobParam.getName();
			List<String> values = paramValues.get(name);
			String[] valuesArray = values.toArray(new String[0]);
			finalValues.put(name, valuesArray);
		}

		try {
			ParameterProcessor paramProcessor = new ParameterProcessor();
			paramProcessorResult = paramProcessor.process(finalValues, reportId);
		} catch (ParseException ex) {
			logger.error("Error", ex);
		}

		return paramProcessorResult;
	}

	/**
	 * Creates a job audit record
	 *
	 * @param user the user running the job
	 * @throws SQLException
	 */
	private void createAuditRecord(User user) throws SQLException {
		logger.debug("Entering createAuditRecord: user={}", user);

		if (job.isEnableAudit()) {
			//generate unique key for this job run
			jobAuditKey = generateKey();

			//insert job start audit values.
			String sql = "INSERT INTO ART_JOBS_AUDIT"
					+ " (JOB_ID, JOB_ACTION, JOB_AUDIT_KEY, START_DATE, USER_ID, USERNAME)"
					+ " VALUES(" + StringUtils.repeat("?", ",", 6) + ")";

			String jobAction = "S"; //job started
			Object[] values = {
				jobId,
				jobAction,
				jobAuditKey,
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				user.getUserId(),
				user.getUsername()
			};

			dbService.update(sql, values);
		}
	}

	/**
	 * Generates a key that can be used as a primary key for audit or archive
	 * records
	 *
	 * @return
	 */
	private String generateKey() {
		logger.debug("Entering generateKey");

		return ArtUtils.getUniqueId();
	}

	/**
	 * Returns a mailer object that can be used to send emails
	 *
	 * @return a mailer object that can be used to send emails
	 */
	private Mailer getMailer() {
		logger.debug("Entering getMailer");

		String smtpServer = Config.getSettings().getSmtpServer();
		String smtpUsername = Config.getSettings().getSmtpUsername();
		String smtpPassword = Config.getSettings().getSmtpPassword();

		Mailer mailer = new Mailer();
		mailer.setHost(smtpServer);
		if (StringUtils.length(smtpUsername) > 3 && smtpPassword != null) {
			mailer.setUsername(smtpUsername);
			mailer.setPassword(smtpPassword);
		}

		mailer.setPort(Config.getSettings().getSmtpPort());
		mailer.setUseAuthentication(Config.getSettings().isUseSmtpAuthentication());
		mailer.setUseStartTls(Config.getSettings().isSmtpUseStartTls());

		return mailer;
	}

	/**
	 * Updates the ART_USER_JOBS table. If Audit Flag is set, a new row is added
	 * to the ART_JOBS_AUDIT table
	 */
	private void afterExecution(boolean splitJob, User user) throws SQLException {
		logger.debug("Entering afterExecution: splitJob={}, user={}", splitJob, user);

		String sql;

		if (jobType.isPublish()) {
			String archiveFileName = job.getLastFileName();

			int runsToArchive = job.getRunsToArchive();

			if (runsToArchive > 0 && archiveFileName != null) {
				//update archives
				updateArchives(splitJob, user);
			} else {
				//if not archiving, delete previous file
				if (archiveFileName != null && !archiveFileName.startsWith("-")) {
					String filePath = Config.getJobsExportPath() + archiveFileName;
					File previousFile = new File(filePath);
					if (previousFile.exists()) {
						previousFile.delete();
					}
				}

				//delete old archives if they exist
				if (runsToArchive == 0) {
					deleteArchives();
				}
			}
		}

		//update job details
		//no need to update jobs table if non-split job. aftercompletion will do the final update to the jobs table
		if (splitJob) {
			sql = "UPDATE ART_USER_JOBS SET LAST_FILE_NAME = ?,"
					+ " LAST_RUN_DETAILS=?, LAST_RUN_MESSAGE=?,"
					+ " LAST_START_DATE = ?, LAST_END_DATE = ? "
					+ " WHERE JOB_ID = ? AND USER_ID = ?";

			if (runDetails.length() > 4000) {
				runDetails = runDetails.substring(0, 4000);
			}

			Object[] values2 = {
				fileName,
				runDetails,
				runMessage,
				jobStartDate,
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				jobId,
				user.getUserId()
			};

			dbService.update(sql, values2);
		}

		//update audit table if required
		if (job.isEnableAudit()) {
			sql = "UPDATE ART_JOBS_AUDIT SET JOB_ACTION = 'E',"
					+ " END_DATE = ? WHERE JOB_AUDIT_KEY = ? AND JOB_ID = ?";

			Object[] values3 = {
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				jobAuditKey,
				jobId
			};

			dbService.update(sql, values3);
		}
	}

	/**
	 * Updates the job archives table
	 *
	 * @param splitJob whether this is a split job
	 * @param user the user of the archive record
	 */
	private void updateArchives(boolean splitJob, User user) {
		logger.debug("Entering updateArchives: splitJob={}, user={}", splitJob, user);

		try {
			String sql;

			boolean jobShared = false;
			if (job.isAllowSharing()) {
				jobShared = true;
			}

			String jobSharedFlag;
			if (splitJob) {
				jobSharedFlag = "S";
			} else if (jobShared) {
				jobSharedFlag = "Y";
			} else {
				jobSharedFlag = "N";
			}

			String archiveId = generateKey();
			int runsToArchive = job.getRunsToArchive();

			//add record to archive
			sql = "INSERT INTO ART_JOB_ARCHIVES"
					+ " (ARCHIVE_ID,JOB_ID,USER_ID,USERNAME,ARCHIVE_FILE_NAME,"
					+ " START_DATE,END_DATE,JOB_SHARED)"
					+ " VALUES(" + StringUtils.repeat("?", ",", 8) + ")";

			Object[] values = {
				archiveId,
				jobId,
				user.getUserId(),
				user.getUsername(),
				job.getLastFileName(),
				DatabaseUtils.toSqlTimestamp(job.getLastStartDate()),
				DatabaseUtils.toSqlTimestamp(job.getLastEndDate()),
				jobSharedFlag
			};

			dbService.update(sql, values);

			//delete previous run's records
			List<String> oldRecords = new ArrayList<>();
			if (jobSharedFlag.equals("S")) {
				sql = "SELECT ARCHIVE_ID, ARCHIVE_FILE_NAME "
						+ " FROM ART_JOB_ARCHIVES "
						+ " WHERE JOB_ID=? AND USER_ID=?"
						+ " ORDER BY START_DATE DESC";

				ResultSetHandler<List<Map<String, Object>>> h = new MapListHandler();
				List<Map<String, Object>> records = dbService.query(sql, h, jobId, user.getUserId());

				int count = 0;
				for (Map<String, Object> record : records) {
					count++;
					if (count > runsToArchive) {
						//delete archive file and database record
						String oldFileName = (String) record.get("ARCHIVE_FILE_NAME");
						String oldArchive = (String) record.get("ARCHIVE_ID");

						//remember database record for deletion
						oldRecords.add("'" + oldArchive + "'");

						//delete file
						if (oldFileName != null && !oldFileName.startsWith("-")) {
							String filePath = Config.getJobsExportPath() + oldFileName;
							File previousFile = new File(filePath);
							if (previousFile.exists()) {
								previousFile.delete();
							}
						}
					}
				}
			} else {
				sql = "SELECT ARCHIVE_ID, ARCHIVE_FILE_NAME "
						+ " FROM ART_JOB_ARCHIVES "
						+ " WHERE JOB_ID=?"
						+ " ORDER BY START_DATE DESC";

				ResultSetHandler<List<Map<String, Object>>> h = new MapListHandler();
				List<Map<String, Object>> records = dbService.query(sql, h, jobId);

				int count = 0;
				for (Map<String, Object> record : records) {
					count++;
					if (count > runsToArchive) {
						//delete archive file and database record
						String oldFileName = (String) record.get("ARCHIVE_FILE_NAME");
						String oldArchive = (String) record.get("ARCHIVE_ID");

						//remember database record for deletion
						oldRecords.add("'" + oldArchive + "'");

						//delete file
						if (oldFileName != null && !oldFileName.startsWith("-")) {
							String filePath = Config.getJobsExportPath() + oldFileName;
							File previousFile = new File(filePath);
							if (previousFile.exists()) {
								previousFile.delete();
							}
						}
					}
				}
			}

			//delete old archive records
			if (oldRecords.size() > 0) {
				String oldRecordsString = StringUtils.join(oldRecords, ",");
				sql = "DELETE FROM ART_JOB_ARCHIVES WHERE ARCHIVE_ID IN(" + oldRecordsString + ")";
				dbService.update(sql);
			}
		} catch (SQLException ex) {
			logger.error("Error", ex);
		}
	}

	/**
	 * Deletes all records from job archives table for this job
	 */
	private void deleteArchives() {
		logger.debug("Entering deleteArchives");

		try {
			String sql;

			List<String> oldRecords = new ArrayList<>();

			sql = "SELECT ARCHIVE_ID, ARCHIVE_FILE_NAME"
					+ " FROM ART_JOB_ARCHIVES"
					+ " WHERE JOB_ID=?";

			ResultSetHandler<List<Map<String, Object>>> h = new MapListHandler();
			List<Map<String, Object>> records = dbService.query(sql, h, jobId);

			for (Map<String, Object> record : records) {
				//delete archive file and database record
				String oldFileName = (String) record.get("ARCHIVE_FILE_NAME");
				String oldArchive = (String) record.get("ARCHIVE_ID");

				//remember database record for deletion
				oldRecords.add("'" + oldArchive + "'");

				//delete file
				if (oldFileName != null && !oldFileName.startsWith("-")) {
					List<String> details = ArtUtils.getFileDetailsFromResult(oldFileName);
					oldFileName = details.get(0);
					String filePath = Config.getJobsExportPath() + oldFileName;
					File previousFile = new File(filePath);
					if (previousFile.exists()) {
						previousFile.delete();
					}
				}
			}

			//delete old archive records
			if (oldRecords.size() > 0) {
				String oldRecordsString = StringUtils.join(oldRecords, ",");
				sql = "DELETE FROM ART_JOB_ARCHIVES WHERE ARCHIVE_ID IN(" + oldRecordsString + ")";
				dbService.update(sql);
			}
		} catch (SQLException ex) {
			logger.error("Error", ex);
		}
	}
}
