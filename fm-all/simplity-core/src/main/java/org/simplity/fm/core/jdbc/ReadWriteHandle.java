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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.simplity.fm.core.ApplicationError;
import org.simplity.fm.core.data.DataTable;
import org.simplity.fm.core.data.Record;
import org.simplity.fm.core.db.DbUtil;
import org.simplity.fm.core.db.IReadWriteHandle;
import org.simplity.fm.core.valueschema.ValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author simplity.org
 *
 */
public class ReadWriteHandle extends ReadonlyHandle
		implements
			IReadWriteHandle {
	private static final Logger logger = LoggerFactory
			.getLogger(ReadWriteHandle.class);

	/**
	 * to be created by DbDriver ONLY
	 *
	 * @param con
	 * @param readOnly
	 */
	ReadWriteHandle(final Connection con) {
		super(con);
	}

	@Override
	public int writeFromRecord(final String sql, final Record inputRecord)
			throws SQLException {
		try (PreparedStatement ps = this.con.prepareStatement(sql)) {
			DbUtil.setPsParamValues(ps, inputRecord);
			return ps.executeUpdate();
		}
	}

	@Override
	public int write(final String sql, final Object[] parameterValues,
			ValueType[] parameterTypes) throws SQLException {
		logger.info("Generic Write SQL:{}", sql);
		try (PreparedStatement ps = this.con.prepareStatement(sql)) {
			DbUtil.setPsParamValues(ps, parameterValues, parameterTypes);
			return ps.executeUpdate();
		}
	}

	@Override
	public int insertWithKeyGeneration(final String sql,
			final Object[] parameterValues, ValueType[] parameterTypes,
			String generatedColumnName, long[] generatedKeys)
			throws SQLException {
		logger.info("Generic Write SQL:{}", sql);
		try (PreparedStatement ps = this.con.prepareStatement(sql)) {
			DbUtil.setPsParamValues(ps, parameterValues, parameterTypes);
			final int n = ps.executeUpdate();
			if (n > 0) {
				generatedKeys[0] = getGeneratedKey(ps);
			}
			return n;
		}
	}

	@Override
	public <T extends Record> int writeFromDataTable(final String sql,
			final DataTable<T> dataTable) throws SQLException {
		logger.info("Generic Batch SQL:{}", sql);

		try (PreparedStatement ps = this.con.prepareStatement(sql)) {
			for (T record : dataTable) {
				DbUtil.setPsParamValues(ps, record);
				ps.addBatch();
			}
			return accumulate(ps.executeBatch());
		}
	}

	@Override
	public int writeMany(final String sql, final Object[][] parameterValues,
			ValueType[] parameterTypes) throws SQLException {
		logger.info("Generic Batch SQL:{}", sql);
		try (PreparedStatement ps = this.con.prepareStatement(sql)) {
			for (final Object[] row : parameterValues) {
				DbUtil.setPsParamValues(ps, row, parameterTypes);
				ps.addBatch();
			}
			return accumulate(ps.executeBatch());
		}
	}

	@Override
	public int insertWithKeyGenerations(final String sql,
			final Object[][] rowsToInsert, ValueType[] parameterTypes,
			String generatedColumnName, long[] generatedKeys)
			throws SQLException {
		logger.info("Generic Write SQL:{}", sql);
		try (PreparedStatement ps = this.con.prepareStatement(sql)) {
			for (final Object[] row : rowsToInsert) {
				DbUtil.setPsParamValues(ps, row, parameterTypes);
				ps.addBatch();
			}

			int[] arr = ps.executeBatch();

			int nbrRows = rowsToInsert.length;
			if (generatedKeys.length != nbrRows) {
				throw new ApplicationError(nbrRows
						+ " are to be inserted but generated keys arrays specified has a length of only "
						+ generatedKeys.length);
			}
			getGeneratedKeys(ps, generatedKeys);
			return accumulate(arr);
		}
	}

	/**
	 * the array of counts returned by the driver may contain -1 as value
	 *
	 * @param counts
	 * @return
	 */
	private static int accumulate(final int[] counts) {
		int n = 0;
		for (final int i : counts) {
			/*
			 * some drivers return -1 indicating inability to get nbr rows
			 * affected
			 */
			if (i < 0) {
				logger.warn(
						"Driver returned -1 as number of rows affected for a batch. assumed to be 1");
				n++;
			} else {
				n += i;
			}
		}
		logger.info("{} rows affected ", n);
		return n;
	}

	private static long getGeneratedKey(final PreparedStatement ps)
			throws SQLException {
		try (ResultSet rs = ps.getGeneratedKeys()) {
			if (rs.next()) {
				return rs.getLong(1);
			}
			throw new SQLException("Driver failed to return a generated key ");
		}
	}

	private static void getGeneratedKeys(final PreparedStatement ps,
			long[] keys) throws SQLException {
		int idx = 0;
		int n = keys.length;
		try (ResultSet rs = ps.getGeneratedKeys()) {
			while (rs.next()) {
				if (idx == n) {
					throw new SQLException("Bulk insert inserted " + n
							+ " rows but generated more keys!!");
				}
				keys[idx] = rs.getLong(1);
				idx++;
			}
		}
	}

}
