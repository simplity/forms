// SPDX-License-Identifier: MIT
package org.simplity.fm.core.infra;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Provide a SQL connection for the RDBMS service used by this app
 * 
 * @author simplity.org
 *
 */
public interface DbConnectionFactory {
	/**
	 *
	 * @return non-null sql connection for default schema or this application.
	 * @throws SQLException
	 *             if no driver is set-up, or there is some problem in getting a
	 *             connection
	 */
	Connection getConnection() throws SQLException;

	/**
	 * to be used to get a connection to a schema that is not the default for
	 * the application
	 *
	 * @param schema
	 *            non-null schema name.
	 * @return non-null sql connection for default schema or this application.
	 * @throws SQLException
	 *             if no driver is set-up, or there is some problem in getting a
	 *             connection
	 */
	Connection getConnection(String schema) throws SQLException;
}
