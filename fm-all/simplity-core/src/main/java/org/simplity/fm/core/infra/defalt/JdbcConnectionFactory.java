// SPDX-License-Identifier: MIT
package org.simplity.fm.core.infra.defalt;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.simplity.fm.core.infra.DbConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * serves as an example, or even a base class, for an application to design its
 * IConnecitonFactory class.
 *
 * @author simplity.org
 *
 */
public class JdbcConnectionFactory {
	private static final Logger logger = LoggerFactory.getLogger(JdbcConnectionFactory.class);

	/**
	 * properties file expected to be found in resources folder
	 */
	public static final String PROPERTIES_FILE = "db.properties";
	private static final String URL_KEY = "db.url";
	private static final String USER_KEY = "db.user";
	private static final String PASSWORD_KEY = "db.password";
	private static final String DRIVER_KEY = "db.driver";

	/**
	 *
	 * @param resourceURL URL for the db.properties file. typically obtained as
	 *                    class.getClassLoader().getResource("db.properties")
	 * @return a new instance of DbConnectionFactory based on properties file
	 */
	public static DbConnectionFactory newFactory(URL resourceURL) {
		Properties dbProperties = loadProperties(resourceURL);
		if (dbProperties == null) {
			return null;
		}

		String driverName = getDriverClassName(dbProperties);

		try {
			Class.forName(driverName);
		} catch (ClassNotFoundException e) {
			logger.error("JDBC driver not found: {}. Make sure the driver JAR is in the classpath.", driverName);
			return null;
		}
		final String url = dbProperties.getProperty(URL_KEY);
		final String user = dbProperties.getProperty(USER_KEY);
		final String password = dbProperties.getProperty(PASSWORD_KEY);

		return new DbConnectionFactory() {

			@SuppressWarnings("resource")
			@Override
			public Connection getConnection() throws SQLException {
				return DriverManager.getConnection(url, user, password);
			}

			@Override
			public Connection getConnection(String schema) throws SQLException {
				throw new UnsupportedOperationException("Getting connection to alternate schema is not supported.");
			}
		};
	}

	/**
	 * Loads database properties from the db.properties file in resources folder
	 */
	static Properties loadProperties(URL resourceURL) {
		Properties dbProperties = new Properties();
		try (InputStream inputStream = resourceURL.openStream()) {

			dbProperties.load(inputStream);

			// Validate required properties exist
			String[] requiredKeys = { URL_KEY, USER_KEY, PASSWORD_KEY, DRIVER_KEY };

			for (String key : requiredKeys) {
				if (dbProperties.getProperty(key) == null || dbProperties.getProperty(key).trim().isEmpty()) {
					logger.error("Error while reading properties file{}. Missing required propert:{}",
							resourceURL.getPath(), key);
					return null;
				}
			}
			return dbProperties;

		} catch (IOException e) {
			logger.error("Error while reading properties file{}. {}", resourceURL.getPath(), e.getMessage());
			return null;
		}
	}

	/**
	 * Maps driver names to their corresponding JDBC driver class names
	 */
	static String getDriverClassName(Properties dbProperties) {
		String driver = dbProperties.getProperty(DRIVER_KEY).toLowerCase();

		switch (driver) {
		case "postgres":
		case "postgresql":
			return "org.postgresql.Driver";
		case "sqlserver":
		case "mssql":
			return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
		case "mysql":
			return "com.mysql.cj.jdbc.Driver";
		case "oracle":
			return "oracle.jdbc.driver.OracleDriver";
		case "h2":
			return "org.h2.Driver";
		case "sqlite":
			return "org.sqlite.JDBC";
		default:
			// Assume the driver property contains the full class name
			return dbProperties.getProperty(DRIVER_KEY);
		}
	}
}
