// SPDX-License-Identifier: MIT
package org.simplity.fm.core.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import org.simplity.fm.core.db.TransactionHandle;

/**
 * db handle that allows multiple transactions.
 *
 * @author simplity.org
 *
 */
public class JdbcTransactionHandle extends JdbcReadWriteHandle
		implements
			TransactionHandle {

	/**
	 * @param con
	 */
	JdbcTransactionHandle(final Connection con) {
		super(con);
	}

	@Override
	public void setAutoCommitMode(final boolean mode) throws SQLException {
		this.con.setAutoCommit(mode);
	}

	@Override
	public void commit() throws SQLException {
		this.con.commit();
	}

	@Override
	public void rollback() throws SQLException {
		this.con.rollback();
	}

}
