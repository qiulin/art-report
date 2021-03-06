/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package art.job;

import art.connectionpool.DbConnections;
import art.dbutils.DbService;
import art.dbutils.DatabaseUtils;
import art.destination.Destination;
import art.destination.DestinationService;
import art.enums.JobType;
import art.holiday.Holiday;
import art.holiday.HolidayService;
import art.jobrunners.ReportJob;
import art.report.Report;
import art.report.ReportService;
import art.schedule.Schedule;
import art.schedule.ScheduleService;
import art.smtpserver.SmtpServer;
import art.smtpserver.SmtpServerService;
import art.user.User;
import art.user.UserService;
import art.utils.ArtUtils;
import art.utils.CachedResult;
import art.utils.CronStringHelper;
import art.utils.ExpressionHelper;
import art.utils.SchedulerUtils;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import static org.quartz.JobKey.jobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import static org.quartz.TriggerKey.triggerKey;
import org.quartz.impl.calendar.CronCalendar;
import org.quartz.impl.triggers.AbstractTrigger;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Provides methods for retrieving, adding, updating and deleting jobs
 *
 * @author Timothy Anyona
 */
@Service
public class JobService {

	private static final Logger logger = LoggerFactory.getLogger(JobService.class);

	private final DbService dbService;
	private final ReportService reportService;
	private final UserService userService;
	private final ScheduleService scheduleService;
	private final HolidayService holidayService;
	private final DestinationService destinationService;
	private final SmtpServerService smtpServerService;

	@Autowired
	public JobService(DbService dbService, ReportService reportService,
			UserService userService, ScheduleService scheduleService,
			HolidayService holidayService, DestinationService destinationService,
			SmtpServerService smtpServerService) {

		this.dbService = dbService;
		this.reportService = reportService;
		this.userService = userService;
		this.scheduleService = scheduleService;
		this.holidayService = holidayService;
		this.destinationService = destinationService;
		this.smtpServerService = smtpServerService;
	}

	public JobService() {
		dbService = new DbService();
		reportService = new ReportService();
		userService = new UserService();
		scheduleService = new ScheduleService();
		holidayService = new HolidayService();
		destinationService = new DestinationService();
		smtpServerService = new SmtpServerService();
	}

	private final String SQL_SELECT_ALL = "SELECT * FROM ART_JOBS AJ";

	/**
	 * Maps a resultset to an object
	 */
	private class JobMapper extends BasicRowProcessor {

		@Override
		public <T> List<T> toBeanList(ResultSet rs, Class<T> type) throws SQLException {
			List<T> list = new ArrayList<>();
			while (rs.next()) {
				list.add(toBean(rs, type));
			}
			return list;
		}

		@Override
		public <T> T toBean(ResultSet rs, Class<T> type) throws SQLException {
			Job job = new Job();

			populateJob(job, rs);

			return type.cast(job);
		}

	}

	/**
	 * Maps a resultset to an object
	 */
	private class SharedJobMapper extends BasicRowProcessor {

		@Override
		public <T> List<T> toBeanList(ResultSet rs, Class<T> type) throws SQLException {
			List<T> list = new ArrayList<>();
			while (rs.next()) {
				list.add(toBean(rs, type));
			}
			return list;
		}

		@Override
		public <T> T toBean(ResultSet rs, Class<T> type) throws SQLException {
			SharedJob job = new SharedJob();

			populateJob(job, rs);

			return type.cast(job);
		}
	}

	/**
	 * Populates a job object
	 *
	 * @param job the job to populate
	 * @param rs the resultset that contains the job's details
	 * @throws SQLException
	 */
	private void populateJob(Job job, ResultSet rs) throws SQLException {
		job.setJobId(rs.getInt("JOB_ID"));
		job.setName(rs.getString("JOB_NAME"));
		job.setOutputFormat(rs.getString("OUTPUT_FORMAT"));
		job.setJobType(JobType.toEnum(rs.getString("JOB_TYPE")));
		job.setScheduleSecond(rs.getString("JOB_SECOND"));
		job.setScheduleMinute(rs.getString("JOB_MINUTE"));
		job.setScheduleHour(rs.getString("JOB_HOUR"));
		job.setScheduleDay(rs.getString("JOB_DAY"));
		job.setScheduleMonth(rs.getString("JOB_MONTH"));
		job.setScheduleWeekday(rs.getString("JOB_WEEKDAY"));
		job.setScheduleYear(rs.getString("JOB_YEAR"));
		job.setScheduleTimeZone(rs.getString("TIME_ZONE"));
		job.setMailTo(rs.getString("MAIL_TOS"));
		job.setMailFrom(rs.getString("MAIL_FROM"));
		job.setMailCc(rs.getString("MAIL_CC"));
		job.setMailBcc(rs.getString("MAIL_BCC"));
		job.setMailSubject(rs.getString("SUBJECT"));
		job.setMailMessage(rs.getString("MESSAGE"));
		job.setCachedDatasourceId(rs.getInt("CACHED_DATASOURCE_ID"));
		job.setCachedTableName(rs.getString("CACHED_TABLE_NAME"));
		job.setStartDate(rs.getTimestamp("START_DATE"));
		job.setEndDate(rs.getTimestamp("END_DATE"));
		job.setNextRunDate(rs.getTimestamp("NEXT_RUN_DATE"));
		job.setLastFileName(rs.getString("LAST_FILE_NAME"));
		job.setLastRunMessage(rs.getString("LAST_RUN_MESSAGE"));
		job.setLastRunDetails(rs.getString("LAST_RUN_DETAILS"));
		job.setLastStartDate(rs.getTimestamp("LAST_START_DATE"));
		job.setLastEndDate(rs.getTimestamp("LAST_END_DATE"));
		job.setActive(rs.getBoolean("ACTIVE"));
		job.setEnableAudit(rs.getBoolean("ENABLE_AUDIT"));
		job.setAllowSharing(rs.getBoolean("ALLOW_SHARING"));
		job.setAllowSplitting(rs.getBoolean("ALLOW_SPLITTING"));
		job.setRecipientsReportId(rs.getInt("RECIPIENTS_QUERY_ID"));
		job.setRunsToArchive(rs.getInt("RUNS_TO_ARCHIVE"));
		job.setFixedFileName(rs.getString("FIXED_FILE_NAME"));
		job.setSubDirectory(rs.getString("SUB_DIRECTORY"));
		job.setBatchFile(rs.getString("BATCH_FILE"));
		job.setEmailTemplate(rs.getString("EMAIL_TEMPLATE"));
		job.setExtraSchedules(rs.getString("EXTRA_SCHEDULES"));
		job.setHolidays(rs.getString("HOLIDAYS"));
		job.setQuartzCalendarNames(rs.getString("QUARTZ_CALENDAR_NAMES"));
		job.setOptions(rs.getString("JOB_OPTIONS"));
		job.setErrorNotificationTo(rs.getString("ERROR_EMAIL_TO"));
		job.setPreRunReport(rs.getString("PRE_RUN_REPORT"));
		job.setPostRunReport(rs.getString("POST_RUN_REPORT"));
		job.setCreationDate(rs.getTimestamp("CREATION_DATE"));
		job.setUpdateDate(rs.getTimestamp("UPDATE_DATE"));
		job.setCreatedBy(rs.getString("CREATED_BY"));
		job.setUpdatedBy(rs.getString("UPDATED_BY"));

		Report report = reportService.getReport(rs.getInt("QUERY_ID"));
		job.setReport(report);

		User user = userService.getUser(rs.getInt("USER_ID"));
		job.setUser(user);

		Schedule schedule = scheduleService.getSchedule(rs.getInt("SCHEDULE_ID"));
		job.setSchedule(schedule);

		SmtpServer smtpServer = smtpServerService.getSmtpServer(rs.getInt("SMTP_SERVER_ID"));
		job.setSmtpServer(smtpServer);

		List<Holiday> sharedHolidays = holidayService.getJobHolidays(job.getJobId());
		job.setSharedHolidays(sharedHolidays);

		List<Destination> destinations = destinationService.getJobDestinations(job.getJobId());
		job.setDestinations(destinations);
	}

