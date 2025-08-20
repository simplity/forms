// SPDX-License-Identifier: MIT
package org.simplity.fm.core.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import org.simplity.fm.core.db.DbDriver;
import org.simplity.fm.core.db.DbMetaDataReader;
import org.simplity.fm.core.db.DbReader;
import org.simplity.fm.core.db.DbTransacter;
import org.simplity.fm.core.db.DbWriter;
import org.simplity.fm.core.db.ReadWriteHandle;
import org.simplity.fm.core.db.ReadonlyHandle;
import org.simplity.fm.core.db.TransactionHandle;
import org.simplity.fm.core.infra.DbConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Driver to deal with RDBMS read/write operations. Note that we expose
 * much-higher level APIs that the JDBC driver. And, of course we provide the
 * very basic feature : read/write. That is the whole idea of this class -
 * provide simple API to do the most common operation
 *
 * This is an immutable class, and hence can be used as a singleton. This is
 * designed to be accessed through App
 *
 * @author simplity.org
 *
 */
public class JdbcDriver implements DbDriver {
	protected static final Logger logger = LoggerFactory.getLogger(JdbcDriver.class);

	private final DbConnectionFactory factory;

	/**
	 * to be used by APP, and no one else..
	 *
	 * @param factory
	 */
	public JdbcDriver(final DbConnectionFactory factory) {
		this.factory = factory;
		//
	}

	@Override
	public boolean doReadonlyOperations(final DbReader reader) throws SQLException {
		this.checkFactory();
		try (Connection con = this.factory.getConnection()) {
			return doReadOnly(con, reader);
		}
	}

	@Override
	public boolean doReadonlyOperations(final String schemaName, final DbReader reader) throws SQLException {
		this.checkFactory();
		try (Connection con = this.factory.getConnection(schemaName)) {
			return doReadOnly(con, reader);
		}
	}

	@Override
	public boolean doReadMetaData(DbMetaDataReader reader) throws SQLException {
		this.checkFactory();
		try (Connection con = this.factory.getConnection()) {
			try {
				return reader.read(con.getMetaData());
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("Exception occurred in the middle of a transaction: {}, {}", e, e.getMessage());
				throw new SQLException(e.getMessage());
			}
		}
	}

	@Override
	public boolean doReadMetaData(String schemaName, DbMetaDataReader reader) throws SQLException {
		this.checkFactory();
		try (Connection con = this.factory.getConnection(schemaName)) {
			try {
				return reader.read(con.getMetaData());
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("Exception occurred in the middle of a transaction: {}, {}", e, e.getMessage());
				throw new SQLException(e.getMessage());
			}
		}
	}

	/**
	 * do read-write operations on the rdbms within a transaction boundary. The
	 * transaction is managed by the driver.
	 *
	 * @param updater function that reads from db and writes to it within a
	 *                transaction boundary. returns true to commit the transaction,
	 *                or false to signal a roll-back. The transaction is rolled back
	 *                on exceptions as well.
	 * @throws SQLException
	 */
	@Override
	public boolean doReadWriteOperations(final DbWriter updater) throws SQLException {
		this.checkFactory();
		try (Connection con = this.factory.getConnection()) {
			return doReadWrite(con, updater);
		}
	}

	@Override
	public boolean doReadWriteOperations(final String schemaName, final DbWriter updater) throws SQLException {
		this.checkFactory();
		try (Connection con = this.factory.getConnection(schemaName)) {
			return doReadWrite(con, updater);
		}
	}

	@Override
	public boolean doMultipleTransactions(final DbTransacter transacter) throws SQLException {
		this.checkFactory();
		try (Connection con = this.factory.getConnection()) {
			return doTransact(con, transacter);
		}
	}

	@Override
	public boolean doMultipleTransactions(final String schemaName, final DbTransacter transacter) throws SQLException {
		this.checkFactory();
		try (Connection con = this.factory.getConnection(schemaName)) {
			return doTransact(con, transacter);
		}
	}

	private void checkFactory() throws SQLException {
		if (this.factory == null) {
			final String msg = "Db driver is not set up for this application. No db operations are possible";
			logger.error(msg);
			throw new SQLException(msg);
		}
	}

	private static boolean doReadOnly(final Connection con, final DbReader reader) throws SQLException {

		final ReadonlyHandle handle = new JdbcReadonlyHandle(con);
		try {
			con.setReadOnly(true);
			return reader.read(handle);
		} catch (final Exception e) {
			e.printStackTrace();
			logger.error("Exception occurred in the middle of a transaction: {}, {}", e, e.getMessage());
			throw new SQLException(e.getMessage());
		}
	}

	private static boolean doReadWrite(final Connection con, final DbWriter updater) throws SQLException {
		final ReadWriteHandle handle = new JdbcReadWriteHandle(con);
		try {
			con.setAutoCommit(false);
			if (updater.readWrite(handle)) {
				con.commit();
				return true;
			}
			con.rollback();
			return false;

		} catch (final Exception e) {
			e.printStackTrace();
			logger.error("Exception occurred in the middle of a transaction: {}, {}", e, e.getMessage());
			try {
				con.rollback();
			} catch (final Exception ignore) {
				//
			}
			throw new SQLException(e.getMessage());
		}
	}

	private static boolean doTransact(final Connection con, final DbTransacter transacter) throws SQLException {
		final TransactionHandle handle = new JdbcTransactionHandle(con);
		try {
			return transacter.transact(handle);
		} catch (final Exception e) {
			e.printStackTrace();
			logger.error("Exception thrown by a batch processor. {}, {}", e, e.getMessage());
			final SQLException se = new SQLException(e.getMessage());
			try {
				con.rollback();
			} catch (final Exception ignore) {
				//
			}
			throw se;
		}

	}
}