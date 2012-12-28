package art.utils;

import art.servlets.ArtDBCP;
import java.sql.*;
import java.text.Collator;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * class to represent query groups
 * 
 * @author Timothy Anyona
 */
public class QueryGroup {

    final static Logger logger = LoggerFactory.getLogger(QueryGroup.class);
    int groupId = -1;
    String name = "";
    String description = "";

    /**
     * 
     */
    public QueryGroup() {
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

            String sql = "SELECT QUERY_GROUP_ID, NAME, DESCRIPTION "
                    + " FROM ART_QUERY_GROUPS "
                    + " WHERE QUERY_GROUP_ID = ?";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, gId);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                groupId = rs.getInt("QUERY_GROUP_ID");
                name = rs.getString("NAME");
                description = rs.getString("DESCRIPTION");
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

            String sql = "DELETE FROM ART_QUERY_GROUPS "
                    + " WHERE QUERY_GROUP_ID = ?";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, gId);

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
            String sql = "SELECT MAX(QUERY_GROUP_ID) FROM ART_QUERY_GROUPS ";

            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                groupId = rs.getInt(1) + 1;
            }
            rs.close();
            ps.close();

            //insert new group
            sql = "INSERT INTO ART_QUERY_GROUPS (QUERY_GROUP_ID, NAME, DESCRIPTION) "
                    + " VALUES (?,?,?)";

            ps = conn.prepareStatement(sql);
            ps.setInt(1, groupId);
            ps.setString(2, name);
            ps.setString(3, description);

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

            String sql = "UPDATE ART_QUERY_GROUPS SET "
                    + "  NAME = ?, DESCRIPTION = ?"
                    + " WHERE QUERY_GROUP_ID = ? ";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, description);
            ps.setInt(3, groupId);

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
     * Get all queries in a given group
     * 
     * @param gId
     * @return all queries in a given group
     */
    public Map getLinkedQueries(int gId) {
        TreeMap<Integer, ArtQuery> map = new TreeMap<Integer, ArtQuery>();

        Connection conn = null;

        try {
            conn = ArtDBCP.getConnection();

            String sql;
            PreparedStatement ps;
            ResultSet rs;
            int queryId;

            sql = "SELECT QUERY_GROUP_ID, QUERY_ID, NAME FROM ART_QUERIES "
                    + " WHERE QUERY_GROUP_ID = ?  ";

            ps = conn.prepareStatement(sql);
            ps.setInt(1, gId);

            rs = ps.executeQuery();
            while (rs.next()) {
                ArtQuery aq = new ArtQuery();
                queryId = rs.getInt("QUERY_ID");
                aq.setQueryId(queryId);
                aq.setGroupId(rs.getInt("QUERY_GROUP_ID"));
                aq.setName(rs.getString("NAME"));

                map.put(queryId, aq);
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
     * Get id and name for all query groups
     * 
     * @return id and name for all query groups
     */
    public Map<String, Integer> getAllQueryGroupNames() {
        Collator stringCollator = Collator.getInstance();
        stringCollator.setStrength(Collator.TERTIARY); //order by case
        TreeMap<String, Integer> map = new TreeMap<String, Integer>(stringCollator);

        Connection conn = null;

        try {
            conn = ArtDBCP.getConnection();
            Statement st = conn.createStatement();
            String sql = "SELECT QUERY_GROUP_ID, NAME "
                    + "FROM ART_QUERY_GROUPS";

            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                map.put(rs.getString("NAME"), Integer.valueOf(rs.getInt("QUERY_GROUP_ID")));
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
}