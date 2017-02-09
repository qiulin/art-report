/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package art.utils;

import art.connectionpool.DbConnections;
import art.dbutils.DatabaseUtils;
import art.enums.ColumnType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides helper methods to be used within the application
 *
 * @author Timothy Anyona
 */
public class ArtHelper {

	private static final Logger logger = LoggerFactory.getLogger(ArtHelper.class);
	private static final int MAX_LOG_MESSAGE_LENGTH = 500;

	/**
	 * Logs some action to the ART_LOGS table.
	 *
	 * @param user the username of user who executed the report
	 * @param type the type of event
	 * @param ip the ip address from which report was run
	 * @param reportId the id of the report that was run
	 * @param totalTime the total time to run the report and display the results
	 * @param fetchTime the time to fetch the results from the database
	 * @param message the log message
	 */
	public static void log(String user, String type, String ip, int reportId, long totalTime,
			long fetchTime, String message) {

		if (StringUtils.length(message) > MAX_LOG_MESSAGE_LENGTH) {
			message = message.substring(0, MAX_LOG_MESSAGE_LENGTH);
		}

		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = DbConnections.getArtDbConnection();
			String sql = "INSERT INTO ART_LOGS"
					+ " (LOG_DATE, USERNAME, LOG_TYPE, IP, QUERY_ID,"
					+ " TOTAL_TIME, FETCH_TIME, MESSAGE) "
					+ " VALUES (?,?,?,?,?,?,?,?) ";

			ps = conn.prepareStatement(sql);

			Timestamp now = new Timestamp(new Date().getTime());

			ps.setTimestamp(1, now);
			ps.setString(2, user);
			ps.setString(3, type);
			ps.setString(4, ip);
			ps.setInt(5, reportId);
			ps.setInt(6, (int) totalTime);
			ps.setInt(7, (int) fetchTime);
			ps.setString(8, message);

			ps.executeUpdate();
		} catch (SQLException ex) {
			logger.error("Error", ex);
		} finally {
			DatabaseUtils.close(ps, conn);
		}
	}

	/**
	 * Logs login attempts to the ART_LOGS table.
	 *
	 * @param user the username
	 * @param type "login" if successful or "loginerr" if not
	 * @param ip the ip address from which login was done or attempted
	 * @param message the log message
	 */
	public static void log(String user, String type, String ip, String message) {
		if (StringUtils.length(message) > MAX_LOG_MESSAGE_LENGTH) {
			message = message.substring(0, MAX_LOG_MESSAGE_LENGTH);
		}

		Connection conn = null;
		PreparedStatement ps = null;
		try {
			Timestamp now = new Timestamp(new Date().getTime());
			conn = DbConnections.getArtDbConnection();
			String sql = "INSERT INTO ART_LOGS"
					+ " (LOG_DATE, USERNAME, LOG_TYPE, IP, MESSAGE) "
					+ " VALUES (?,?,?,?,?) ";
			ps = conn.prepareStatement(sql);

			ps.setTimestamp(1, now);
			ps.setString(2, user);
			ps.setString(3, type);
			ps.setString(4, ip);
			ps.setString(5, message);

			ps.executeUpdate();
		} catch (SQLException ex) {
			logger.error("Error", ex);
		} finally {
			DatabaseUtils.close(ps, conn);
		}
	}
}
