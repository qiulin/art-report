package art.reportgroup;

import art.servlets.ArtConfig;
import art.utils.DbUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

/**
 * Class to provide data access methods for report groups. Data stored in ART_QUERY_GROUPS
 * 
 * @author Timothy Anyona
 */
@Repository
public class ReportGroupRepository {
	final static Logger logger = LoggerFactory.getLogger(ReportGroupRepository.class);
	
	/**
	 * Get report groups that are available for selection for a given user
	 * 
	 * @param username
	 * @return
	 * @throws SQLException 
	 */
	public List<ReportGroup> getAvailableReportGroups(String username) throws SQLException {
		//use set to ensure unique items but return list for consistency
		Set<ReportGroup> groups = new HashSet<ReportGroup>();
		
		System.out.println("cache miss. " + username);

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		//use list to avoid retrieving already added groups
		List<Integer> groupIds=new ArrayList<Integer>();
		int groupId;

		try {
			//get groups that user has explicit rights to see
			conn = ArtConfig.getConnection();

			String sql;

			try {
				sql = "SELECT aqg.QUERY_GROUP_ID, aqg.NAME, aqg.DESCRIPTION "
						+ " FROM ART_USER_QUERY_GROUPS auqg , ART_QUERY_GROUPS aqg "
						+ " WHERE auqg.USERNAME = ? "
						+ " AND auqg.QUERY_GROUP_ID = aqg.QUERY_GROUP_ID ";

				ps = conn.prepareStatement(sql);
				ps.setString(1, username);

				rs = ps.executeQuery();
				while (rs.next()) {
					ReportGroup group = new ReportGroup();

					groupId=rs.getInt("QUERY_GROUP_ID");
					group.setReportGroupId(groupId);
					group.setName(rs.getString("NAME"));
					group.setDescription(rs.getString("DESCRIPTION"));

					groups.add(group);
					groupIds.add(Integer.valueOf(groupId));
				}
			} finally {
				DbUtils.close(rs, ps);
			}

			//add groups to which the user has access through his user group 
			try {
				sql = "SELECT DISTINCT AQG.QUERY_GROUP_ID, AQG.NAME, AQG.DESCRIPTION "
						+ " FROM ART_USER_GROUP_GROUPS AUGG, ART_QUERY_GROUPS AQG "
						+ " WHERE AUGG.QUERY_GROUP_ID=AQG.QUERY_GROUP_ID "
						+ " AND EXISTS (SELECT * FROM ART_USER_GROUP_ASSIGNMENT AUGA "
						+ " WHERE AUGA.USERNAME = ? AND AUGA.USER_GROUP_ID = AUGG.USER_GROUP_ID)";

				if(!groupIds.isEmpty()){
					sql=sql + " AND AQG.QUERY_GROUP_ID NOT IN(" + StringUtils.join(groupIds, ",") + ")";
				}
				
				ps = conn.prepareStatement(sql);
				ps.setString(1, username);

				rs = ps.executeQuery();
				while (rs.next()) {
					ReportGroup group = new ReportGroup();

					groupId=rs.getInt("QUERY_GROUP_ID");
					group.setReportGroupId(groupId);
					group.setName(rs.getString("NAME"));
					group.setDescription(rs.getString("DESCRIPTION"));

					groups.add(group);
					groupIds.add(Integer.valueOf(groupId));
				}
			} finally {
				DbUtils.close(rs, ps);
			}

			//add groups where user has right to query but not to group
			try {
				sql = "SELECT DISTINCT AQG.QUERY_GROUP_ID, AQG.NAME, AQG.DESCRIPTION "
						+ " FROM ART_USER_QUERIES AUQ, ART_QUERIES AQ, ART_QUERY_GROUPS AQG "
						+ " WHERE AUQ.QUERY_ID=AQ.QUERY_ID AND AQ.QUERY_GROUP_ID=AQG.QUERY_GROUP_ID "
						+ " AND AUQ.USERNAME=? AND AQG.QUERY_GROUP_ID<>0"
						+ " AND AQ.QUERY_TYPE<>119 AND AQ.QUERY_TYPE<>120";
				
				if(!groupIds.isEmpty()){
					sql=sql + " AND AQG.QUERY_GROUP_ID NOT IN(" + StringUtils.join(groupIds, ",") + ")";
				}

				ps = conn.prepareStatement(sql);
				ps.setString(1, username);

				rs = ps.executeQuery();
				while (rs.next()) {
					ReportGroup group = new ReportGroup();

					groupId=rs.getInt("QUERY_GROUP_ID");
					group.setReportGroupId(groupId);
					group.setName(rs.getString("NAME"));
					group.setDescription(rs.getString("DESCRIPTION"));

					groups.add(group);
					groupIds.add(Integer.valueOf(groupId));
				}
			} finally {
				DbUtils.close(rs, ps);
			}

			//add groups where user's group has rights to the query
			try {
				sql = "SELECT DISTINCT AQG.QUERY_GROUP_ID, AQG.NAME, AQG.DESCRIPTION "
						+ " FROM ART_USER_GROUP_QUERIES AUGQ, ART_QUERIES AQ, ART_QUERY_GROUPS AQG "
						+ " WHERE AUGQ.QUERY_ID=AQ.QUERY_ID AND AQ.QUERY_GROUP_ID=AQG.QUERY_GROUP_ID "
						+ " AND AQG.QUERY_GROUP_ID<>0 AND AQ.QUERY_TYPE<>119 AND AQ.QUERY_TYPE<>120 "
						+ " AND EXISTS (SELECT * FROM ART_USER_GROUP_ASSIGNMENT AUGA "
						+ " WHERE AUGA.USERNAME = ? AND AUGA.USER_GROUP_ID = AUGQ.USER_GROUP_ID)";
				
				if(!groupIds.isEmpty()){
					sql=sql + " AND AQG.QUERY_GROUP_ID NOT IN(" + StringUtils.join(groupIds, ",") + ")";
				}

				ps = conn.prepareStatement(sql);
				ps.setString(1, username);

				rs = ps.executeQuery();
				while (rs.next()) {
					ReportGroup group = new ReportGroup();

					group.setReportGroupId(rs.getInt("QUERY_GROUP_ID"));
					group.setName(rs.getString("NAME"));
					group.setDescription(rs.getString("DESCRIPTION"));

					groups.add(group);
				}
			} finally {
				DbUtils.close(rs, ps);
			}
		} finally {
			DbUtils.close(conn);
		}
		
		return new ArrayList<ReportGroup>(groups);
	}
}
