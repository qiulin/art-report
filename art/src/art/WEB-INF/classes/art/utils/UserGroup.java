package art.utils;

import art.servlets.ArtDBCP;
import java.sql.*;
import java.text.Collator;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to represent user groups
 * 
 * @author Timothy Anyona
 */
public class UserGroup {

    final static Logger logger = LoggerFactory.getLogger(UserGroup.class);
    int groupId = -1;
    String name = "";
    String description = "";
    int defaultObjectGroup = -1;

    /**
     * 
     */
    public UserGroup() {
    }

    /**
     * 
     * @param value
     */
    public void setDefaultObjectGroup(int value) {
        defaultObjectGroup = value;
    }

    /**
     * 
     * @return default object group
     */
    public int getDefaultObjectGroup() {
        return defaultObjectGroup;
    }

    /**
     * 
     * @param value
     */
    public void setGroupId(int value) {
        groupId = value;
    }

    /**
     * 
     * @return group id
     */
    public int getGroupId() {
        return groupId;
    }

    /**
     * 
     * @param value
     */
    public void setName(String value) {
        name = value;
    }

    /**
     * 
     * @return group name
     */
    public String getName() {
        return name;
    }

    /**
     * 
     * @param value
     */
    public void setDescription(String value) {
        description = value;
    }