	/**
	 * Returns all jobs
	 *
	 * @return all jobs
	 * @throws SQLException
	 */
	@Cacheable("jobs")
	public List<Job> getAllJobs() throws SQLException {
		logger.debug("Entering getAllJobs");

		ResultSetHandler<List<Job>> h = new BeanListHandler<>(Job.class, new JobMapper());
		return dbService.query(SQL_SELECT_ALL, h);
	}

	/**
	 * Returns jobs with given ids
	 *
	 * @param ids comma separated string of the job ids to retrieve
	 * @return jobs with given ids
	 * @throws SQLException
	 */
	public List<Job> getJobs(String ids) throws SQLException {
		logger.debug("Entering getJobs: ids='{}'", ids);

		Object[] idsArray = ArtUtils.idsToObjectArray(ids);

		String sql = SQL_SELECT_ALL
				+ " WHERE JOB_ID IN(" + StringUtils.repeat("?", ",", idsArray.length) + ")";

		ResultSetHandler<List<Job>> h = new BeanListHandler<>(Job.class, new JobMapper());
		return dbService.query(sql, h, idsArray);
	}

	/**
	 * Returns a job with the given id
	 *
	 * @param id the job id
	 * @return job if found, null otherwise
	 * @throws SQLException
	 */
	@Cacheable("jobs")
	public Job getJob(int id) throws SQLException {
		logger.debug("Entering getJob: id={}", id);

		String sql = SQL_SELECT_ALL + " WHERE JOB_ID=?";
		ResultSetHandler<Job> h = new BeanHandler<>(Job.class, new JobMapper());
		return dbService.query(sql, h, id);
	}

	/**
	 * Deletes a job
	 *
	 * @param id the job id
	 * @throws SQLException
	 * @throws org.quartz.SchedulerException
	 */
	@CacheEvict(value = "jobs", allEntries = true)
	public void deleteJob(int id) throws SQLException, SchedulerException {
		logger.debug("Entering deleteJob: id={}", id);

		//get job object. need job details in order to delete cached table for cached result jobs
		Job job = getJob(id);
		if (job == null) {
			logger.warn("Cannot delete job {}. Job not available.", id);
			return;
		}

		//delete records in quartz tables
		Scheduler scheduler = SchedulerUtils.getScheduler();

		if (scheduler == null) {
			logger.warn("Cannot delete job {}. Scheduler not available.", id);
			return;
		}

		deleteQuartzJob(job, scheduler);

		// Delete the Cached table if this job is a cache result one
		JobType jobType = job.getJobType();
		if (jobType.isCache()) {
			// Delete
			int targetDatabaseId = Integer.parseInt(job.getOutputFormat());
			Connection connCache = DbConnections.getConnection(targetDatabaseId);
			try {
				String cachedTableName = job.getCachedTableName();
				if (StringUtils.isBlank(cachedTableName)) {
					cachedTableName = job.getReport().getName() + "_J" + job.getJobId();
				}
				CachedResult cr = new CachedResult();
				cr.setTargetConnection(connCache);
				cr.setCachedTableName(cachedTableName);
				cr.drop(); //potential sql injection. drop hardcoded table names only
			} finally {
				DatabaseUtils.close(connCache);
			}
		}

		String sql;

		//delete foreign key records
		sql = "DELETE FROM ART_JOBS_PARAMETERS WHERE JOB_ID=?";
		dbService.update(sql, id);

		sql = "DELETE FROM ART_USER_JOB_MAP WHERE JOB_ID=?";
		dbService.update(sql, id);

		sql = "DELETE FROM ART_USER_GROUP_JOBS WHERE JOB_ID=?";
		dbService.update(sql, id);

		sql = "DELETE FROM ART_JOB_ARCHIVES WHERE JOB_ID=?";
		dbService.update(sql, id);

		//finally delete job
		sql = "DELETE FROM ART_JOBS WHERE JOB_ID=?";
		dbService.update(sql, id);
	}

