/**
 * Copyright (C) 2013 Enrico Liboni <eliboni@users.sourceforge.net>
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
package art.report;

import art.datasource.Datasource;
import art.dbutils.DbService;
import art.servlets.ArtConfig;
import art.dbutils.DbUtils;
import art.enums.ReportStatus;
import art.enums.ReportType;
import art.reportgroup.ReportGroup;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Class to provide methods related to reports
 *
 * @author Timothy Anyona
 */
@Service
public class ReportService {

	private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

	@Autowired
	private DbService dbService;

	private final String SQL_SELECT_ALL = "SELECT AQ.*, "
			+ " AQG.NAME AS GROUP_NAME, AD.NAME AS DATASOURCE_NAME"
			+ " FROM ART_QUERIES AQ"
			+ " LEFT JOIN ART_QUERY_GROUPS AQG ON" //use left join so that all reports are returned
			+ " AQ.QUERY_GROUP_ID=AQG.QUERY_GROUP_ID"
			+ " LEFT JOIN ART_DATABASES AD ON"
			+ " AQ.DATABASE_ID=AD.DATABASE_ID";

	/**
	 * Class to map resultset to an object
	 */
	private class ReportMapper extends BasicRowProcessor {

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
			Report report = new Report();

			report.setReportId(rs.getInt("QUERY_ID"));
			report.setName(rs.getString("NAME"));
			report.setShortDescription(rs.getString("SHORT_DESCRIPTION"));
			report.setDescription(rs.getString("DESCRIPTION"));
			report.setReportType(rs.getInt("QUERY_TYPE"));

			ReportGroup reportGroup = new ReportGroup();
			reportGroup.setReportGroupId(rs.getInt("QUERY_GROUP_ID"));
			reportGroup.setName(rs.getString("GROUP_NAME"));
			report.setReportGroup(reportGroup);

			Datasource datasource = new Datasource();
			datasource.setDatasourceId(rs.getInt("DATABASE_ID"));
			datasource.setName(rs.getString("DATASOURCE_NAME"));
			report.setDatasource(datasource);

			report.setContactPerson(rs.getString("CONTACT_PERSON"));
			report.setUseRules(rs.getBoolean("USE_RULES"));
			report.setReportStatus(ReportStatus.toEnum(rs.getString("REPORT_STATUS")));
			report.setParametersInOutput(rs.getBoolean("PARAMETERS_IN_OUTPUT"));
			report.setxAxisLabel(rs.getString("X_AXIS_LABEL"));
			report.setyAxisLabel(rs.getString("Y_AXIS_LABEL"));
			report.setChartOptionsSetting(rs.getString("GRAPH_OPTIONS"));
			report.setTemplate(rs.getString("TEMPLATE"));
			report.setDisplayResultset(rs.getInt("DISPLAY_RESULTSET"));
			report.setXmlaUrl(rs.getString("XMLA_URL"));
			report.setXmlaDatasource(rs.getString("XMLA_DATASOURCE"));
			report.setXmlaCatalog(rs.getString("XMLA_CATALOG"));
			report.setXmlaUsername(rs.getString("XMLA_USERNAME"));
			report.setXmlaPassword(rs.getString("XMLA_PASSWORD"));

			report.setCreationDate(rs.getTimestamp("CREATION_DATE"));
			report.setUpdateDate(rs.getTimestamp("UPDATE_DATE"));

			setChartOptions(report);

			return type.cast(report);
		}

