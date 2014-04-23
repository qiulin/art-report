/**
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
package art.usergroupmembership;

import art.dbutils.DbService;
import art.user.User;
import art.usergroup.UserGroup;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Class to provide methods related to user group membership
 *
 * @author Timothy Anyona
 */
@Service
public class UserGroupMembershipService {

	private static final Logger logger = LoggerFactory.getLogger(UserGroupMembershipService.class);

	@Autowired
	private DbService dbService;

	private final String SQL_SELECT_ALL
			= "SELECT AU.USER_ID, AU.USERNAME, AUG.USER_GROUP_ID, AUG.NAME AS GROUP_NAME"
			+ " FROM ART_USER_GROUP_ASSIGNMENT AUGA"
			+ " INNER JOIN ART_USERS AU ON"
			+ " AUGA.USER_ID=AU.USER_ID"
			+ " INNER JOIN ART_USER_GROUPS AUG ON"
			+ " AUGA.USER_GROUP_ID=AUG.USER_GROUP_ID";

	/**
	 * Class to map resultset to an object
	 */
	private class UserGroupMembershipMapper extends BasicRowProcessor {

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
			UserGroupMembership membership = new UserGroupMembership();

			User user = new User();
			user.setUserId(rs.getInt("USER_ID"));
			user.setUsername(rs.getString("USERNAME"));

			membership.setUser(user);

			UserGroup group = new UserGroup();
			group.setUserGroupId(rs.getInt("USER_GROUP_ID"));
			group.setName(rs.getString("GROUP_NAME"));

			membership.setUserGroup(group);

			return type.cast(membership);
		}
	}

	/**
	 * Get all user group memberships
	 *
	 * @return list of all user group memberships, empty list otherwise
	 * @throws SQLException
	 */
	public List<UserGroupMembership> getAllUserGroupMemberships() throws SQLException {
		logger.debug("Entering getAllUserGroupMemberships");

		ResultSetHandler<List<UserGroupMembership>> h = new BeanListHandler<>(UserGroupMembership.class, new UserGroupMembershipMapper());
		return dbService.query(SQL_SELECT_ALL, h);
	}

	/**
	 * Delete a user group membership
	 *
	 * @param userId
	 * @param userGroupId
	 * @throws SQLException
	 */
	public void deleteUserGroupMembership(int userId, int userGroupId) throws SQLException {
		logger.debug("Entering deleteUserGroupMembership: userId={}, userGroupId={}",
				userId, userGroupId);

		String sql;

		sql = "DELETE FROM ART_USER_GROUP_ASSIGNMENT WHERE USER_ID=? AND USER_GROUP_ID=?";
		dbService.update(sql, userId, userGroupId);
	}

	/**
	 * Add or remove user group memberships
	 *
	 * @param action "ADD" or "REMOVE"
	 * @param users
	 * @param userGroups array of user group ids
	 * @throws SQLException
	 */
	public void updateUserGroupMemberships(String action, String[] users, Integer[] userGroups) throws SQLException {
		logger.debug("Entering updateUserGroupMemberships: action='{}'", action);

		if (action == null || users == null || userGroups == null) {
			return;
		}

		String sql;

		if (action.equals("ADD")) {
			sql = "INSERT INTO ART_USER_GROUP_ASSIGNMENT (USER_ID, USERNAME, USER_GROUP_ID) VALUES (?, ?, ?)";
		} else {
			sql = "DELETE FROM ART_USER_GROUP_ASSIGNMENT WHERE USER_ID=? AND USERNAME=? AND USER_GROUP_ID=?";
		}

		String sqlTest = "UPDATE ART_USER_GROUP_ASSIGNMENT SET USER_ID=? WHERE USER_ID=? AND USERNAME=? AND USER_GROUP_ID=?";
		int affectedRows;
		boolean updateRight;

		for (String user : users) {
			Integer userId = Integer.valueOf(StringUtils.substringBefore(user, "-"));
			//username won't be needed once user id columns completely replace username in foreign keys
			String username = StringUtils.substringAfter(user, "-");

			for (Integer userGroupId : userGroups) {
				updateRight = true;
				if (action.equals("ADD")) {
					//test if record exists. to avoid integrity constraint error
					affectedRows = dbService.update(sqlTest, userId, userId, username, userGroupId);
					if (affectedRows > 0) {
						//record exists. don't attempt a reinsert.
						updateRight = false;
					}
				}
				if (updateRight) {
					dbService.update(sql, userId, username, userGroupId);
				}
			}
		}
	}

}