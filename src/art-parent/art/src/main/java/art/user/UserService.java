package art.user;

import art.dbutils.DbService;
import art.enums.AccessLevel;
import art.dbutils.DbUtils;
import art.report.Report;
import art.usergroup.UserGroup;
import art.usergroup.UserGroupService;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Class to provide methods related to users
 *
 * @author Timothy Anyona
 */
@Service
public class UserService {

	//for caching info, see
	//http://wangxiangblog.blogspot.com/2013/02/spring-cache.html
	//http://viralpatel.net/blogs/cache-support-spring-3-1-m1/
	private static final Logger logger = LoggerFactory.getLogger(UserService.class);

	@Autowired
	private DbService dbService;

	@Autowired
	private UserGroupService userGroupService;

	final String SQL_SELECT_ALL = "SELECT * FROM ART_USERS ";

	/**
	 * Class to map resultset to an object
	 */
	private class UserMapper extends BasicRowProcessor {

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
			User user = new User();

			user.setUsername(rs.getString("USERNAME"));
			user.setEmail(rs.getString("EMAIL"));
			user.setAccessLevel(AccessLevel.toEnum(rs.getInt("ACCESS_LEVEL")));
			user.setFullName(rs.getString("FULL_NAME"));
			user.setActive(rs.getBoolean("ACTIVE"));
			user.setPassword(rs.getString("PASSWORD"));
			user.setDefaultReportGroup(rs.getInt("DEFAULT_QUERY_GROUP"));
			user.setPasswordAlgorithm(rs.getString("PASSWORD_ALGORITHM"));
			user.setStartReport(rs.getString("START_QUERY"));
			user.setUserId(rs.getInt("USER_ID"));
			user.setCanChangePassword(rs.getBoolean("CAN_CHANGE_PASSWORD"));
			user.setCreationDate(rs.getTimestamp("CREATION_DATE"));
			user.setUpdateDate(rs.getTimestamp("UPDATE_DATE"));