	/**
	 * Deletes multiple jobs
	 *
	 * @param ids the job ids to delete
	 * @throws SQLException
	 * @throws org.quartz.SchedulerException
	 */
	@CacheEvict(value = "jobs", allEntries = true)
	public void deleteJobs(Integer[] ids) throws SQLException, SchedulerException {
		logger.debug("Entering deleteJobs: ids={}", (Object) ids);

		for (Integer id : ids) {
			deleteJob(id);
		}
	}

	/**
	 * Adds a new job to the database
	 *
	 * @param job the new job
	 * @param actionUser the user who is performing the action
	 * @return new record id
	 * @throws SQLException
	 */
	@CacheEvict(value = "jobs", allEntries = true)
	public synchronized int addJob(Job job, User actionUser) throws SQLException {
		logger.debug("Entering addJob: job={}, actionUser={}", job, actionUser);

		//generate new id
		String sql = "SELECT MAX(JOB_ID) FROM ART_JOBS";
		int newId = dbService.getNewRecordId(sql);

		saveJob(job, newId, actionUser);

		return newId;
	}

	/**
	 * Updates an existing job
	 *
	 * @param job the updated job
	 * @param actionUser the user who is performing the action
	 * @throws SQLException
	 */
	@CacheEvict(value = "jobs", allEntries = true)
	public void updateJob(Job job, User actionUser) throws SQLException {
		logger.debug("Entering updateJob: job={}, actionUser={}", job, actionUser);

		Integer newRecordId = null;
		saveJob(job, newRecordId, actionUser);
	}

	/**
	 * Imports job records
	 *
	 * @param jobs the list of jobs to import
	 * @param actionUser the user who is performing the import
	 * @param conn the connection to use
	 * @throws SQLException
	 */
	@CacheEvict(value = "jobs", allEntries = true)
	public void importJobs(List<Job> jobs, User actionUser,
			Connection conn) throws SQLException {

		logger.debug("Entering importJobs: actionUser={}", actionUser);

		boolean originalAutoCommit = true;

		try {
			String sql = "SELECT MAX(JOB_ID) FROM ART_JOBS";
			int id = dbService.getMaxRecordId(conn, sql);

			originalAutoCommit = conn.getAutoCommit();
			conn.setAutoCommit(false);

			for (Job job : jobs) {
				id++;
				saveJob(job, id, actionUser, conn);
			}
			conn.commit();
		} catch (SQLException ex) {
			conn.rollback();
			throw ex;
		} finally {
			conn.setAutoCommit(originalAutoCommit);
		}
	}

	/**
	 * Saves a job
	 *
	 * @param job the job to save
	 * @param newRecordId id of the new record or null if editing an existing
	 * record
	 * @param actionUser the user who is performing the save
	 * @throws SQLException
	 */
	private void saveJob(Job job, Integer newRecordId,
			User actionUser) throws SQLException {

		Connection conn = null;
		saveJob(job, newRecordId, actionUser, conn);
	}

