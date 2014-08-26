/*
 * Copyright (C) 2014 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * ART. If not, see <http://www.gnu.org/licenses/>.
 */
package art.datasource;

import art.dbutils.DbService;
import art.dbutils.DbUtils;
import art.enums.AccessLevel;
import art.user.User;
import art.utils.ActionResult;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
 * Class with methods for datasources
 *
 * @author Timothy Anyona
 */
@Service
public class DatasourceService {

	private static final Logger logger = LoggerFactory.getLogger(DatasourceService.class);

	@Autowired
	private DbService dbService;

	private final String SQL_SELECT_ALL = "SELECT * FROM ART_DATABASES";

	/**
	 * Class to map resultset to an object
	 */
	private class DatasourceMapper extends BasicRowProcessor {

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
			Datasource datasource = new Datasource();

			datasource.setDatasourceId(rs.getInt("DATABASE_ID"));
			datasource.setName(rs.getString("NAME"));
			datasource.setDescription(rs.getString("DESCRIPTION"));
			datasource.setJndi(rs.getBoolean("JNDI"));
			datasource.setDriver(rs.getString("DRIVER"));
			datasource.setUrl(rs.getString("URL"));
			datasource.setUsername(rs.getString("USERNAME"));
			datasource.setPassword(rs.getString("PASSWORD"));
			datasource.setConnectionPoolTimeout(rs.getInt("POOL_TIMEOUT"));
			datasource.setTestSql(rs.getString("TEST_SQL"));
			datasource.setActive(rs.getBoolean("ACTIVE"));
			datasource.setCreationDate(rs.getTimestamp("CREATION_DATE"));
			datasource.setUpdateDate(rs.getTimestamp("UPDATE_DATE"));
			datasource.setCreatedBy(rs.getString("CREATED_BY"));
			datasource.setUpdatedBy(rs.getString("UPDATED_BY"));