    /**
     * 
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Populate the object with details of the given group
     * 
     * @param gId
     * @return <code>true</code> if successful
     */
    public boolean load(int gId) {
        boolean success = false;

        Connection conn = null;

        try {
            conn = ArtDBCP.getConnection();

            String sql = "SELECT USER_GROUP_ID, NAME, DESCRIPTION, DEFAULT_OBJECT_GROUP "
                    + " FROM ART_USER_GROUPS "
                    + " WHERE USER_GROUP_ID = ?";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, gId);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                groupId = rs.getInt("USER_GROUP_ID");
                name = rs.getString("NAME");
                description = rs.getString("DESCRIPTION");
                defaultObjectGroup = rs.getInt("DEFAULT_OBJECT_GROUP");
            }
            rs.close();
            ps.close();
            success = true;
        } catch (Exception e) {
            logger.error("Error", e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
                logger.error("Error", e);
            }
        }

        return success;
    }

    /**
     * Delete group
     * 
     * @param gId
     * @return <code>true</code> if successful
     */
    public boolean delete(int gId) {
        boolean success = false;

        Connection conn = null;

        try {
            conn = ArtDBCP.getConnection();

            Statement st;

            st = conn.createStatement();

            //delete user group rights
            st.executeUpdate("DELETE FROM ART_USER_GROUP_GROUPS WHERE USER_GROUP_ID = " + gId);
            st.executeUpdate("DELETE FROM ART_USER_GROUP_QUERIES WHERE USER_GROUP_ID = " + gId);

            //delete user group memberships
            st.executeUpdate("DELETE FROM ART_USER_GROUP_ASSIGNMENT WHERE USER_GROUP_ID = " + gId);

            //delete user group shared jobs
            st.executeUpdate("DELETE FROM ART_USER_GROUP_JOBS WHERE USER_GROUP_ID = " + gId);

            //delete user group
            st.executeUpdate("DELETE FROM ART_USER_GROUPS WHERE USER_GROUP_ID = " + gId);

            st.close();
            success = true;
        } catch (Exception e) {
            logger.error("Error", e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
                logger.error("Error", e);
            }
        }

        return success;
    }

    /**
     * Insert new group
     * 
     * @return <code>true</code> if successful
     */
    public boolean insert() {
        boolean success = false;

        Connection conn = null;

        try {
            conn = ArtDBCP.getConnection();

            // get new query group id
            String sql = "SELECT MAX(USER_GROUP_ID) FROM ART_USER_GROUPS ";

            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                groupId = rs.getInt(1) + 1;
            }
            rs.close();
            ps.close();

            //insert new group
            sql = "INSERT INTO ART_USER_GROUPS (USER_GROUP_ID, NAME, DESCRIPTION, DEFAULT_OBJECT_GROUP) "
                    + " VALUES (?,?,?,?)";

            ps = conn.prepareStatement(sql);
            ps.setInt(1, groupId);
            ps.setString(2, name);
            ps.setString(3, description);
            ps.setInt(4, defaultObjectGroup);

            ps.executeUpdate();
            ps.close();
            success = true;
        } catch (Exception e) {
            logger.error("Error", e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
                logger.error("Error", e);
            }
        }

        return success;
    }

    /**
     * Update group
     * 
     * @return <code>true</code> if successful
     */
    public boolean update() {
        boolean success = false;

        Connection conn = null;

        try {
            conn = ArtDBCP.getConnection();

            String sql = "UPDATE ART_USER_GROUPS SET "
                    + "  NAME = ?, DESCRIPTION = ?, DEFAULT_OBJECT_GROUP=? "
                    + " WHERE USER_GROUP_ID = ? ";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, description);
            ps.setInt(3, defaultObjectGroup);
            ps.setInt(4, groupId);

            ps.executeUpdate();
            ps.close();
            success = true;
        } catch (Exception e) {
            logger.error("Error", e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
                logger.error("Error", e);
            }
        }

        return success;
    }

    /**
     * Add users to this user group
     * 
     * @param users
     */
    public void addUsers(String[] users) {
        Connection conn = null;

        try {
            conn = ArtDBCP.getConnection();

            String sql = "INSERT INTO ART_USER_GROUP_ASSIGNMENT (USERNAME, USER_GROUP_ID) VALUES (?,?)";

            PreparedStatement ps = conn.prepareStatement(sql);
            if (users != null) {
                for (int i = 0; i < users.length; i++) {
                    ps.setString(1, users[i]);
                    ps.setInt(2, groupId);
                    try {
                        ps.executeUpdate();
                    } catch (SQLException e) {
                        //likely user already belongs to the group. don't log error (primary key already exists)
                    }
                }
            }
            ps.close();
        } catch (Exception e) {
            logger.error("Error", e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
                logger.error("Error", e);
            }
        }
    }

    /**
     * Remove users from this user group
     * 
     * @param users
     */
    public void removeUsers(String[] users) {
        Connection conn = null;

        try {
            conn = ArtDBCP.getConnection();

            String sql = "DELETE FROM ART_USER_GROUP_ASSIGNMENT WHERE USERNAME = ? AND USER_GROUP_ID = ?";
            PreparedStatement ps = conn.prepareStatement(sql);

            //allow for deleting of split jobs where access was granted to the user via group membership			
            String sqlSplitJob = "DELETE FROM ART_SHARED_JOBS WHERE USERNAME = ? AND USER_GROUP_ID = ?";
            PreparedStatement psSplitJob = conn.prepareStatement(sqlSplitJob);

            if (users != null) {
                for (int i = 0; i < users.length; i++) {
                    try {
                        //remove user from group
                        ps.setString(1, users[i]);
                        ps.setInt(2, groupId);
                        ps.executeUpdate();

                        //delete split jobs where access was granted to the user via group membership	
                        psSplitJob.setString(1, users[i]);
                        psSplitJob.setInt(2, groupId);
                        psSplitJob.executeUpdate();
                    } catch (SQLException e) {
                        logger.error("Error", e);
                    }
                }
            }
            ps.close();
            psSplitJob.close();
        } catch (Exception e) {
            logger.error("Error", e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
                logger.error("Error", e);
            }
        }
    }

    /**
     * Get id and name for all user groups
     * 
     * @return id and name for all user groups
     */
    public Map getAllUserGroupNames() {
        Collator stringCollator = Collator.getInstance();
        stringCollator.setStrength(Collator.TERTIARY); //order by case
        TreeMap<String, Integer> map = new TreeMap<String, Integer>(stringCollator);

        Connection conn = null;

        try {
            conn = ArtDBCP.getConnection();

            Statement st = conn.createStatement();
            String sql = "SELECT USER_GROUP_ID, NAME "
                    + "FROM ART_USER_GROUPS";

            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                map.put(rs.getString("NAME"), new Integer(rs.getInt("USER_GROUP_ID")));
            }
            rs.close();
            st.close();

        } catch (Exception e) {
            logger.error("Error", e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error("Error", e);
            }
        }

        return map;
    }

    /**
     * Get an indicator of which users belong to which user groups
     * 
     * @return an indicator of which users belong to which user groups
     */
    public Map getUserGroupAssignment() {
        TreeMap<Integer, String> map = new TreeMap<Integer, String>();

        Connection conn = null;

        try {
            conn = ArtDBCP.getConnection();

            String sql;
            PreparedStatement ps;
            ResultSet rs;
            String tmp;
            Integer count = 0;

            sql = "SELECT AUGA.USERNAME, AUG.NAME "
                    + " FROM ART_USER_GROUP_ASSIGNMENT AUGA,ART_USER_GROUPS AUG "
                    + " WHERE AUGA.USER_GROUP_ID=AUG.USER_GROUP_ID "
                    + " ORDER BY AUGA.USERNAME";

            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                count++;
                tmp = rs.getString("USERNAME") + " - " + rs.getString("NAME");
                map.put(count, tmp);
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            logger.error("Error", e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error("Error", e);
            }
        }

        return map;
    }

    /**
     * Get an indicator of which user groups a given user belongs to
     * 
     * @param user
     * @return an indicator of which user groups a given user belongs to
     */
    public Map getUserGroupMemberships(String user) {
        TreeMap<Integer, String> map = new TreeMap<Integer, String>();

        Connection conn = null;

        try {
            conn = ArtDBCP.getConnection();

            String sql;
            PreparedStatement ps;
            ResultSet rs;
            String tmp;
            Integer count = 0;

            sql = "SELECT AUG.NAME "
                    + " FROM ART_USER_GROUP_ASSIGNMENT AUGA,ART_USER_GROUPS AUG "
                    + " WHERE AUGA.USER_GROUP_ID=AUG.USER_GROUP_ID "
                    + " AND AUGA.USERNAME=? "
                    + " ORDER BY AUGA.USERNAME";

            ps = conn.prepareStatement(sql);
            ps.setString(1, user);
            rs = ps.executeQuery();
            while (rs.next()) {
                count++;
                tmp = rs.getString("NAME");
                map.put(count, tmp);
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            logger.error("Error", e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error("Error", e);
            }
        }

        return map;
    }

    /**
     * Get an indicator of which users belong to this user groups
     * 
     * @return an indicator of which users belong to this user groups
     */
    public Map getUserGroupMembers() {
        TreeMap<Integer, String> map = new TreeMap<Integer, String>();

        Connection conn = null;

        try {
            conn = ArtDBCP.getConnection();

            String sql;
            PreparedStatement ps;
            ResultSet rs;
            String tmp;
            Integer count = 0;

            sql = "SELECT AUGA.USERNAME "
                    + " FROM ART_USER_GROUP_ASSIGNMENT AUGA "
                    + " WHERE AUGA.USER_GROUP_ID=? "
                    + " ORDER BY AUGA.USERNAME";

            ps = conn.prepareStatement(sql);
            ps.setInt(1, groupId);
            rs = ps.executeQuery();
            while (rs.next()) {
                count++;
                tmp = rs.getString("USERNAME");
                map.put(count, tmp);
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            logger.error("Error", e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error("Error", e);
            }
        }

        return map;
    }

    /**
     * Add or remove users from user groups
     * 
     * @param action
     * @param users
     * @param groups
     */
    public void updateUserGroupAssignment(String action, String[] users, String[] groups) {

        if (action == null || users == null || groups == null) {
            return;
        }

        Connection conn = null;

        try {
            conn = ArtDBCP.getConnection();

            String sql;
            PreparedStatement ps;

            if (action.equals("GRANT")) {
                sql = "INSERT INTO ART_USER_GROUP_ASSIGNMENT (USERNAME, USER_GROUP_ID) values (? , ? ) ";
            } else {
                sql = "DELETE FROM ART_USER_GROUP_ASSIGNMENT WHERE USERNAME = ? AND USER_GROUP_ID = ? ";
            }
            ps = conn.prepareStatement(sql);

            //allow for deleting of split jobs where access was granted via group membership			
            String sqlRemove = "DELETE FROM ART_SHARED_JOBS WHERE USERNAME = ? AND USER_GROUP_ID = ?";
            PreparedStatement psRemove = conn.prepareStatement(sqlRemove);

            for (int i = 0; i < users.length; i++) {
                for (int j = 0; j < groups.length; j++) {
                    ps.setString(1, users[i]);
                    ps.setInt(2, Integer.parseInt(groups[j]));
                    try {
                        ps.executeUpdate();
                    } catch (SQLIntegrityConstraintViolationException e) {
                        logger.info("User {} already a member of User Group ID {}", users[i], groups[j]);
                    } catch (SQLException e) {
                        logger.error("Error updating user group assignment. User={}, Group ID={}", new Object[]{users[i], groups[j], e});
                    }

                    //delete split jobs where access was granted via group membership
                    if (action.equals("REVOKE")) {
                        try {
                            psRemove.setString(1, users[i]);
                            psRemove.setInt(2, Integer.parseInt(groups[j]));
                            psRemove.executeUpdate();
                        } catch (SQLException e) {
                            logger.error("Error while removing split jobs", e);
                        }
                    }
                }
            }
            ps.close();
            psRemove.close();
        } catch (Exception e) {
            logger.error("Error", e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error("Error", e);
            }
        }
    }
	
	/**
     * Get the name of a given user group
     * 
     * @return an indicator of which users belong to this user groups
     */
    public String getUserGroupName(int gId) {
        String gName="";

        Connection conn = null;

        try {
            conn = ArtDBCP.getConnection();

            String sql;
            PreparedStatement ps;
            ResultSet rs;

            sql = "SELECT NAME "
                    + " FROM ART_USER_GROUPS "
                    + " WHERE USER_GROUP_ID=? ";

            ps = conn.prepareStatement(sql);
            ps.setInt(1, gId);
            rs = ps.executeQuery();
            if (rs.next()) {
                gName = rs.getString("NAME");
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            logger.error("Error", e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
                logger.error("Error", e);
            }
        }

        return gName;
    }
}