	/**
	 * Saves a job
	 *
	 * @param job the job to save
	 * @param newRecordId id of the new record or null if editing an existing
	 * record
	 * @param actionUser the user who is performing the action
	 * @param conn the connection to use. if null, the art database will be used
	 * @throws SQLException
	 */
	private void saveJob(Job job, Integer newRecordId,
			User actionUser, Connection conn) throws SQLException {

		logger.debug("Entering saveJob: job={}, newRecordId={}, actionUser={}",
				job, newRecordId, actionUser);

		Integer reportId; //database column doesn't allow null
		if (job.getReport() == null) {
			reportId = 0;
		} else {
			reportId = job.getReport().getReportId();
		}

		Integer userId;
		String username;
		if (job.getUser() == null) {
			userId = null;
			username = "";
		} else {
			userId = job.getUser().getUserId();
			username = job.getUser().getUsername();
		}

		Integer recipientsReportId = job.getRecipientsReportId();
		if (recipientsReportId == 0) {
			recipientsReportId = null;
		}

		Integer ftpServerId = 0;

		Integer scheduleId = null;
		if (job.getSchedule() != null) {
			scheduleId = job.getSchedule().getScheduleId();
			if (scheduleId == 0) {
				scheduleId = null;
			}
		}

		Integer smtpServerId = null;
		if (job.getSmtpServer() != null) {
			smtpServerId = job.getSmtpServer().getSmtpServerId();
			if (smtpServerId == 0) {
				smtpServerId = null;
			}
		}

		int affectedRows;

		boolean newRecord = false;
		if (newRecordId != null) {
			newRecord = true;
		}

		if (newRecord) {
			String sql = "INSERT INTO ART_JOBS"
					+ " (JOB_ID, JOB_NAME, QUERY_ID, USER_ID, USERNAME,"
					+ " OUTPUT_FORMAT, JOB_TYPE, JOB_SECOND, JOB_MINUTE, JOB_HOUR, JOB_DAY,"
					+ " JOB_MONTH, JOB_WEEKDAY, JOB_YEAR, TIME_ZONE,"
					+ " MAIL_TOS, MAIL_FROM, MAIL_CC, MAIL_BCC,"
					+ " SUBJECT, MESSAGE, CACHED_DATASOURCE_ID, CACHED_TABLE_NAME,"
					+ " START_DATE, END_DATE, NEXT_RUN_DATE,"
					+ " ACTIVE, ENABLE_AUDIT, ALLOW_SHARING, ALLOW_SPLITTING,"
					+ " RECIPIENTS_QUERY_ID, RUNS_TO_ARCHIVE,"
					+ " FIXED_FILE_NAME, SUB_DIRECTORY, BATCH_FILE,"
					+ " FTP_SERVER_ID, EMAIL_TEMPLATE,"
					+ " EXTRA_SCHEDULES, HOLIDAYS, QUARTZ_CALENDAR_NAMES,"
					+ " SCHEDULE_ID, SMTP_SERVER_ID, JOB_OPTIONS, ERROR_EMAIL_TO,"
					+ " PRE_RUN_REPORT, POST_RUN_REPORT,"
					+ " CREATION_DATE, CREATED_BY)"
					+ " VALUES(" + StringUtils.repeat("?", ",", 48) + ")";

			Object[] values = {
				newRecordId,
				job.getName(),
				reportId,
				userId,
				username,
				job.getOutputFormat(),
				job.getJobType().getValue(),
				job.getScheduleSecond(),
				job.getScheduleMinute(),
				job.getScheduleHour(),
				job.getScheduleDay(),
				job.getScheduleMonth(),
				job.getScheduleWeekday(),
				job.getScheduleYear(),
				job.getScheduleTimeZone(),
				job.getMailTo(),
				job.getMailFrom(),
				job.getMailCc(),
				job.getMailBcc(),
				job.getMailSubject(),
				job.getMailMessage(),
				job.getCachedDatasourceId(),
				job.getCachedTableName(),
				DatabaseUtils.toSqlTimestamp(job.getStartDate()),
				DatabaseUtils.toSqlTimestamp(job.getEndDate()),
				DatabaseUtils.toSqlTimestamp(job.getNextRunDate()),
				BooleanUtils.toInteger(job.isActive()),
				BooleanUtils.toInteger(job.isEnableAudit()),
				BooleanUtils.toInteger(job.isAllowSharing()),
				BooleanUtils.toInteger(job.isAllowSplitting()),
				recipientsReportId,
				job.getRunsToArchive(),
				job.getFixedFileName(),
				job.getSubDirectory(),
				job.getBatchFile(),
				ftpServerId,
				job.getEmailTemplate(),
				job.getExtraSchedules(),
				job.getHolidays(),
				job.getQuartzCalendarNames(),
				scheduleId,
				smtpServerId,
				job.getOptions(),
				job.getErrorNotificationTo(),
				job.getPreRunReport(),
				job.getPostRunReport(),
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				actionUser.getUsername()
			};

			if (conn == null) {
				affectedRows = dbService.update(sql, values);
			} else {
				affectedRows = dbService.update(conn, sql, values);
			}
		} else {
			String sql = "UPDATE ART_JOBS SET JOB_NAME=?, QUERY_ID=?,"
					+ " USER_ID=?, USERNAME=?, OUTPUT_FORMAT=?, JOB_TYPE=?,"
					+ " JOB_SECOND=?, JOB_MINUTE=?, JOB_HOUR=?, JOB_DAY=?,"
					+ " JOB_MONTH=?, JOB_WEEKDAY=?, JOB_YEAR=?, TIME_ZONE=?,"
					+ " MAIL_TOS=?, MAIL_FROM=?, MAIL_CC=?, MAIL_BCC=?,"
					+ " SUBJECT=?, MESSAGE=?, CACHED_DATASOURCE_ID=?, CACHED_TABLE_NAME=?,"
					+ " START_DATE=?, END_DATE=?, NEXT_RUN_DATE=?,"
					+ " ACTIVE=?, ENABLE_AUDIT=?,"
					+ " ALLOW_SHARING=?, ALLOW_SPLITTING=?, RECIPIENTS_QUERY_ID=?,"
					+ " RUNS_TO_ARCHIVE=?, FIXED_FILE_NAME=?, SUB_DIRECTORY=?,"
					+ " BATCH_FILE=?, FTP_SERVER_ID=?,"
					+ " EMAIL_TEMPLATE=?, EXTRA_SCHEDULES=?, HOLIDAYS=?,"
					+ " QUARTZ_CALENDAR_NAMES=?, SCHEDULE_ID=?, SMTP_SERVER_ID=?,"
					+ " JOB_OPTIONS=?, ERROR_EMAIL_TO=?,"
					+ " PRE_RUN_REPORT=?, POST_RUN_REPORT=?,"
					+ " UPDATE_DATE=?, UPDATED_BY=?"
					+ " WHERE JOB_ID=?";

			Object[] values = {
				job.getName(),
				reportId,
				userId,
				username,
				job.getOutputFormat(),
				job.getJobType().getValue(),
				job.getScheduleSecond(),
				job.getScheduleMinute(),
				job.getScheduleHour(),
				job.getScheduleDay(),
				job.getScheduleMonth(),
				job.getScheduleWeekday(),
				job.getScheduleYear(),
				job.getScheduleTimeZone(),
				job.getMailTo(),
				job.getMailFrom(),
				job.getMailCc(),
				job.getMailBcc(),
				job.getMailSubject(),
				job.getMailMessage(),
				job.getCachedDatasourceId(),
				job.getCachedTableName(),
				DatabaseUtils.toSqlTimestamp(job.getStartDate()),
				DatabaseUtils.toSqlTimestamp(job.getEndDate()),
				DatabaseUtils.toSqlTimestamp(job.getNextRunDate()),
				BooleanUtils.toInteger(job.isActive()),
				BooleanUtils.toInteger(job.isEnableAudit()),
				BooleanUtils.toInteger(job.isAllowSharing()),
				BooleanUtils.toInteger(job.isAllowSplitting()),
				recipientsReportId,
				job.getRunsToArchive(),
				job.getFixedFileName(),
				job.getSubDirectory(),
				job.getBatchFile(),
				ftpServerId,
				job.getEmailTemplate(),
				job.getExtraSchedules(),
				job.getHolidays(),
				job.getQuartzCalendarNames(),
				scheduleId,
				smtpServerId,
				job.getOptions(),
				job.getErrorNotificationTo(),
				job.getPreRunReport(),
				job.getPostRunReport(),
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				actionUser.getUsername(),
				job.getJobId()
			};

			if (conn == null) {
				affectedRows = dbService.update(sql, values);
			} else {
				affectedRows = dbService.update(conn, sql, values);
			}
		}

		if (newRecordId != null) {
			job.setJobId(newRecordId);
		}

		logger.debug("affectedRows={}", affectedRows);

		if (affectedRows != 1) {
			logger.warn("Problem with save. affectedRows={}, newRecord={}, job={}",
					affectedRows, newRecord, job);
		}
	}

