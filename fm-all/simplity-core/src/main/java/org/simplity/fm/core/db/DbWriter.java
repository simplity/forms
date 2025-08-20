// SPDX-License-Identifier: MIT
package org.simplity.fm.core.db;

import java.sql.SQLException;

/**
 * interface/lambda to carry out read-write operation under a transaction that
 * is managed by the driver.
 * This interface is created instead of using standard functions in java.util
 * because the function needs to throw an exception
 *
 *
 * @author simplity.org
 *
 */
public interface DbWriter {

	/**
	 * function that accesses the db within a transaction boundary. The
	 * transaction is managed by the called driver, and not this function.
	 *
	 * @param handle
	 * @return true if all OK. false in case you detect some condition because
	 *         of which the transaction is to be cancelled.
	 * @throws SQLException
	 */
	boolean readWrite(ReadWriteHandle handle) throws SQLException;

}
