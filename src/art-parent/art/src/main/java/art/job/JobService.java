package art.job;

import art.connectionpool.DbConnections;
import art.dbutils.DbService;
import art.dbutils.DatabaseUtils;
import art.enums.JobType;
import art.report.Report;
import art.report.ReportService;
import art.user.User;
import art.user.UserService;
import art.utils.CachedResult;
import art.utils.SchedulerUtils;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.lang3.StringUtils;
import static org.quartz.JobKey.jobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Class to provide methods related to jobs
 *
 * @author Timothy
 */
@Service
public class JobService {

	private static final Logger logger = LoggerFactory.getLogger(JobService.class);

	private final DbService dbService;
	private final ReportService reportService;
	private final UserService userService;

	@Autowired
	public JobService(DbService dbService, ReportService reportService, UserService userService) {
		this.dbService = dbService;
		this.reportService = reportService;
		this.userService = userService;
	}

	public JobService() {
		dbService = new DbService();
		reportService = new ReportService();
		userService = new UserService();
	}

	private final String SQL_SELECT_ALL = "SELECT AJ.* FROM ART_JOBS AJ";

	/**
	 * Class to map resultset to an object
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

			job.setJobId(rs.getInt("JOB_ID"));
			job.setName(rs.getString("JOB_NAME"));
			job.setOutputFormat(rs.getString("OUTPUT_FORMAT"));
			job.setJobType(JobType.toEnum(rs.getString("JOB_TYPE")));
			job.setScheduleMinute(rs.getString("JOB_MINUTE"));
			job.setScheduleHour(rs.getString("JOB_HOUR"));
			job.setScheduleDay(rs.getString("JOB_DAY"));
			job.setScheduleWeekday(rs.getString("JOB_WEEKDAY"));
			job.setScheduleMonth(rs.getString("JOB_MONTH"));
			job.setMailTo(rs.getString("MAIL_TOS"));
			job.setMailFrom(rs.getString("MAIL_FROM"));
			job.setMailCc(rs.getString("MAIL_CC"));
			job.setMailBcc(rs.getString("MAIL_BCC"));
			job.setMailSubject(rs.getString("SUBJECT"));
			job.setMailMessage(rs.getString("MESSAGE"));
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
			job.setRecipientsQueryId(rs.getInt("RECIPIENTS_QUERY_ID"));
			job.setRunsToArchive(rs.getInt("RUNS_TO_ARCHIVE"));
			job.setCreationDate(rs.getTimestamp("CREATION_DATE"));
			job.setUpdateDate(rs.getTimestamp("UPDATE_DATE"));
			job.setCreatedBy(rs.getString("CREATED_BY"));
			job.setUpdatedBy(rs.getString("UPDATED_BY"));

			Report report = reportService.getReport(rs.getInt("QUERY_ID"));
			job.setReport(report);

			User user = userService.getUser(rs.getInt("USER_ID"));
			job.setUser(user);

			return type.cast(job);
		}
	}

	/**
	 * Get all jobs
	 *
	 * @return list of all jobs, empty list otherwise
	 * @throws SQLException
	 */
	@Cacheable("jobs")
	public List<Job> getAllJobs() throws SQLException {
		logger.debug("Entering getAllJobs");

		ResultSetHandler<List<Job>> h = new BeanListHandler<>(Job.class, new JobMapper());
		return dbService.query(SQL_SELECT_ALL, h);
	}

	/**
	 * Get a job
	 *
	 * @param id
	 * @return populated object if found, null otherwise
	 * @throws SQLException
	 */
	@Cacheable("jobs")
	public Job getJob(int id) throws SQLException {
		logger.debug("Entering getJob: id={}", id);

		return getFreshJob(id);
	}

	/**
	 * Get a job. Data always retrieved from the database and not the cache
	 *
	 * @param id
	 * @return populated object if found, null otherwise
	 * @throws SQLException
	 */
	public Job getFreshJob(int id) throws SQLException {
		logger.debug("Entering getFreshJob: id={}", id);

		String sql = SQL_SELECT_ALL + " WHERE JOB_ID = ? ";
		ResultSetHandler<Job> h = new BeanHandler<>(Job.class, new JobMapper());
		return dbService.query(sql, h, id);
	}