	/**
	 * Updates multiple jobs
	 *
	 * @param multipleJobEdit the multiple job edit details
	 * @param actionUser the user who is performing the action
	 * @throws SQLException
	 */
	@CacheEvict(value = "jobs", allEntries = true)
	public void updateJobs(MultipleJobEdit multipleJobEdit, User actionUser) throws SQLException {
		logger.debug("Entering updateJobs: multipleJobEdit={}, actionUser={}", multipleJobEdit, actionUser);

		String sql;

		List<Object> idsList = ArtUtils.idsToObjectList(multipleJobEdit.getIds());
		if (!multipleJobEdit.isActiveUnchanged()) {
			sql = "UPDATE ART_JOBS SET ACTIVE=?, UPDATED_BY=?, UPDATE_DATE=?"
					+ " WHERE JOB_ID IN(" + StringUtils.repeat("?", ",", idsList.size()) + ")";

			List<Object> valuesList = new ArrayList<>();
			valuesList.add(BooleanUtils.toInteger(multipleJobEdit.isActive()));
			valuesList.add(actionUser.getUsername());
			valuesList.add(DatabaseUtils.getCurrentTimeAsSqlTimestamp());
			valuesList.addAll(idsList);

			Object[] valuesArray = valuesList.toArray(new Object[valuesList.size()]);

			dbService.update(sql, valuesArray);
		}
		User jobUser = multipleJobEdit.getUser();
		if (jobUser != null) {
			sql = "UPDATE ART_JOBS SET USER_ID=?, USERNAME=?, UPDATED_BY=?, UPDATE_DATE=?"
					+ " WHERE JOB_ID IN(" + StringUtils.repeat("?", ",", idsList.size()) + ")";

			List<Object> valuesList = new ArrayList<>();
			valuesList.add(jobUser.getUserId());
			valuesList.add(jobUser.getUsername());
			valuesList.add(actionUser.getUsername());
			valuesList.add(DatabaseUtils.getCurrentTimeAsSqlTimestamp());
			valuesList.addAll(idsList);

			Object[] valuesArray = valuesList.toArray(new Object[valuesList.size()]);

			dbService.update(sql, valuesArray);
		}
	}

	/**
	 * Returns all the jobs a user has access to - both the jobs the user owns
	 * and jobs shared with him.
	 *
	 * @param userId the user id
	 * @return all the jobs the user has access to
	 * @throws java.sql.SQLException
	 */
	public List<Job> getUserJobs(int userId) throws SQLException {
		logger.debug("Entering getUserJobs: userId={}", userId);

		List<Job> jobs = new ArrayList<>();

		jobs.addAll(getOwnedJobs(userId));
		jobs.addAll(getSharedJobs(userId));

		return jobs;
	}

	/**
	 * Returns the jobs a user owns
	 *
	 * @param userId the user id
	 * @return all the jobs that the user owns
	 * @throws java.sql.SQLException
	 */
	public List<Job> getOwnedJobs(int userId) throws SQLException {
		logger.debug("Entering getOwnedJobs: userId={}", userId);

		String sql = SQL_SELECT_ALL + " WHERE AJ.USER_ID=?";
		ResultSetHandler<List<Job>> h = new BeanListHandler<>(Job.class, new JobMapper());
		return dbService.query(sql, h, userId);
	}

	/**
	 * Returns the shared jobs a user has access to
	 *
	 * @param userId the user id
	 * @return all the shared jobs the user has access to
	 * @throws java.sql.SQLException
	 */
	public List<SharedJob> getSharedJobs(int userId) throws SQLException {
		logger.debug("Entering getSharedJobs: userId={}", userId);

		List<SharedJob> jobs = new ArrayList<>();
		String sql;

		//get shared jobs user has access to via job membership. 
		//non-split jobs. no entries for them in the art_user_job_map table
		sql = SQL_SELECT_ALL + " INNER JOIN ART_USER_GROUP_JOBS AUGJ"
				+ " ON AJ.JOB_ID=AUGJ.JOB_ID"
				+ " WHERE AJ.USER_ID <> ? AND EXISTS"
				+ " (SELECT * FROM ART_USER_USERGROUP_MAP AUUGM WHERE AUUGM.USER_ID=?"
				+ " AND AUUGM.USER_GROUP_ID=AUGJ.USER_GROUP_ID)";
		ResultSetHandler<List<SharedJob>> h = new BeanListHandler<>(SharedJob.class, new SharedJobMapper());
		jobs.addAll(dbService.query(sql, h, userId, userId));

		//get shared jobs user has direct access to, but doesn't own. both split and non-split jobs
		//stored in the art_user_job_map table
		sql = SQL_SELECT_ALL + " INNER JOIN ART_USER_JOB_MAP AUJM"
				+ " ON AJ.JOB_ID=AUJM.JOB_ID"
				+ " WHERE AJ.USER_ID<>? AND AUJM.USER_ID=?";
		jobs.addAll(dbService.query(sql, h, userId, userId));

		return jobs;
	}

	/**
	 * Returns jobs that have not been migrated to the quartz scheduling system
	 *
	 * @return jobs that have not been migrated to the quartz scheduling system
	 * @throws java.sql.SQLException
	 */
	public List<Job> getNonQuartzJobs() throws SQLException {
		logger.debug("Entering getNonQuartzJobs");

		String sql = SQL_SELECT_ALL + " WHERE AJ.MIGRATED_TO_QUARTZ='N'";
		ResultSetHandler<List<Job>> h = new BeanListHandler<>(Job.class, new JobMapper());
		return dbService.query(sql, h);
	}

