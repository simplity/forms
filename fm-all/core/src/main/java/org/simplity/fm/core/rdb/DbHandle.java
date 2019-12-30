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

package org.simplity.fm.core.rdb;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.simplity.fm.core.data.PreparedStatementParam;
import org.simplity.fm.core.datatypes.ValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author simplity.org
 *
 */
public class DbHandle {
	private static final Logger logger = LoggerFactory.getLogger(DbHandle.class);
	protected final Connection con;

	/**
	 * to be created by DbDriver ONLY
	 *
	 * @param con
	 * @param readOnly
	 */
	DbHandle(final Connection con) {
		this.con = con;
	}

	/**
	 * Most flexible way to read from db. Caller has full control of what
	 * and how to read.
	 *
	 * @param reader
	 *            instance that wants to read from the database
	 * @return number of rows actually read by the reader.
	 * @throws SQLException
	 *
	 */
	public int read(final IDbReader reader) throws SQLException {
		final String sql = reader.getPreparedStatement();
		if (sql == null) {
			return 0;
		}

		try (PreparedStatement ps = this.con.prepareStatement(sql)) {
			reader.setParams(ps);
			try (ResultSet rs = ps.executeQuery()) {
				int n = 0;
				while (rs.next()) {
					if (reader.readARow(rs) == false) {
						break;
					}
					n++;
				}
				logger.info("{} rows read using read()", n);
				return n;
			}
		}
	}

