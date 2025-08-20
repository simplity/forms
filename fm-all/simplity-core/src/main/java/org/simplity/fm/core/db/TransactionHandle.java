// SPDX-License-Identifier: MIT
package org.simplity.fm.core.db;

import java.sql.SQLException;

/**
 * db handle that allows multiple transactions.
 *
 * @author simplity.org
 *
 */
public interface TransactionHandle extends ReadWriteHandle {

	/**
	 * turn on/off auto commit mode. If it is on, commit/roll-backs are not
	 * valid
	 *
	 * @param mode
	 * @throws SQLException
	 */
	public void setAutoCommitMode(final boolean mode) throws SQLException;

	/**
	 * commit all write operations after the last commit/roll-back
	 *
	 * @throws SQLException
	 */
	public void commit() throws SQLException;

	/**
	 * roll back any writes. This is to be used only to handle any exception. We
	 * strongly suggest that this should never be called by design.
	 *
	 * @throws SQLException
	 */
	public void rollback() throws SQLException;

}