			return type.cast(datasource);
		}
	}

	/**
	 * Get all datasources
	 *
	 * @return list of all datasources, empty list otherwise
	 * @throws SQLException
	 */
	@Cacheable("datasources")
	public List<Datasource> getAllDatasources() throws SQLException {
		ResultSetHandler<List<Datasource>> h = new BeanListHandler<>(Datasource.class, new DatasourceMapper());
		return dbService.query(SQL_SELECT_ALL, h);
	}

	/**
	 * Get a datasource
	 *
	 * @param id
	 * @return populated object if datasource found, null otherwise
	 * @throws SQLException
	 */
	@Cacheable("datasources")
	public Datasource getDatasource(int id) throws SQLException {
		String sql = SQL_SELECT_ALL + " WHERE DATABASE_ID=?";
		ResultSetHandler<Datasource> h = new BeanHandler<>(Datasource.class, new DatasourceMapper());
		return dbService.query(sql, h, id);
	}

	/**
	 * Delete a datasource
	 *
	 * @param id
	 * @return ActionResult. if not successful, data contains a list of linked
	 * reports which prevented the datasource from being deleted
	 * @throws SQLException
	 */
	@CacheEvict(value = "datasources", allEntries = true)
	public ActionResult deleteDatasource(int id) throws SQLException {
		logger.debug("Entering deleteDatasource: id={}", id);

		ActionResult result = new ActionResult();

		//don't delete if important linked records exist
		List<String> linkedReports = getLinkedReports(id);
		if (!linkedReports.isEmpty()) {
			result.setData(linkedReports);
			return result;
		}

		String sql;

		sql = "DELETE FROM ART_DATABASES WHERE DATABASE_ID=?";
		dbService.update(sql, id);

		result.setSuccess(true);
		return result;
	}

	/**
	 * Add a new datasource to the database
	 *
	 * @param datasource
	 * @param actionUser
	 * @return new record id
	 * @throws SQLException
	 */
	@CacheEvict(value = "datasources", allEntries = true)
	public synchronized int addDatasource(Datasource datasource, User actionUser) throws SQLException {
		logger.debug("Entering addDatasource: datasource={}, actionUser={}", datasource, actionUser);

		//generate new id
		String sql = "SELECT MAX(DATABASE_ID) FROM ART_DATABASES";
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

		datasource.setDatasourceId(newId);

		saveDatasource(datasource, true, actionUser);

		return newId;

	}

	/**
	 * Update an existing datasource
	 *
	 * @param datasource
	 * @param actionUser
	 * @throws SQLException
	 */
	@CacheEvict(value = "datasources", allEntries = true)
	public void updateDatasource(Datasource datasource, User actionUser) throws SQLException {
		logger.debug("Entering updateDatasource: datasource={}, actionUser={}", datasource, actionUser);

		saveDatasource(datasource, false, actionUser);
	}

	/**
	 * Save a datasource
	 *
	 * @param datasource
	 * @param newRecord
	 * @param actionUser
	 * @throws SQLException
	 */
	private void saveDatasource(Datasource datasource, boolean newRecord, User actionUser) throws SQLException {
		logger.debug("Entering saveDatasource: datasource={}, newRecord={}, actionUser={}",
				datasource, newRecord, actionUser);

		int affectedRows;
		if (newRecord) {
			String sql = "INSERT INTO ART_DATABASES"
					+ " (DATABASE_ID, NAME, DESCRIPTION, JNDI, DRIVER, URL, USERNAME,"
					+ " PASSWORD, POOL_TIMEOUT, TEST_SQL, ACTIVE, CREATION_DATE, CREATED_BY)"
					+ " VALUES(" + StringUtils.repeat("?", ",", 13) + ")";

			Object[] values = {
				datasource.getDatasourceId(),
				datasource.getName(),
				datasource.getDescription(),
				datasource.isJndi(),
				datasource.getDriver(),
				datasource.getUrl(),
				datasource.getUsername(),
				datasource.getPassword(),
				datasource.getConnectionPoolTimeout(),
				datasource.getTestSql(),
				datasource.isActive(),
				DbUtils.getCurrentTimeAsSqlTimestamp(),
				actionUser.getUsername()
			};

			affectedRows = dbService.update(sql, values);
		} else {
			String sql = "UPDATE ART_DATABASES SET NAME=?, DESCRIPTION=?, JNDI=?,"
					+ " DRIVER=?, URL=?, USERNAME=?, PASSWORD=?,"
					+ " POOL_TIMEOUT=?, TEST_SQL=?, ACTIVE=?, UPDATE_DATE=?, UPDATED_BY=?"
					+ " WHERE DATABASE_ID=?";

			Object[] values = {
				datasource.getName(),
				datasource.getDescription(),
				datasource.isJndi(),
				datasource.getDriver(),
				datasource.getUrl(),
				datasource.getUsername(),
				datasource.getPassword(),
				datasource.getConnectionPoolTimeout(),
				datasource.getTestSql(),
				datasource.isActive(),
				DbUtils.getCurrentTimeAsSqlTimestamp(),
				actionUser.getUsername(),
				datasource.getDatasourceId()
			};

			affectedRows = dbService.update(sql, values);
		}

		logger.debug("affectedRows={}", affectedRows);

		if (affectedRows != 1) {
			logger.warn("Problem with save. affectedRows={}, newRecord={}, datasource={}",
					affectedRows, newRecord, datasource);
		}
	}

	/**
	 * Get reports that use a given datasource
	 *
	 * @param datasourceId
	 * @return list with linked report names, empty list otherwise
	 * @throws SQLException
	 */
	public List<String> getLinkedReports(int datasourceId) throws SQLException {
		logger.debug("Entering getLinkedReports: datasourceId={}", datasourceId);

		String sql = "SELECT NAME"
				+ " FROM ART_QUERIES"
				+ " WHERE DATABASE_ID=?";

		ResultSetHandler<List<String>> h = new ColumnListHandler<>("NAME");
		return dbService.query(sql, h, datasourceId);
	}

	/**
	 * Get datasources that an admin can use, according to his access level
	 *
	 * @param user
	 * @return list of available datasources, empty list otherwise
	 * @throws SQLException
	 */
	@Cacheable("datasources")
	public List<Datasource> getAdminDatasources(User user) throws SQLException {
		logger.debug("Entering getAdminDatasources: user={}", user);

		if (user == null || user.getAccessLevel() == null) {
			return Collections.emptyList();
		}

		logger.debug("user.getAccessLevel()={}", user.getAccessLevel());

		ResultSetHandler<List<Datasource>> h = new BeanListHandler<>(Datasource.class, new DatasourceMapper());
		if (user.getAccessLevel().getValue() >= AccessLevel.StandardAdmin.getValue()) {
			//standard admins and above can work with everything
			return dbService.query(SQL_SELECT_ALL, h);
		} else {
			String sql = "SELECT AD.*"
					+ " FROM ART_DATABASES AD, ART_ADMIN_PRIVILEGES AAP "
					+ " WHERE AD.DATABASE_ID = AAP.VALUE_ID "
					+ " AND AAP.PRIVILEGE = 'DB' "
					+ " AND AAP.USER_ID = ?";
			return dbService.query(sql, h, user.getUserId());
		}
	}

}
