// SPDX-License-Identifier: MIT
package org.simplity.fm.core.db;

import java.sql.SQLException;

/**
 * interface/lambda for carrying out read-only operations on RDBMS. This
 * interface is created instead of using standard functions because of the
 * allowed throws clause
 *
 * @author simplity.org
 *
 */
@FunctionalInterface
public interface DbReader {

	/**
	 * function that reads data from the db
	 *
	 * @param handle
	 * @return true if all ok, false otherwise. Exact meaning of what is allOK is
	 *         left to the individual implementation
	 * @throws SQLException
	 */
	boolean read(ReadonlyHandle handle) throws SQLException;

}
