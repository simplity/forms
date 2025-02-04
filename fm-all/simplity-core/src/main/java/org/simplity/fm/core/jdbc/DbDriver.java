/*
 * Copyright (c) 2019 simplity.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.simplity.fm.core.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import org.simplity.fm.core.db.IDbDriver;
import org.simplity.fm.core.db.IDbMetaDataReader;
import org.simplity.fm.core.db.IDbReader;
import org.simplity.fm.core.db.IDbTransacter;
import org.simplity.fm.core.db.IDbWriter;
import org.simplity.fm.core.db.IReadWriteHandle;
import org.simplity.fm.core.db.IReadonlyHandle;
import org.simplity.fm.core.db.ITransactionHandle;
import org.simplity.fm.core.infra.IDbConnectionFactory;
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
public class DbDriver implements IDbDriver {
	protected static final Logger logger = LoggerFactory.getLogger(DbDriver.class);

	private final IDbConnectionFactory factory;

	/**
	 * to be used by APP, and no one else..
	 *
	 * @param factory
	 */
	public DbDriver(final IDbConnectionFactory factory) {
		this.factory = factory;
		//
	}

	@Override
	public boolean doReadonlyOperations(final IDbReader reader) throws SQLException {
		this.checkFactory();
		try (Connection con = this.factory.getConnection()) {
			return doReadOnly(con, reader);
		}
	}

	@Override
	public boolean doReadonlyOperations(final String schemaName, final IDbReader reader) throws SQLException {
		this.checkFactory();
		try (Connection con = this.factory.getConnection(schemaName)) {
			return doReadOnly(con, reader);
		}
	}

	@Override
	public boolean doReadMetaData(IDbMetaDataReader reader) throws SQLException {
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
	public boolean doReadMetaData(String schemaName, IDbMetaDataReader reader) throws SQLException {
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
	public boolean doReadWriteOperations(final IDbWriter updater) throws SQLException {
		this.checkFactory();
		try (Connection con = this.factory.getConnection()) {
			return doReadWrite(con, updater);
		}
	}

	@Override
	public boolean doReadWriteOperations(final String schemaName, final IDbWriter updater) throws SQLException {
		this.checkFactory();
		try (Connection con = this.factory.getConnection(schemaName)) {
			return doReadWrite(con, updater);
		}
	}

	@Override
	public boolean doMultipleTransactions(final IDbTransacter transacter) throws SQLException {
		this.checkFactory();
		try (Connection con = this.factory.getConnection()) {
			return doTransact(con, transacter);
		}
	}

	@Override
	public boolean doMultipleTransactions(final String schemaName, final IDbTransacter transacter) throws SQLException {
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

	private static boolean doReadOnly(final Connection con, final IDbReader reader) throws SQLException {

		final IReadonlyHandle handle = new ReadonlyHandle(con);
		try {
			con.setReadOnly(true);
			return reader.read(handle);
		} catch (final Exception e) {
			e.printStackTrace();
			logger.error("Exception occurred in the middle of a transaction: {}, {}", e, e.getMessage());
			throw new SQLException(e.getMessage());
		}
	}

	private static boolean doReadWrite(final Connection con, final IDbWriter updater) throws SQLException {
		final IReadWriteHandle handle = new ReadWriteHandle(con);
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

	private static boolean doTransact(final Connection con, final IDbTransacter transacter) throws SQLException {
		final ITransactionHandle handle = new TransactionHandle(con);
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