		/**
		 * Set display options for graphs
		 *
		 * @param optionsString
		 * @param usingShortDescription
		 */
		private void setChartOptions(Report report) {

			ChartOptions chartOptions = new ChartOptions();

			String optionsString;
			boolean usingShortDescription;
			if (report.getChartOptionsSetting() == null) {
				//to support legacy configurations where options were saved in short description field
				usingShortDescription = true;
				optionsString = report.getShortDescription();
			} else {
				usingShortDescription = false;
				optionsString = report.getChartOptionsSetting();
			}

			if (optionsString == null) {
				report.setChartOptions(new ChartOptions());
				return;
			}

			int index;
			index = optionsString.lastIndexOf("@");

			if (usingShortDescription || index > -1) {
				//set default for showlegend. false for heat maps. true for all other graphs
				ReportType reportType = ReportType.toEnum(report.getReportType());
				if (reportType == ReportType.Heatmap) {
					chartOptions.setShowLegend(false);
				} else {
					chartOptions.setShowLegend(true);
				}
				//set default for showlabels. true for pie charts. false for all other graphs
				if (reportType == ReportType.Pie2D || reportType == ReportType.Pie3D) {
					chartOptions.setShowLabels(true);
				} else {
					chartOptions.setShowLabels(false);
				}
			}

			String options;
			if (index > -1) {
				//options specified as part of short description. for backward compatibility with pre-2.0
				options = optionsString.substring(index + 1); //+1 so that the @ is not included in the options string
			} else {
				if (usingShortDescription) {
					//no @ symbol so graph options not specified in short description
					options = "";
				} else {
					options = optionsString;
				}
			}

			StringTokenizer st = new StringTokenizer(options.trim(), " ");

			String token;
			while (st.hasMoreTokens()) {
				token = st.nextToken();

				if (token.startsWith("rotate_at") || token.startsWith("rotateAt")) {
					String tmp = StringUtils.substringAfter(token, ":");
					chartOptions.setRotateAt(NumberUtils.toInt(tmp));
				} else if (token.startsWith("remove_at") || token.startsWith("removeAt")) {
					String tmp = StringUtils.substringAfter(token, ":");
					chartOptions.setRemoveAt(NumberUtils.toInt(tmp));
				} else if (token.startsWith("noleg")) {
					chartOptions.setShowLegend(false);
				} else if (StringUtils.startsWithIgnoreCase(token, "showLegend")) {
					chartOptions.setShowLegend(true);
				} else if (token.startsWith("nolab")) {
					chartOptions.setShowLabels(false);
				} else if (StringUtils.startsWithIgnoreCase(token, "showLabels")) {
					chartOptions.setShowLabels(true);
				} else if (StringUtils.startsWithIgnoreCase(token, "showPoints")) {
					chartOptions.setShowPoints(true);
				} else if (StringUtils.startsWithIgnoreCase(token, "showData")) {
					chartOptions.setShowData(true);
				} else if (token.indexOf("x") != -1) { //must come after named options e.g. rotate_at
					int idx = token.indexOf("x");
					String width = token.substring(0, idx);
					String height = token.substring(idx + 1);
					chartOptions.setWidth(NumberUtils.toInt(width));
					chartOptions.setHeight(NumberUtils.toInt(height));
				} else if (token.indexOf(":") != -1) { //must come after named options e.g. rotate_at
					int idx = token.indexOf(":");
					String yMin = token.substring(0, idx);
					String yMax = token.substring(idx + 1);
					chartOptions.setyAxisMin(NumberUtils.toDouble(yMin));
					chartOptions.setyAxisMax(NumberUtils.toDouble(yMax));
				} else if (token.startsWith("#")) {
					chartOptions.setBgColor(token);
				}
			}

			report.setChartOptions(chartOptions);
		}
	}

	/**
	 * Get the reports that a user can access from the reports page. Excludes
	 * disabled reports and some report types e.g. lovs
	 *
	 * @param username
	 * @return list of available reports, empty list otherwise
	 * @throws SQLException
	 */
	@Cacheable("reports")
	public List<AvailableReport> getAvailableReports(String username) throws SQLException {
		List<AvailableReport> reports = new ArrayList<>();

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = ArtConfig.getConnection();

			String sql;

			//union will return distinct results
			//only show active reports
			//don't show dynamic lov(119), static lov(120), dynamic job recipient(121) reports
			sql = "SELECT AQ.QUERY_ID, AQ.NAME AS QUERY_NAME, AQ.DESCRIPTION,"
					+ " AQ.UPDATE_DATE, AQ.QUERY_GROUP_ID, AQG.NAME AS GROUP_NAME,"
					+ " AQ.CREATION_DATE"
					+ " FROM ART_QUERIES AQ "
					+ " LEFT JOIN ART_QUERY_GROUPS AQG "
					+ " ON AQ.QUERY_GROUP_ID = AQG.QUERY_GROUP_ID "
					+ " WHERE AQ.REPORT_STATUS = 'Active' "
					+ " AND AQ.QUERY_TYPE<>119 AND AQ.QUERY_TYPE<>120 AND AQ.QUERY_TYPE<>121 "
					+ " AND AQ.QUERY_ID IN ("
					//get reports that user has explicit rights to see
					+ " SELECT AQ.QUERY_ID "
					+ " FROM ART_USER_QUERIES AUQ, ART_QUERIES AQ "
					+ " WHERE AUQ.QUERY_ID = AQ.QUERY_ID "
					+ " AND AUQ.USERNAME=? "
					+ " UNION ALL"
					//add reports to which the user has access through his report 
					+ " SELECT AQ.QUERY_ID "
					+ " FROM ART_USER_GROUP_QUERIES AUGQ, ART_QUERIES AQ "
					+ " WHERE AUGQ.QUERY_ID = AQ.QUERY_ID "
					+ " AND EXISTS "
					+ " (SELECT * FROM ART_USER_GROUP_ASSIGNMENT AUGA WHERE AUGA.USERNAME = ?"
					+ " AND AUGA.USER_GROUP_ID=AUGQ.USER_GROUP_ID)"
					+ " UNION ALL"
					// user can run all reports in the report reports he has direct access to
					+ " SELECT AQ.QUERY_ID "
					+ " FROM ART_USER_QUERY_GROUPS AUQG, ART_QUERIES AQ "
					+ " WHERE AUQG.QUERY_GROUP_ID = AQ.QUERY_GROUP_ID "
					+ " AND AUQG.USERNAME=? "
					+ " UNION ALL"
					//user can run all reports in the report reports that his reports have access to
					+ " SELECT AQ.QUERY_ID "
					+ " FROM ART_USER_GROUP_GROUPS AUGG, ART_QUERIES AQ "
					+ " WHERE AUGG.QUERY_GROUP_ID = AQ.QUERY_GROUP_ID "
					+ " AND EXISTS "
					+ " (SELECT * FROM ART_USER_GROUP_ASSIGNMENT AUGA WHERE AUGA.USERNAME = ?"
					+ " AND AUGA.USER_GROUP_ID=AUGG.USER_GROUP_ID)"
					+ " )";

			ps = conn.prepareStatement(sql);
			ps.setString(1, username);
			ps.setString(2, username);
			ps.setString(3, username);
			ps.setString(4, username);

			rs = ps.executeQuery();
			while (rs.next()) {
				AvailableReport report = new AvailableReport();

				report.setReportId(rs.getInt("QUERY_ID"));
				report.setName(rs.getString("QUERY_NAME"));
				report.setDescription(rs.getString("DESCRIPTION"));
				report.setUpdateDate(rs.getTimestamp("UPDATE_DATE"));
				report.setReportGroupId(rs.getInt("QUERY_GROUP_ID"));
				report.setReportGroupName(rs.getString("GROUP_NAME"));
				report.setCreationDate(rs.getTimestamp("CREATION_DATE"));

				reports.add(report);
			}
		} finally {
			DbUtils.close(rs, ps, conn);
		}

		return reports;
	}

	/**
	 * Get all reports
	 *
	 * @return list of all reports, empty list otherwise
	 * @throws SQLException
	 */
	@Cacheable("reports")
	public List<Report> getAllReports() throws SQLException {
		logger.debug("Entering getAllReports");

		ResultSetHandler<List<Report>> h = new BeanListHandler<>(Report.class, new ReportMapper());
		return dbService.query(SQL_SELECT_ALL, h);
	}

	/**
	 * Get a report
	 *
	 * @param id
	 * @return populated object if report found, null otherwise
	 * @throws SQLException
	 */
	@Cacheable("reports")
	public Report getReport(int id) throws SQLException {
		logger.debug("Entering getReport: id={}", id);

		String sql = SQL_SELECT_ALL + " WHERE QUERY_ID = ? ";
		ResultSetHandler<Report> h = new BeanHandler<>(Report.class, new ReportMapper());
		Report report = dbService.query(sql, h, id);
		setReportSource(report);
		return report;
	}

	/**
	 * Delete a report
	 *
	 * @param id
	 * @throws SQLException
	 */
	@CacheEvict(value = "reports", allEntries = true)
	public void deleteReport(int id) throws SQLException {
		logger.debug("Entering deleteReport: id={}", id);

		String sql;

		//delete query-user relationships
		sql = "DELETE FROM ART_USER_QUERIES WHERE QUERY_ID = ?";
		dbService.update(sql, id);

		//delete query parameters
		sql = "DELETE FROM ART_QUERY_FIELDS WHERE QUERY_ID = ?";
		dbService.update(sql, id);

		//delete sql source
		sql = "DELETE FROM ART_ALL_SOURCES WHERE OBJECT_ID = ?";
		dbService.update(sql, id);

		//delete query-rule relationships
		sql = "DELETE FROM ART_QUERY_RULES WHERE QUERY_ID = ?";
		dbService.update(sql, id);

		//delete drilldown queries
		sql = "DELETE FROM ART_DRILLDOWN_QUERIES WHERE QUERY_ID = ?";
		dbService.update(sql, id);

		//lastly, delete query
		sql = "DELETE FROM ART_QUERIES WHERE QUERY_ID = ?";
		dbService.update(sql, id);
	}

	/**
	 * Add a new report to the database
	 *
	 * @param report
	 * @return new record id if operation is successful, 0 otherwise
	 * @throws SQLException
	 */
	@CacheEvict(value = "reports", allEntries = true)
	public synchronized int addReport(Report report) throws SQLException {
		logger.debug("Entering addReport: report={}", report);

		//generate new id
		String sql = "SELECT MAX(QUERY_ID) FROM ART_QUERIES";
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

		sql = "INSERT INTO ART_QUERIES"
				+ " (QUERY_ID, NAME, SHORT_DESCRIPTION, DESCRIPTION, QUERY_TYPE,"
				+ " QUERY_GROUP_ID, DATABASE_ID, CONTACT_PERSON, USE_RULES,"
				+ " REPORT_STATUS, PARAMETERS_IN_OUTPUT, X_AXIS_LABEL, Y_AXIS_LABEL,"
				+ " GRAPH_OPTIONS, TEMPLATE, DISPLAY_RESULTSET, XMLA_URL,"
				+ " XMLA_DATASOURCE, XMLA_CATALOG, XMLA_USERNAME, XMLA_PASSWORD,"
				+ " CREATION_DATE)"
				+ " VALUES(" + StringUtils.repeat("?", ",", 22) + ")";

		//set values for possibly null property objects
		int reportGroupId; //database column doesn't allow null
		if (report.getReportGroup() == null) {
			logger.warn("Report group not defined. Defaulting to 0");
			reportGroupId = 0;
		} else {
			reportGroupId = report.getReportGroup().getReportGroupId();
		}

		int datasourceId; //database column doesn't allow null
		if (report.getDatasource() == null) {
			logger.warn("Datasource not defined. Defaulting to 0");
			datasourceId = 0;
		} else {
			datasourceId = report.getDatasource().getDatasourceId();
		}

		String reportStatus;
		if (report.getReportStatus() == null) {
			logger.warn("Report status not defined. Defaulting to null");
			reportStatus = null;
		} else {
			reportStatus = report.getReportStatus().getValue();
		}

		Object[] values = {
			newId,
			report.getName(),
			report.getShortDescription(),
			report.getDescription(),
			report.getReportType(),
			reportGroupId,
			datasourceId,
			report.getContactPerson(),
			report.isUseRules(),
			reportStatus,
			report.isParametersInOutput(),
			report.getxAxisLabel(),
			report.getyAxisLabel(),
			report.getChartOptionsSetting(),
			report.getTemplate(),
			report.getDisplayResultset(),
			report.getXmlaUrl(),
			report.getXmlaDatasource(),
			report.getXmlaCatalog(),
			report.getXmlaUsername(),
			report.getXmlaPassword(),
			DbUtils.getCurrentTimeStamp(),};

		int affectedRows = dbService.update(sql, values);
		logger.debug("affectedRows={}", affectedRows);

		if (affectedRows != 1) {
			logger.warn("Problem with add. affectedRows={}, report={}", affectedRows, report);
		}

		report.setReportId(newId);
		return newId;
	}

	/**
	 * Update an existing report
	 *
	 * @param report
	 * @throws SQLException
	 */
	@CacheEvict(value = "reports", allEntries = true)
	public void updateReport(Report report) throws SQLException {
		logger.debug("Entering updateReport: report={}", report);

		String sql = "UPDATE ART_QUERIES SET NAME=?, SHORT_DESCRIPTION=?,"
				+ " DESCRIPTION=?, QUERY_TYPE=?, QUERY_GROUP_ID=?,"
				+ " DATABASE_ID=?, CONTACT_PERSON=?, USE_RULES=?, "
				+ " REPORT_STATUS=?, PARAMETERS_IN_OUTPUT=?, X_AXIS_LABEL=?, Y_AXIS_LABEL=?,"
				+ " GRAPH_OPTIONS=?, TEMPLATE=?, DISPLAY_RESULTSET=?, XMLA_URL=?,"
				+ " XMLA_DATASOURCE=?, XMLA_CATALOG=?,"
				+ " XMLA_USERNAME=?, XMLA_PASSWORD=?, UPDATE_DATE=?"
				+ " WHERE QUERY_ID=?";

		//set values for possibly null property objects
		int reportGroupId; //database column doesn't allow null
		if (report.getReportGroup() == null) {
			logger.warn("Report group not defined. Defaulting to 0");
			reportGroupId = 0;
		} else {
			reportGroupId = report.getReportGroup().getReportGroupId();
		}

		int datasourceId; //database column doesn't allow null
		if (report.getDatasource() == null) {
			logger.warn("Datasource not defined. Defaulting to 0");
			datasourceId = 0;
		} else {
			datasourceId = report.getDatasource().getDatasourceId();
		}

		String reportStatus;
		if (report.getReportStatus() == null) {
			logger.warn("Report status not defined. Defaulting to null");
			reportStatus = null;
		} else {
			reportStatus = report.getReportStatus().getValue();
		}

		Object[] values = {
			report.getName(),
			report.getShortDescription(),
			report.getDescription(),
			report.getReportType(),
			reportGroupId,
			datasourceId,
			report.getContactPerson(),
			report.isUseRules(),
			reportStatus,
			report.isParametersInOutput(),
			report.getxAxisLabel(),
			report.getyAxisLabel(),
			report.getChartOptionsSetting(),
			report.getTemplate(),
			report.getDisplayResultset(),
			report.getXmlaUrl(),
			report.getXmlaDatasource(),
			report.getXmlaCatalog(),
			report.getXmlaUsername(),
			report.getXmlaPassword(),
			DbUtils.getCurrentTimeStamp(),
			report.getReportId()
		};

		int affectedRows = dbService.update(sql, values);
		logger.debug("affectedRows={}", affectedRows);

		if (affectedRows != 1) {
			logger.warn("Problem with update. affectedRows={}, report={}", affectedRows, report);
		}
	}

	/**
	 * Get values for possibly null property objects
	 *
	 * @param report
	 * @return list with values to save. Index 0 = report group id, 1 =
	 * datasource id, 2 = report status
	 */
	private List<Object> getSaveDefaults(Report report) {
		List<Object> values=new ArrayList<>();
		
		int reportGroupId; //database column doesn't allow null
		if (report.getReportGroup() == null) {
			logger.warn("Report group not defined. Defaulting to 0");
			reportGroupId = 0;
		} else {
			reportGroupId = report.getReportGroup().getReportGroupId();
		}
		values.add(reportGroupId);

		int datasourceId; //database column doesn't allow null
		if (report.getDatasource() == null) {
			logger.warn("Datasource not defined. Defaulting to 0");
			datasourceId = 0;
		} else {
			datasourceId = report.getDatasource().getDatasourceId();
		}
		values.add(datasourceId);

		String reportStatus;
		if (report.getReportStatus() == null) {
			logger.warn("Report status not defined. Defaulting to null");
			reportStatus = null;
		} else {
			reportStatus = report.getReportStatus().getValue();
		}
		values.add(reportStatus);
		
		return values;
	}

	/**
	 * Update the report source for a given report
	 *
	 * @param reportId
	 * @param reportSource new report source
	 * @throws SQLException
	 */
	public void updateReportSource(int reportId, String reportSource) throws SQLException {
		// Delete Old Source
		String sql = "DELETE FROM ART_ALL_SOURCES WHERE OBJECT_ID=?";
		dbService.update(sql, reportId);

		// Write the source in small segments
		// This guarantees portability across databases with different max VARCHAR sizes
		sql = "INSERT INTO ART_ALL_SOURCES "
				+ " (OBJECT_ID, LINE_NUMBER, SOURCE_INFO)"
				+ " VALUES(" + StringUtils.repeat("?", ",", 3) + ")";

		if (reportSource == null) {
			reportSource = "";
		}

		final int SOURCE_CHUNK_LENGTH = 4000; //length of column that holds report source

		List<Object[]> values = new ArrayList<>();

		int start = 0;
		int end = SOURCE_CHUNK_LENGTH;
		int lineNumber = 1;
		int textLength = reportSource.length();

		while (end < textLength) {
			values.add(new Object[]{
				Integer.valueOf(reportId),
				Integer.valueOf(lineNumber),
				reportSource.substring(start, end)
			});
			start = end;
			end = end + SOURCE_CHUNK_LENGTH;
			lineNumber++;
		}
		values.add(new Object[]{
			Integer.valueOf(reportId),
			Integer.valueOf(lineNumber),
			reportSource.substring(start)
		});

		dbService.batch(sql, values.toArray(new Object[0][]));
	}

	/**
	 * Get and populate the report source for a report
	 *
	 * @param report
	 * @throws SQLException
	 */
	private void setReportSource(Report report) throws SQLException {
		if (report == null) {
			return;
		}

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		String sql = "SELECT SOURCE_INFO"
				+ " FROM ART_ALL_SOURCES "
				+ " WHERE OBJECT_ID=?"
				+ " ORDER BY LINE_NUMBER";

		try {
			conn = ArtConfig.getConnection();
			rs = DbUtils.query(conn, ps, sql, report.getReportId());
			StringBuilder sb = new StringBuilder(1024);
			while (rs.next()) {
				sb.append(rs.getString("SOURCE_INFO"));
			}
			report.setReportSource(sb.toString());
			//set html source for use with text reports
			ReportType reportType = ReportType.toEnum(report.getReportType());
			if (reportType == ReportType.Text || reportType == ReportType.TextPublic) {
				report.setReportSourceHtml(report.getReportSource());
			}
		} finally {
			DbUtils.close(rs, ps, conn);
		}
	}

	/**
	 * Copy a report
	 *
	 * @param report new report
	 * @param originalReportId
	 * @throws SQLException
	 */
	@CacheEvict(value = "reports", allEntries = true)
	public void copyReport(Report report, int originalReportId) throws SQLException {
		//insert new report
		int newId = addReport(report);
		if (newId <= 0) {
			logger.warn("Report not copied: {}", report);
			return;
		}

		try {
			//copy parameters
			copyTableRow("ART_QUERY_FIELDS", "QUERY_ID", originalReportId, newId);

			//copy rules
			copyTableRow("ART_QUERY_RULES", "QUERY_ID", originalReportId, newId);

			//copy drilldown reports
			copyTableRow("ART_DRILLDOWN_QUERIES", "QUERY_ID", originalReportId, newId);
		} catch (SQLException ex) {
			//if an error occurred when copying report details, delete report also
			deleteReport(newId);
			throw ex;
		}
	}

	/**
	 * Copy some aspect of a report
	 *
	 * @param tableName
	 * @param keyColumnName
	 * @param keyId
	 * @param newKeyId
	 * @return the number of records copied, 0 otherwise
	 * @throws SQLException
	 */
	private int copyTableRow(String tableName, String keyColumnName,
			int keyId, int newKeyId) throws SQLException {

		int count = 0; //number of records copied

		Connection conn = ArtConfig.getConnection();

		if (conn == null) {
			logger.warn("Connection to the ART Database not available");
			return 0;
		}

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			String sql = "SELECT * FROM " + tableName
					+ " WHERE " + keyColumnName + " = ?";

			rs = DbUtils.query(conn, ps, sql, keyId);
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();

			sql = "INSERT INTO " + tableName + " VALUES ("
					+ StringUtils.repeat("?", ",", columnCount) + ")";

			while (rs.next()) {
				//insert new record for each existing record
				List<Object> columnValues = new ArrayList<>();
				for (int i = 0; i < columnCount; i++) {
					if (StringUtils.equalsIgnoreCase(rsmd.getColumnName(i + 1), keyColumnName)) {
						columnValues.add(newKeyId);
					} else {
						columnValues.add(rs.getObject(i + 1));
					}
				}

				dbService.update(sql, columnValues.toArray());
				count++;
			}
		} finally {
			DbUtils.close(rs, ps, conn);
		}

		return count;
	}

}