			return type.cast(user);
		}
	}

	/**
	 * Get all users
	 *
	 * @return list of all users, empty list otherwise
	 * @throws SQLException
	 */
	@Cacheable("users")
	public List<User> getAllUsers() throws SQLException {
		logger.debug("Entering getAllUsers");

		ResultSetHandler<List<User>> h = new BeanListHandler<>(User.class, new UserMapper());
		return dbService.query(SQL_SELECT_ALL, h);
	}

	/**
	 * Get admin users (junior admin and above)
	 *
	 * @return list of admin users, empty list otherwise
	 * @throws SQLException
	 */
	@Cacheable("users")
	public List<User> getAdminUsers() throws SQLException {
		logger.debug("Entering getAdminUsers");

		String sql = SQL_SELECT_ALL + "WHERE ACCESS_LEVEL>=?";
		ResultSetHandler<List<User>> h = new BeanListHandler<>(User.class, new UserMapper());
		return dbService.query(sql, h, AccessLevel.JuniorAdmin.getValue());
	}

	/**
	 * Get a user
	 *
	 * @param id
	 * @return populated object if found, null otherwise
	 * @throws SQLException
	 */
	@Cacheable("users")
	public User getUser(int id) throws SQLException {
		logger.debug("Entering getUser: id={}", id);

		String sql = SQL_SELECT_ALL + " WHERE USER_ID = ? ";
		ResultSetHandler<User> h = new BeanHandler<>(User.class, new UserMapper());
		User user = dbService.query(sql, h, id);
		populateUserGroups(user);
		return user;
	}

	/**
	 * Get a user
	 *
	 * @param username
	 * @return populated object if found, null otherwise
	 * @throws SQLException
	 */
	@Cacheable("users")
	public User getUser(String username) throws SQLException {
		logger.debug("Entering getUser: username='{}'", username);

		String sql = SQL_SELECT_ALL + " WHERE USERNAME = ? ";
		ResultSetHandler<User> h = new BeanHandler<>(User.class, new UserMapper());
		User user = dbService.query(sql, h, username);
		populateUserGroups(user);
		return user;
	}

	/**
	 * Populate a user's user groups and set properties whose values may come
	 * from user groups
	 *
	 * @param user
	 */
	private void populateUserGroups(User user) throws SQLException {
		if (user == null) {
			return;
		}

		int effectiveDefaultReportGroup = user.getDefaultReportGroup();
		String effectiveStartReport = user.getStartReport();

		List<UserGroup> groups = userGroupService.getUserGroupsForUser(user.getUserId());

		for (UserGroup group : groups) {
			if (effectiveDefaultReportGroup <= 0) {
				effectiveDefaultReportGroup = group.getDefaultReportGroup();
			}
			if (StringUtils.isBlank(effectiveStartReport)) {
				effectiveStartReport = group.getStartReport();
			}
		}

		user.setEffectiveDefaultReportGroup(effectiveDefaultReportGroup);
		user.setEffectiveStartReport(effectiveStartReport);

		user.setUserGroups(groups);
	}

	/**
	 * Delete a user and all related records
	 *
	 * @param id
	 * @param linkedJobs output parameter. list that will be populated with
	 * linked jobs if they exist
	 * @return -1 if the record was not deleted because there are some linked
	 * records in other tables, otherwise the count of the number of users
	 * deleted
	 * @throws SQLException
	 */
	@CacheEvict(value = "users", allEntries = true)
	public int deleteUser(int id, List<String> linkedJobs) throws SQLException {
		logger.debug("Entering deleteUser: id={}", id);

		//don't delete if important linked records exist
		List<String> jobs = getLinkedJobs(id);
		if (!jobs.isEmpty()) {
			if (linkedJobs != null) {
				linkedJobs.addAll(jobs);
			}
			return -1;
		}

		String sql;

		//delete foreign key records
		sql = "DELETE FROM ART_ADMIN_PRIVILEGES WHERE USER_ID=?";
		dbService.update(sql, id);

		sql = "DELETE FROM ART_USER_QUERIES WHERE USER_ID=?";
		dbService.update(sql, id);

		//delete user-report user relationships
		sql = "DELETE FROM ART_USER_QUERY_GROUPS WHERE USER_ID=?";
		dbService.update(sql, id);

		//delete user-rules relationships
		sql = "DELETE FROM ART_USER_RULES WHERE USER_ID=?";
		dbService.update(sql, id);

		//delete user-shared job relationships
		sql = "DELETE FROM ART_USER_JOBS WHERE USER_ID=?";
		dbService.update(sql, id);

		sql = "DELETE FROM ART_USER_GROUP_ASSIGNMENT WHERE USER_ID=?";
		dbService.update(sql, id);

		sql = "DELETE FROM ART_JOB_ARCHIVES WHERE USER_ID=?";
		dbService.update(sql, id);

		//lastly, delete user
		sql = "DELETE FROM ART_USERS WHERE USER_ID=?";
		return dbService.update(sql, id);
	}

	/**
	 * Update a user's password
	 *
	 * @param userId
	 * @param newPassword password hash
	 * @param passwordAlgorithm
	 * @throws SQLException
	 */
	@CacheEvict(value = "users", allEntries = true)
	public void updatePassword(int userId, String newPassword, String passwordAlgorithm) throws SQLException {
		String sql = "UPDATE ART_USERS SET PASSWORD=?, UPDATE_DATE=?,"
				+ " PASSWORD_ALGORITHM=?"
				+ " WHERE USER_ID=?";

		Object[] values = {
			newPassword,
			DbUtils.getCurrentTimeStamp(),
			passwordAlgorithm,
			userId
		};

		dbService.update(sql, values);
	}

	/**
	 * Add a new user to the database
	 *
	 * @param user
	 * @return new record id
	 * @throws SQLException
	 */
	@CacheEvict(value = "users", allEntries = true)
	public synchronized int addUser(User user) throws SQLException {
		logger.debug("Entering addUser: user={}", user);

		//generate new id
		String sql = "SELECT MAX(USER_ID) FROM ART_USERS";
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

		sql = "INSERT INTO ART_USERS"
				+ " (USER_ID, USERNAME, PASSWORD, PASSWORD_ALGORITHM,"
				+ " FULL_NAME, EMAIL, ACCESS_LEVEL, DEFAULT_QUERY_GROUP,"
				+ " START_QUERY_GROUP, CAN_CHANGE_PASSWORD, ACTIVE, CREATION_DATE)"
				+ " VALUES(" + StringUtils.repeat("?", ",", 12) + ")";

		//set values for possibly null property objects
		Map<String, Object> defaults = getSaveDefaults(user);

		Object[] values = {
			newId,
			user.getUsername(),
			user.getPassword(),
			user.getPasswordAlgorithm(),
			user.getFullName(),
			user.getEmail(),
			defaults.get("accessLevel"),
			user.getDefaultReportGroup(),
			user.getStartReport(),
			user.isCanChangePassword(),
			user.isActive(),
			DbUtils.getCurrentTimeStamp()
		};

		dbService.update(sql, values);

		return newId;
	}

	/**
	 * Update an existing user record
	 *
	 * @param user
	 * @throws SQLException
	 */
	@CacheEvict(value = "users", allEntries = true)
	public void updateUser(User user) throws SQLException {
		logger.debug("Entering updateUser: user={}", user);

		String sql = "UPDATE ART_USERS SET USERNAME=?, PASSWORD=?,"
				+ " PASSWORD_ALGORITHM=?, FULL_NAME=?, EMAIL=?,"
				+ " ACCESS_LEVEL=?, DEFAULT_QUERY_GROUP=?, START_QUERY=?,"
				+ " CAN_CHANGE_PASSWORD=?, ACTIVE=?, UPDATE_DATE=?"
				+ " WHERE USER_ID=?";

		//set values for possibly null property objects
		Map<String, Object> defaults = getSaveDefaults(user);

		Object[] values = {
			user.getUsername(),
			user.getPassword(),
			user.getPasswordAlgorithm(),
			user.getFullName(),
			user.getEmail(),
			defaults.get("accessLevel"),
			user.getDefaultReportGroup(),
			user.getStartReport(),
			user.isCanChangePassword(),
			user.isActive(),
			DbUtils.getCurrentTimeStamp(),
			user.getUserId()
		};

		dbService.update(sql, values);
	}

	/**
	 * Get jobs that are owned by a given user
	 *
	 * @param userId
	 * @return list with linked job names, empty list otherwise
	 * @throws SQLException
	 */
	public List<String> getLinkedJobs(int userId) throws SQLException {
		logger.debug("Entering getLinkedJobs: userId={}", userId);

		String sql = "SELECT JOB_NAME"
				+ " FROM ART_JOBS"
				+ " WHERE USER_ID=?";

		ResultSetHandler<List<String>> h = new ColumnListHandler<>("JOB_NAME");
		return dbService.query(sql, h, userId);
	}
	
	/**
	 * Get values for possibly null property objects
	 *
	 * @param user
	 * @return map with values to save. key = field name, value = field value
	 */
	private Map<String, Object> getSaveDefaults(User user) {
		Map<String, Object> values = new HashMap<>();
		
		Integer accessLevel;
		if (user.getAccessLevel() == null) {
			logger.warn("Access level not defined. Defaulting to 0");
			accessLevel = 0;
		} else {
			accessLevel = user.getAccessLevel().getValue();
		}
		values.put("accessLevel", accessLevel);

		return values;
	}
}