	/**
	 * Deletes the quartz job associated with the given art job, also deleting
	 * any associated triggers and calendars
	 *
	 * @param job the art job object
	 * @param scheduler the quartz scheduler
	 * @throws org.quartz.SchedulerException
	 */
	public void deleteQuartzJob(Job job, Scheduler scheduler) throws SchedulerException {
		if (scheduler == null) {
			return;
		}

		int jobId = job.getJobId();
		String jobName = "job" + jobId;

		scheduler.deleteJob(jobKey(jobName, ArtUtils.JOB_GROUP));

		String quartzCalendarNames = job.getQuartzCalendarNames();
		if (StringUtils.isNotBlank(quartzCalendarNames)) {
			String[] calendarNames = StringUtils.split(quartzCalendarNames, ",");
			for (String calendarName : calendarNames) {
				try {
					scheduler.deleteCalendar(calendarName);
				} catch (SchedulerException ex) {
					logger.error("Error", ex);
				}
			}
		}
	}

	/**
	 * Returns jobs that use a given schedule as a fixed schedule
	 *
	 * @param scheduleId the schedule id
	 * @return jobs that use the schedule as a fixed schedule
	 * @throws SQLException
	 */
	public List<Job> getScheduleJobs(int scheduleId) throws SQLException {
		logger.debug("Entering getScheduleJobs");

		String sql = SQL_SELECT_ALL + " WHERE SCHEDULE_ID=?";
		ResultSetHandler<List<Job>> h = new BeanListHandler<>(Job.class, new JobMapper());
		return dbService.query(sql, h, scheduleId);
	}

	/**
	 * Returns jobs that use a given holiday, either directly as shared holiday
	 * or as part of a fixed holiday
	 *
	 * @param holidayId the holiday id
	 * @return jobs that use the holiday
	 * @throws SQLException
	 */
	public List<Job> getHolidayJobs(int holidayId) throws SQLException {
		logger.debug("Entering getHolidayJobs");

		String sql = SQL_SELECT_ALL
				//where holidays are used directly
				+ " WHERE EXISTS (SELECT *"
				+ " FROM ART_JOB_HOLIDAY_MAP AJHM"
				+ " WHERE AJHM.JOB_ID=AJ.JOB_ID AND AJHM.HOLIDAY_ID=?)"
				+ " OR"
				//where holidays are part of the fixed schedule
				+ " EXISTS (SELECT *"
				+ " FROM ART_JOB_SCHEDULES AJS"
				+ " INNER JOIN ART_SCHEDULE_HOLIDAY_MAP ASHM"
				+ " ON AJS.SCHEDULE_ID=ASHM.SCHEDULE_ID"
				+ " WHERE AJS.SCHEDULE_ID=AJ.SCHEDULE_ID AND ASHM.HOLIDAY_ID=?)";

		ResultSetHandler<List<Job>> h = new BeanListHandler<>(Job.class, new JobMapper());
		return dbService.query(sql, h, holidayId, holidayId);
	}

	/**
	 * Processes schedule fields and creates quartz schedules for the job
	 *
	 * @param job the art job object
	 * @param actionUser the user who initiated the action
	 * @throws java.text.ParseException
	 * @throws org.quartz.SchedulerException
	 * @throws java.sql.SQLException
	 */
	public void processSchedules(Job job, User actionUser)
			throws ParseException, SchedulerException, SQLException {

		//http://www.quartz-scheduler.org/documentation/quartz-2.x/tutorials/tutorial-lesson-04.html
		Scheduler scheduler = SchedulerUtils.getScheduler();
		if (scheduler == null) {
			logger.warn("Scheduler not available. Job Id {}", job.getJobId());
			return;
		}

		//delete job while it has old calendar names, before updating the calendar names field
		deleteQuartzJob(job, scheduler);

		//job must have been saved in order to use job id for job, trigger and calendar names
		int jobId = job.getJobId();

		String timeZoneId;
		Schedule schedule = job.getSchedule();
		if (schedule == null) {
			timeZoneId = job.getScheduleTimeZone();
		} else {
			timeZoneId = schedule.getTimeZone();
		}

		TimeZone timeZone;
		if (StringUtils.isBlank(timeZoneId)) {
			timeZone = TimeZone.getDefault();
		} else {
			timeZone = TimeZone.getTimeZone(timeZoneId);
		}

		logger.debug("timeZoneId='{}'. Job Id {}", timeZoneId, job.getJobId());
		logger.debug("timeZone={}. Job Id {}", timeZone, job.getJobId());

		//get applicable holidays
		List<org.quartz.Calendar> calendars = processHolidays(job, timeZone);
		String globalCalendarName = "calendar" + jobId;
		org.quartz.Calendar globalCalendar = null;
		List<String> calendarNames = new ArrayList<>();
		for (org.quartz.Calendar calendar : calendars) {
			String calendarName = calendar.getDescription();
			if (StringUtils.isBlank(calendarName)) {
				globalCalendar = calendar;
				globalCalendar.setDescription(globalCalendarName);
				calendarName = globalCalendarName;
			}
			calendarNames.add(calendarName);

			boolean replace = true;
			boolean updateTriggers = true;
			scheduler.addCalendar(calendarName, calendar, replace, updateTriggers);
		}

		Set<Trigger> triggers = processTriggers(job, globalCalendar, timeZone);

		//get earliest next run date from all available triggers
		Date nextRunDate = JobUtils.getNextFireTime(triggers, scheduler);
		job.setNextRunDate(nextRunDate);

		String quartzCalendarNames = StringUtils.join(calendarNames, ",");
		job.setQuartzCalendarNames(quartzCalendarNames);

		//update next run date and calendar names fields
		updateJob(job, actionUser);

		String jobName = "job" + jobId;

		JobDetail quartzJob = JobBuilder.newJob(ReportJob.class)
				.withIdentity(jobName, ArtUtils.JOB_GROUP)
				.usingJobData("jobId", jobId)
				.build();

		//add job and triggers to scheduler
		boolean replace = true;
		scheduler.scheduleJob(quartzJob, triggers, replace);
	}