	/**
	 * Delete a job
	 *
	 * @param id
	 * @throws SQLException
	 * @throws org.quartz.SchedulerException
	 */
	@CacheEvict(value = "jobs", allEntries = true)
	public void deleteJob(int id) throws SQLException, SchedulerException {
		logger.debug("Entering deleteJob: id={}", id);

		//get job object. need job details in order to delete cached table for cached result jobs
		Job job = getJob(id);
		if (job == null) {
			logger.warn("Cannot delete job: {}. Job not available.", id);
			return;
		}

		//delete records in quartz tables
		Scheduler scheduler = SchedulerUtils.getScheduler();
		if (scheduler == null) {
			logger.warn("Cannot delete job: {}. Scheduler not available.", id);
			return;
		}
		scheduler.deleteJob(jobKey(String.valueOf(id)));

		// Delete the Cached table if this job is a cache result one
		JobType jobType = job.getJobType();
		if (jobType == JobType.CacheAppend || jobType == JobType.CacheInsert) {
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

		sql = "DELETE FROM ART_USER_JOBS WHERE JOB_ID=?";
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
	 * Delete a job
	 *
	 * @param ids
	 * @throws SQLException
	 * @throws org.quartz.SchedulerException
	 */
	@CacheEvict(value = "jobs", allEntries = true)
	public void deleteJobs(Integer[] ids) throws SQLException, SchedulerException {
		logger.debug("Entering deleteJobs: ids={}", (Object)ids);
		
		for(Integer id : ids){
			deleteJob(id);
		}
	}

	/**
	 * Add a new job to the database
	 *
	 * @param job
	 * @return new record id
	 * @throws SQLException
	 */
	@CacheEvict(value = "jobs", allEntries = true)
	public synchronized int addJob(Job job, User actionUser) throws SQLException {
		logger.debug("Entering addJob: job={}, actionUser={}", job, actionUser);

		//generate new id
		String sql = "SELECT MAX(JOB_ID) FROM ART_JOBS";
		ResultSetHandler<Integer> h = new ScalarHandler<>();
		Integer maxId = dbService.query(sql, h);
		logger.debug("maxId={}", maxId);

		int newId;
		if (maxId == null || maxId < 0) {
			//no records in the table, or only hardcoded records
			newId = 1;
		} else {
			newId = maxId + 1;
		}
		logger.debug("newId={}", newId);

		job.setJobId(newId);
		boolean newRecord = true;

		saveJob(job, newRecord, actionUser);

		return newId;
	}

	/**
	 * Update an existing job
	 *
	 * @param job
	 * @throws SQLException
	 */
	@CacheEvict(value = "jobs", allEntries = true)
	public void updateJob(Job job, User actionUser) throws SQLException {
		logger.debug("Entering updateJob: job={}, actionUser={}", job, actionUser);

		boolean newRecord = false;

		saveJob(job, newRecord, actionUser);
	}

	/**
	 * Get all the jobs a user has access to. Both the jobs the user owns and
	 * jobs shared with him
	 *
	 * @param userId
	 * @return all the jobs a user has access to
	 * @throws java.sql.SQLException
	 */
	@Cacheable("jobs")
	public List<Job> getJobs(int userId) throws SQLException {
		logger.debug("Entering getJobs: userId={}", userId);

		List<Job> jobs = new ArrayList<>();

		jobs.addAll(getOwnedJobs(userId));
		jobs.addAll(getSharedJobs(userId));

		return jobs;
	}

	/**
	 * Get the jobs a user owns
	 *
	 * @param userId
	 * @return list of jobs that the user owns, empty list if none
	 * @throws java.sql.SQLException
	 */
	public List<Job> getOwnedJobs(int userId) throws SQLException {
		logger.debug("Entering getOwnedJobs: userId={}", userId);

		String sql = SQL_SELECT_ALL + " WHERE AJ.USER_ID=?";
		ResultSetHandler<List<Job>> h = new BeanListHandler<>(Job.class, new JobMapper());
		return dbService.query(sql, h, userId);
	}

	/**
	 * Get the shared jobs a user has access to
	 *
	 * @param userId
	 * @return the shared jobs a user has access to
	 * @throws java.sql.SQLException
	 */
	public List<SharedJob> getSharedJobs(int userId) throws SQLException {
		logger.debug("Entering getSharedJobs: userId={}", userId);

		List<SharedJob> jobs = new ArrayList<>();
		String sql;

		//get shared jobs user has access to via job membership. 
		//non-split jobs. no entries for them in the art_user_jobs table
		sql = SQL_SELECT_ALL + " INNER JOIN ART_USER_GROUP_JOBS AUGJ"
				+ " ON AJ.JOB_ID=AUGJ.JOB_ID"
				+ " WHERE AJ.USER_ID <> ? AND EXISTS "
				+ " (SELECT * FROM ART_USER_GROUP_ASSIGNMENT AUGA WHERE AUGA.USER_ID = ? "
				+ " AND AUGA.USER_GROUP_ID=AUGJ.USER_GROUP_ID)";
		ResultSetHandler<List<SharedJob>> h = new BeanListHandler<>(SharedJob.class, new JobMapper());
		jobs.addAll(dbService.query(sql, h, userId, userId));

		//get shared jobs user has direct access to, but doesn't own. both split and non-split jobs
		//stored in the art_user_jobs table
		sql = SQL_SELECT_ALL + " INNER JOIN ART_USER_JOBS AUJ"
				+ " ON AJ.JOB_ID=AUJ.JOB_ID"
				+ " WHERE AJ.USER_ID<>? AND AUJ.USER_ID=?";
		jobs.addAll(dbService.query(sql, h, userId, userId));

		return jobs;
	}

	/**
	 * Get jobs that have not been migrated to the quartz scheduling system
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

	private void saveJob(Job job, boolean newRecord, User actionUser) throws SQLException {
		logger.debug("Entering saveJob: job={}, newRecord={}, actionUser={}", job, newRecord, actionUser);

		Integer reportId; //database column doesn't allow null
		if (job.getReport() == null) {
			logger.warn("Report not defined. Defaulting to 0");
			reportId = 0;
		} else {
			reportId = job.getReport().getReportId();
		}

		Integer userId; //database column doesn't allow null
		String username;
		if (job.getUser() == null) {
			logger.warn("User not defined. Defaulting to 0");
			userId = 0;
			username = "";
		} else {
			userId = job.getUser().getUserId();
			username = job.getUser().getUsername();
		}

		String migratedToQuartz = "X";

		int affectedRows;
		if (newRecord) {
			String sql = "INSERT INTO ART_JOBS"
					+ " (JOB_ID, JOB_NAME, QUERY_ID, USER_ID, USERNAME,"
					+ " OUTPUT_FORMAT, JOB_TYPE, JOB_MINUTE, JOB_HOUR, JOB_DAY,"
					+ " JOB_WEEKDAY, JOB_MONTH, MAIL_TOS, MAIL_FROM, MAIL_CC,"
					+ " MAIL_BCC, SUBJECT, MESSAGE, CACHED_TABLE_NAME,"
					+ " START_DATE, END_DATE, NEXT_RUN_DATE,"
					+ " ACTIVE, ENABLE_AUDIT, ALLOW_SHARING, ALLOW_SPLITTING,"
					+ " RECIPIENTS_QUERY_ID, RUNS_TO_ARCHIVE, MIGRATED_TO_QUARTZ,"
					+ " CREATION_DATE, CREATED_BY)"
					+ " VALUES(" + StringUtils.repeat("?", ",", 31) + ")";

			Object[] values = {
				job.getJobId(),
				job.getName(),
				reportId,
				userId,
				username,
				job.getOutputFormat(),
				job.getJobType().getValue(),
				job.getScheduleMinute(),
				job.getScheduleHour(),
				job.getScheduleDay(),
				job.getScheduleWeekday(),
				job.getScheduleMonth(),
				job.getMailTo(),
				job.getMailFrom(),
				job.getMailCc(),
				job.getMailBcc(),
				job.getMailSubject(),
				job.getMailMessage(),
				job.getCachedTableName(),
				DatabaseUtils.toSqlTimestamp(job.getStartDate()),
				DatabaseUtils.toSqlTimestamp(job.getEndDate()),
				DatabaseUtils.toSqlTimestamp(job.getNextRunDate()),
				job.isActive(),
				job.isEnableAudit(),
				job.isAllowSharing(),
				job.isAllowSplitting(),
				job.getRecipientsQueryId(),
				job.getRunsToArchive(),
				migratedToQuartz,
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				actionUser.getUsername()
			};

			affectedRows = dbService.update(sql, values);
		} else {
			String sql = "UPDATE ART_JOBS SET JOB_NAME=?, QUERY_ID=?,"
					+ " USER_ID=?, USERNAME=?, OUTPUT_FORMAT=?, JOB_TYPE=?,"
					+ " JOB_MINUTE=?, JOB_HOUR=?, JOB_DAY=?, JOB_WEEKDAY=?,"
					+ " JOB_MONTH=?, MAIL_TOS=?, MAIL_FROM=?, MAIL_CC=?, MAIL_BCC=?,"
					+ " SUBJECT=?, MESSAGE=?, CACHED_TABLE_NAME=?, START_DATE=?,"
					+ " END_DATE=?, NEXT_RUN_DATE=?,"
					+ " ACTIVE=?, ENABLE_AUDIT=?,"
					+ " ALLOW_SHARING=?, ALLOW_SPLITTING=?, RECIPIENTS_QUERY_ID=?,"
					+ " RUNS_TO_ARCHIVE=?, MIGRATED_TO_QUARTZ=?, "
					+ " UPDATE_DATE=?, UPDATED_BY=?"
					+ " WHERE JOB_ID=?";

			Object[] values = {
				job.getName(),
				reportId,
				userId,
				username,
				job.getOutputFormat(),
				job.getJobType().getValue(),
				job.getScheduleMinute(),
				job.getScheduleHour(),
				job.getScheduleDay(),
				job.getScheduleWeekday(),
				job.getScheduleMonth(),
				job.getMailTo(),
				job.getMailFrom(),
				job.getMailCc(),
				job.getMailBcc(),
				job.getMailSubject(),
				job.getMailMessage(),
				job.getCachedTableName(),
				DatabaseUtils.toSqlTimestamp(job.getStartDate()),
				DatabaseUtils.toSqlTimestamp(job.getEndDate()),
				DatabaseUtils.toSqlTimestamp(job.getNextRunDate()),
				job.isActive(),
				job.isEnableAudit(),
				job.isAllowSharing(),
				job.isAllowSplitting(),
				job.getRecipientsQueryId(),
				job.getRunsToArchive(),
				migratedToQuartz,
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				actionUser.getUsername(),
				job.getJobId()
			};

			affectedRows = dbService.update(sql, values);
		}

		logger.debug("affectedRows={}", affectedRows);

		if (affectedRows != 1) {
			logger.warn("Problem with save. affectedRows={}, newRecord={}, job={}",
					affectedRows, newRecord, job);
		}
	}

}
