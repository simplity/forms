// SPDX-License-Identifier: MIT
package org.simplity.fm.core.db;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * interface/lambda to carry out read operations with the jdbc object
 * DatabaseMetadata.
 *
 *
 * @author simplity.org
 *
 */
public interface DbMetaDataReader {

	/**
	 * function that accesses the DatabaseMetaData instance for this database
	 * transaction is managed by the called driver, and not this function.
	 *
	 * @param metaData
	 * @return true if all ok, false otherwise. Exact meaning of what is allOK is
	 *         left to the individual implementation
	 * @throws SQLException
	 */
	boolean read(DatabaseMetaData metaData) throws SQLException;
}