	/**
	 * lower level API that is very close to the JDBC API for reading one
	 * row from the result of a select query
	 *
	 * @param sql
	 *            a prepared statement
	 * @param whereTypes
	 *            parameter types of where clause. These are the parameters
	 *            set to the prepared statement before getting the result
	 *            set from the prepared statement
	 * @param whereData
	 *            must have the same number of elements as in whereTypes.
	 *            Values to be set to the prepared statement
	 * @param resultTypes
	 *            this array has one element for each parameter expected in
	 *            the output result set. Values are extracted from the
	 *            result set based on these types
	 * @return an array of object values extracted from the result set. null
	 *         if result-set had no rows
	 * @throws SQLException
	 */
	public Object[] read(final String sql, final ValueType[] whereTypes, final Object[] whereData,
			final ValueType[] resultTypes) throws SQLException {
		try (PreparedStatement ps = this.con.prepareStatement(sql)) {
			int posn = 0;
			for (final ValueType vt : whereTypes) {
				final Object val = whereData[posn];
				posn++;
				vt.setPsParam(ps, posn, val);
			}

			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next()) {
					return null;
				}
				final Object[] result = new Object[resultTypes.length];
				for (int i = 0; i < resultTypes.length; i++) {
					result[i] = resultTypes[i].getFromRs(rs, i + 1);
				}
				return result;
			}
		}
	}

	/**
	 * lower level API that is very close to the JDBC API for reading all
	 * rows from the result of a select query
	 *
	 * @param sql
	 *            a prepared statement
	 * @param whereTypes
	 *            parameter types of where clause. These are the parameters
	 *            set to the prepared statement before getting the result
	 *            set from the prepared statement
	 * @param whereData
	 *            must have the same number of elements as in whereTypes.
	 *            Values to be set to the prepared statement
	 * @param resultTypes
	 *            this array has one element for each parameter expected in
	 *            the output result set. Values are extracted from the
	 *            result set based on these types
	 * @return an array of rows from the result set. Each row is an array of
	 *         object values from the result set row.
	 * @throws SQLException
	 */
	public Object[][] readRows(final String sql, final ValueType[] whereTypes, final Object[] whereData,
			final ValueType[] resultTypes) throws SQLException {
		try (PreparedStatement ps = this.con.prepareStatement(sql)) {
			int posn = 0;
			for (final ValueType vt : whereTypes) {
				final Object val = whereData[posn];
				posn++;
				vt.setPsParam(ps, posn, val);
			}

			final List<Object[]> result = new ArrayList<>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					final Object[] row = new Object[resultTypes.length];
					result.add(row);
					for (int i = 0; i < resultTypes.length; i++) {
						row[i] = resultTypes[i].getFromRs(rs, i + 1);
					}
				}
				if (result.size() == 0) {
					return null;
				}
				return result.toArray(new Object[0][]);
			}
		}
	}

	/**
	 * @param writer
	 * @return number of affected rows.
	 * @throws SQLException
	 */
	public int write(final IDbWriter writer) throws SQLException {
		final String sql = writer.getPreparedStatement();
		if (sql == null) {
			logger.warn(
					"writer {} returned null as prepared statement, indicatiing taht it does not want to write.. Opertion skipped.",
					writer.getClass().getName());
			return 0;
		}
		logger.info("SQL:{}", sql);

		try (PreparedStatement ps = this.con.prepareStatement(sql)) {
			int n = 0;
			if (writer.setParams(ps)) {
				n = ps.executeUpdate();
				logger.info("{} rows affected ", n);
			} else {
				logger.warn("call back function returned false and hence the write operaiton is abandoned");
			}
			return n;
		}
	}

	/**
	 * write to the rdbms using a writer
	 *
	 * @param writer
	 * @return number of rows affected
	 * @throws SQLException
	 */
	public int write(final IDbMultipleWriter writer) throws SQLException {

		final String sql = writer.getPreparedStatement();
		if (sql == null) {
			logger.warn(
					"writer {} returned null as prepared statement, indicatiing taht it does not want to write.. Opertion skipped.",
					writer.getClass().getName());
			return 0;
		}
		logger.info("Batch SQL:{}", sql);

		try (PreparedStatement ps = this.con.prepareStatement(sql)) {
			while (true) {
				final boolean hasMore = writer.setParams(ps);
				ps.addBatch();
				if (!hasMore) {
					break;
				}
			}

			final int[] nbrs = ps.executeBatch();
			int n = 0;
			for (final int i : nbrs) {
				/*
				 * some drivers return -1 indicating inability to get nbr rows
				 * affected
				 */
				if (i < 0) {
					n = 1;
					break;
				}
				n += i;
			}
			logger.info("{} rows affected ", n);
			return n;
		}
	}

	/**
	 * @param writer
	 * @param keyColumnName
	 * @param generatedKeys
	 * @return number of affected rows.
	 * @throws SQLException
	 */
	public int insertAndGenerteKeys(final IDbWriter writer, final String keyColumnName, final long[] generatedKeys)
			throws SQLException {
		final String[] keys = { keyColumnName };

		final String sql = writer.getPreparedStatement();
		if (sql == null) {
			logger.warn("Writer returned a null SQL, indicating no action.");
			return 0;
		}
		logger.info("Insert With Key SQL:{}", sql);
		try (PreparedStatement ps = this.con.prepareStatement(sql, keys)) {
			int result = 0;
			if (writer.setParams(ps)) {
				result = ps.executeUpdate();
				if (result > 0) {
					final long id = getGeneratedKey(ps);
					logger.info("Row iinserted with generated key = {}", id);
					generatedKeys[0] = id;
				} else {
					logger.info("{} rows inserted ", result);
				}
			} else {
				logger.warn("Call back function returned false, and hence insert operation is abandoned");
			}
			return result;
		}
	}

	/**
	 * API that is close to the JDBC API for updating/inserting/deleting
	 *
	 * @param sql
	 *            a prepared statement that manipulates data.
	 * @param paramTypes
	 *            type of parameters to be set the prepared statement
	 * @param paramValues
	 *            values to be set to the prepared statement
	 * @return number of affected rows. -1 if the driver was unable to
	 *         determine it
	 * @throws SQLException
	 */
	public int write(final String sql, final ValueType[] paramTypes, final Object[] paramValues) throws SQLException {
		logger.info("Generic Write SQL:{}", sql);

		try (PreparedStatement ps = this.con.prepareStatement(sql)) {
			for (int i = 0; i < paramValues.length; i++) {
				paramTypes[i].setPsParam(ps, i + 1, paramValues[i]);
			}
			final int n = ps.executeUpdate();
			logger.info("{} rows affected ", n);
			return n;
		}
	}

	/**
	 * API that is close to the JDBC API for updating/inserting/deleting
	 *
	 * @param sql
	 *            a prepared statement that manipulates data.
	 * @param params
	 *            parameters to be set the prepared statement
	 * @return number of affected rows. -1 if the driver was unable to
	 *         determine it
	 * @throws SQLException
	 */
	public int write(final String sql, final PreparedStatementParam[] params) throws SQLException {
		logger.info("Generic Write SQL:{}", sql);

		try (PreparedStatement ps = this.con.prepareStatement(sql)) {
			final int posn = 0;
			for (final PreparedStatementParam p : params) {
				p.setPsParam(ps, posn);
			}
			final int n = ps.executeUpdate();
			logger.info("{} rows affected ", n);
			return n;
		}
	}

	/**
	 * API that is close to the JDBC API for updating/inserting/deleting
	 *
	 * @param sql
	 *            a prepared statement that manipulates data.
	 * @param paramTypes
	 *            type of parameters to be set the prepared statement
	 * @param paramValues
	 *            values to be set to the prepared statement. these will be
	 *            executed in a batch
	 * @return number of affected rows, on element per batch. -1 implies
	 *         that the driver was unable to determine it
	 * @throws SQLException
	 */
	public int[] writeMany(final String sql, final ValueType[] paramTypes, final Object[][] paramValues)
			throws SQLException {
		logger.info("Generic Batch SQL:{}", sql);
		try (PreparedStatement ps = this.con.prepareStatement(sql)) {
			for (final Object[] row : paramValues) {
				// ps.addBatch();
				for (int i = 0; i < paramTypes.length; i++) {
					paramTypes[i].setPsParam(ps, i + 1, row[i]);
				}
				ps.addBatch();
			}
			return ps.executeBatch();
		}
	}

	/**
	 *
	 * @return blob object
	 * @throws SQLException
	 */
	public Clob createClob() throws SQLException {
		return this.con.createClob();
	}

	/**
	 *
	 * @return blob object
	 * @throws SQLException
	 */
	public Blob createBlob() throws SQLException {
		return this.con.createBlob();
	}

	private static long getGeneratedKey(final PreparedStatement ps) throws SQLException {
		try (ResultSet rs = ps.getGeneratedKeys()) {
			if (rs.next()) {
				return rs.getLong(1);
			}
			throw new SQLException("Driver failed to return a generated key ");
		}
	}

	static void warn(final String sql, final ValueType[] types, final Object[] vals) {
		final StringBuilder sbf = new StringBuilder();
		sbf.append("RDBMS is not set up. Sql = ").append(sql);
		for (int i = 0; i < types.length; i++) {
			sbf.append('(').append(types[i]).append(", ").append(vals[i]).append(") ");
		}
		logger.warn(sbf.toString());
	}

	static void warn(final String sql) {
		logger.error("RDBMS is not set up. Sql = ", sql);
	}

}
