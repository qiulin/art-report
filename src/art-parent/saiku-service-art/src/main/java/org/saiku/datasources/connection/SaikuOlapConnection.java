/*  
 *   Copyright 2012 OSBI Ltd
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.saiku.datasources.connection;

import org.olap4j.OlapConnection;
import org.olap4j.OlapWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import mondrian.rolap.RolapConnection;
import org.apache.commons.lang.StringUtils;
import org.olap4j.metadata.Catalog;
import org.olap4j.metadata.NamedList;

public class SaikuOlapConnection implements ISaikuConnection {

	private String name;
	private boolean initialized = false;
	private Properties properties;
	private OlapConnection olapConnection;
	private String username;
	private String password;

	private static final Logger log = LoggerFactory.getLogger(SaikuOlapConnection.class);

	public SaikuOlapConnection(String name, Properties props) {
		this.name = name;
		this.properties = props;
	}

	public SaikuOlapConnection(Properties props) {
		this.properties = props;
		this.name = props.getProperty(ISaikuConnection.NAME_KEY);
	}

	public boolean connect() throws Exception {
		return connect(properties);
	}

	public boolean connect(Properties props) throws Exception {
		String safemode = System.getProperty("saiku.safemode");
		if (safemode != null && safemode.equals("true")) {
			log.debug("Not starting connection " + name + ", Saiku in safe mode");
			return false;
		} else {
			if ((props.containsKey("enabled") && props.getProperty("enabled").equals("true")) || !props.containsKey("enabled")) {
				this.username = props.getProperty(ISaikuConnection.USERNAME_KEY);
				this.password = props.getProperty(ISaikuConnection.PASSWORD_KEY);
				String driver = props.getProperty(ISaikuConnection.DRIVER_KEY);
				this.properties = props;
				String url = props.getProperty(ISaikuConnection.URL_KEY);

//				if (url.contains("Mondrian=4")) {
//					url = url.replace("Mondrian=4; ", "");
//					url = url.replace("jdbc:mondrian", "jdbc:mondrian4");
//				}
				if (url.length() > 0 && url.charAt(url.length() - 1) != ';') {
					url += ";";
				}
				//use ends with instead of equals to cater for net.sf.mondrianart.mondrian.olap4j.MondrianOlap4jDriver
				if (StringUtils.endsWith(driver, "mondrian.olap4j.MondrianOlap4jDriver")) {
					if (username != null && username.length() > 0) {
						url += "JdbcUser=" + username + ";";
					}
					if (password != null && password.length() > 0) {
						url += "JdbcPassword=" + password + ";";
					}
				}

				Class.forName(driver);
				Connection connection = DriverManager.getConnection(url, username, password);

				if (connection != null) {
					final OlapWrapper wrapper = (OlapWrapper) connection;
					OlapConnection tmpolapConnection = wrapper.unwrap(OlapConnection.class);

					if (tmpolapConnection == null) {
						throw new Exception("Connection is null");
					}

//					NamedList<Catalog> catalogs = tmpolapConnection.getOlapCatalogs();
//					log.info("Catalogs: " + catalogs.size());
					olapConnection = tmpolapConnection;
					initialized = true;
					return true;
				}
			} else if (props.containsKey("enabled") && props.getProperty("enabled").equals("false")) {
				log.info("Datasource marked as disabled.");
				return false;
			}
			return false;
		}
	}

	public boolean clearCache() throws Exception {
		if (olapConnection.isWrapperFor(RolapConnection.class)) {
			log.info("Clearing cache");
			RolapConnection rcon = olapConnection.unwrap(RolapConnection.class);
			rcon.getCacheControl(null).flushSchemaCache();
		}
		return true;
	}

	public String getDatasourceType() {
		return ISaikuConnection.OLAP_DATASOURCE;
	}

	public boolean initialized() {
		return initialized;
	}

	public Connection getConnection() {
		try {
			if (olapConnection.isClosed()) {
				connect();
			}
		} catch (Exception e) {
			log.error("Error", e);
		}
		return olapConnection;
	}

	public void setProperties(Properties props) {
		properties = props;
	}

	public String getName() {
		return name;
	}

	public Properties getProperties() {
		return properties;
	}

	public void close() throws SQLException {
		if (!olapConnection.isClosed()) {
			olapConnection.close();
		}
	}

}