	/**
	 * Processes schedule definitions in the main fields and extra section
	 *
	 * @param job the art job
	 * @param globalCalendar the global calendar to apply to triggers
	 * @param timeZone the time zone to use for the triggers
	 * @return the list of triggers to use for the job
	 * @throws ParseException
	 */
	private Set<Trigger> processTriggers(Job job, org.quartz.Calendar globalCalendar,
			TimeZone timeZone) throws ParseException {

		Set<Trigger> triggers = new HashSet<>();

		int jobId = job.getJobId();
		String mainTriggerName = "trigger" + jobId;

		String cronString;

		Schedule schedule = job.getSchedule();
		if (schedule == null) {
			cronString = CronStringHelper.getCronString(job);
		} else {
			cronString = CronStringHelper.getCronString(schedule);
		}

		//if start date is in the past, job will fire once immediately, for the missed fire time in the past
		Date now = new Date();
		if (job.getStartDate().before(now)) {
			job.setStartDate(now);
		}

		//create trigger that defines the main schedule for the job
		CronTriggerImpl mainTrigger = new CronTriggerImpl();
		mainTrigger.setKey(triggerKey(mainTriggerName, ArtUtils.TRIGGER_GROUP));
		mainTrigger.setCronExpression(cronString);
		mainTrigger.setStartTime(job.getStartDate());
		mainTrigger.setEndTime(job.getEndDate());
		mainTrigger.setTimeZone(timeZone);

		if (globalCalendar != null) {
			mainTrigger.setCalendarName(globalCalendar.getDescription());
		}

		triggers.add(mainTrigger);

		//create triggers for extra schedules
		String extraSchedules;
		if (schedule == null) {
			extraSchedules = job.getExtraSchedules();
		} else {
			extraSchedules = schedule.getExtraSchedules();
		}
		if (StringUtils.isNotBlank(extraSchedules)) {
			if (StringUtils.startsWith(extraSchedules, ExpressionHelper.GROOVY_START_STRING)) {
				ExpressionHelper expressionHelper = new ExpressionHelper();
				Object result = expressionHelper.runGroovyExpression(extraSchedules);
				if (result instanceof List) {
					@SuppressWarnings("unchecked")
					List<AbstractTrigger<Trigger>> extraTriggers = (List<AbstractTrigger<Trigger>>) result;
					for (AbstractTrigger<Trigger> extraTrigger : extraTriggers) {
						finalizeTriggerProperties(extraTrigger, globalCalendar, job);
					}
					triggers.addAll(extraTriggers);
				} else {
					if (result instanceof AbstractTrigger) {
						@SuppressWarnings("unchecked")
						AbstractTrigger<Trigger> extraTrigger = (AbstractTrigger<Trigger>) result;
						finalizeTriggerProperties(extraTrigger, globalCalendar, job);
						triggers.add(extraTrigger);
					}
				}
			} else {
				String values[] = extraSchedules.split("\\r?\\n");
				int index = 1;
				for (String value : values) {
					index++;
					String extraTriggerName = mainTriggerName + "-" + index;

					CronTriggerImpl extraTrigger = new CronTriggerImpl();
					extraTrigger.setKey(triggerKey(extraTriggerName, ArtUtils.TRIGGER_GROUP));
					String finalCronString = CronStringHelper.processDynamicTime(value);
					extraTrigger.setCronExpression(finalCronString);
					extraTrigger.setStartTime(job.getStartDate());
					extraTrigger.setEndTime(job.getEndDate());
					extraTrigger.setTimeZone(timeZone);

					if (globalCalendar != null) {
						extraTrigger.setCalendarName(globalCalendar.getDescription());
					}

					triggers.add(extraTrigger);
				}
			}
		}

		return triggers;
	}

	/**
	 * Sets properties for a trigger where they are not explicitly defined e.g.
	 * calendar name, start date and end date
	 *
	 * @param trigger the trigger to set
	 * @param globalCalendar the global calendar in use
	 * @param job the art job
	 */
	private void finalizeTriggerProperties(AbstractTrigger<Trigger> trigger,
			org.quartz.Calendar globalCalendar, Job job) {

		if (StringUtils.isBlank(trigger.getCalendarName()) && globalCalendar != null) {
			trigger.setCalendarName(globalCalendar.getDescription());
		}
		if (trigger.getStartTime() == null) {
			trigger.setStartTime(job.getStartDate());
		}
		if (trigger.getEndTime() == null) {
			trigger.setEndTime(job.getEndDate());
		}
	}

	/**
	 * Process holiday definitions
	 *
	 * @param job the art job
	 * @param timeZone the time zone to use
	 * @return the list of calendars representing configured holidays
	 * @throws ParseException
	 */
	private List<org.quartz.Calendar> processHolidays(Job job, TimeZone timeZone)
			throws ParseException {

		List<org.quartz.Calendar> calendars = new ArrayList<>();

		String holidays;
		Schedule schedule = job.getSchedule();
		if (schedule == null) {
			holidays = job.getHolidays();
		} else {
			holidays = schedule.getHolidays();
		}

		List<org.quartz.Calendar> mainCalendars = processHolidayString(holidays, timeZone);

		List<org.quartz.Calendar> nonLabelledCalendars = new ArrayList<>();
		for (org.quartz.Calendar calendar : mainCalendars) {
			if (StringUtils.isBlank(calendar.getDescription())) {
				nonLabelledCalendars.add(calendar);
			} else {
				calendars.add(calendar);
			}
		}

		List<Holiday> sharedHolidays;
		if (schedule == null) {
			sharedHolidays = job.getSharedHolidays();
		} else {
			sharedHolidays = schedule.getSharedHolidays();
		}

		if (CollectionUtils.isNotEmpty(sharedHolidays)) {
			for (Holiday holiday : sharedHolidays) {
				List<org.quartz.Calendar> sharedCalendars = processHolidayString(holiday.getDefinition(), timeZone);
				for (org.quartz.Calendar calendar : sharedCalendars) {
					if (StringUtils.isBlank(calendar.getDescription())) {
						nonLabelledCalendars.add(calendar);
					} else {
						calendars.add(calendar);
					}
				}
			}
		}

		if (CollectionUtils.isNotEmpty(nonLabelledCalendars)) {
			org.quartz.Calendar finalNonLabelledCalendar = concatenateCalendars(nonLabelledCalendars);
			calendars.add(finalNonLabelledCalendar);
		}

		return calendars;
	}

