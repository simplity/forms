// SPDX-License-Identifier: MIT
package org.simplity.fm.core.infra.defalt;

import java.sql.Connection;
import java.sql.SQLException;

import org.simplity.fm.core.ApplicationError;
import org.simplity.fm.core.infra.DbConnectionFactory;

/**
 * @author simplity.org
 *
 */
public class DefunctDbConFactory implements DbConnectionFactory {
	private static final String ERROR = "No db connection factory is set for this app. No DB operations are possible";

	@Override
	public Connection getConnection() throws SQLException {
		throw new ApplicationError(ERROR);
	}

	@Override
	public Connection getConnection(final String schema) throws SQLException {
		throw new ApplicationError(ERROR);
	}

}