	/**
	 * Processes a string containing holiday definitions
	 *
	 * @param holidays the string containing holiday definitions
	 * @param timeZone the time zone to use
	 * @return a list of calendars representing the holiday definitions
	 * @throws ParseException
	 */
	private List<org.quartz.Calendar> processHolidayString(String holidays,
			TimeZone timeZone) throws ParseException {

		List<org.quartz.Calendar> calendars = new ArrayList<>();

		if (StringUtils.isNotBlank(holidays)) {
			if (StringUtils.startsWith(holidays, ExpressionHelper.GROOVY_START_STRING)) {
				ExpressionHelper expressionHelper = new ExpressionHelper();
				Object result = expressionHelper.runGroovyExpression(holidays);
				if (result instanceof List) {
					@SuppressWarnings("unchecked")
					List<org.quartz.Calendar> groovyCalendars = (List<org.quartz.Calendar>) result;
					List<org.quartz.Calendar> nonLabelledGroovyCalendars = new ArrayList<>();
					for (org.quartz.Calendar calendar : groovyCalendars) {
						if (StringUtils.isBlank(calendar.getDescription())) {
							nonLabelledGroovyCalendars.add(calendar);
						} else {
							calendars.add(calendar);
						}
					}
					if (CollectionUtils.isNotEmpty(nonLabelledGroovyCalendars)) {
						org.quartz.Calendar finalCalendar = concatenateCalendars(nonLabelledGroovyCalendars);
						calendars.add(finalCalendar);
					}
				} else {
					org.quartz.Calendar calendar = (org.quartz.Calendar) result;
					calendars.add(calendar);
				}
			} else {
				String values[] = holidays.split("\\r?\\n");
				List<org.quartz.Calendar> cronCalendars = new ArrayList<>();
				for (String value : values) {
					CronCalendar calendar = new CronCalendar(value);
					calendar.setTimeZone(timeZone);
					cronCalendars.add(calendar);
				}
				if (CollectionUtils.isNotEmpty(cronCalendars)) {
					org.quartz.Calendar finalCalendar = concatenateCalendars(cronCalendars);
					calendars.add(finalCalendar);
				}
			}
		}

		return calendars;
	}

	/**
	 * Concatenate calendars to get one calendar that includes all the dates in
	 * the given calendars
	 *
	 * @param calendars the list of calendars to concatenate
	 * @return a calendar that includes all the dates in the given calendars
	 */
	private org.quartz.Calendar concatenateCalendars(List<org.quartz.Calendar> calendars) {
		//https://stackoverflow.com/questions/5863435/quartz-net-multple-calendars
		if (CollectionUtils.isEmpty(calendars)) {
			return null;
		}

		//concatenate calendars. you can only specify one calendar for a trigger
		for (int i = 0; i < calendars.size(); i++) {
			if (i > 0) {
				org.quartz.Calendar currentCalendar = calendars.get(i);
				org.quartz.Calendar previousCalendar = calendars.get(i - 1);
				currentCalendar.setBaseCalendar(previousCalendar);
			}
		}
		org.quartz.Calendar finalCalendar = calendars.get(calendars.size() - 1);

		return finalCalendar;
	}

	/**
	 * Returns jobs that use a given schedule
	 *
	 * @param scheduleId the schedule id
	 * @return jobs that use the schedule
	 * @throws SQLException
	 */
	public List<Job> getJobsWithSchedule(int scheduleId) throws SQLException {
		logger.debug("Entering getJobsWithSchedule: scheduleId={}", scheduleId);

		String sql = SQL_SELECT_ALL
				+ " WHERE SCHEDULE_ID=?";

		ResultSetHandler<List<Job>> h = new BeanListHandler<>(Job.class, new JobMapper());
		return dbService.query(sql, h, scheduleId);
	}

	/**
	 * Returns jobs that use a given smtp server
	 *
	 * @param smtpServerId the smtp server id
	 * @return jobs that use the smtp server
	 * @throws SQLException
	 */
	public List<Job> getJobsWithSmtpServer(int smtpServerId) throws SQLException {
		logger.debug("Entering getJobsWithSmtpServer: smtpServerId={}", smtpServerId);

		String sql = SQL_SELECT_ALL
				+ " WHERE SMTP_SERVER_ID=?";

		ResultSetHandler<List<Job>> h = new BeanListHandler<>(Job.class, new JobMapper());
		return dbService.query(sql, h, smtpServerId);
	}

	/**
	 * Returns jobs that use a given destination
	 *
	 * @param destinationId the destination id
	 * @return jobs that use the destination
	 * @throws SQLException
	 */
	public List<Job> getJobsWithDestination(int destinationId) throws SQLException {
		logger.debug("Entering getJobsWithDestination: destinationId={}", destinationId);

		String sql = SQL_SELECT_ALL
				+ " INNER JOIN ART_JOB_DESTINATION_MAP AJDM"
				+ " ON AJ.JOB_ID=AJDM.JOB_ID"
				+ " WHERE AJDM.DESTINATION_ID=?";

		ResultSetHandler<List<Job>> h = new BeanListHandler<>(Job.class, new JobMapper());
		return dbService.query(sql, h, destinationId);
	}

	/**
	 * Returns jobs that use a given holiday
	 *
	 * @param holidayId the holiday id
	 * @return jobs that use the holiday
	 * @throws SQLException
	 */
	public List<Job> getJobsWithHoliday(int holidayId) throws SQLException {
		logger.debug("Entering getJobsWithHoliday: holidayId={}", holidayId);

		String sql = SQL_SELECT_ALL
				+ " INNER JOIN ART_JOB_HOLIDAY_MAP AJHM"
				+ " ON AJ.JOB_ID=AJHM.JOB_ID"
				+ " WHERE AJHM.HOLIDAY_ID=?";

		ResultSetHandler<List<Job>> h = new BeanListHandler<>(Job.class, new JobMapper());
		return dbService.query(sql, h, holidayId);
	}
}
