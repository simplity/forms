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

import org.simplity.fm.core.data.Record;
import org.simplity.fm.core.db.DbUtil;
import org.simplity.fm.core.db.IReadonlyHandle;
import org.simplity.fm.core.db.IRecordProcessor;
import org.simplity.fm.core.db.IRowProcessor;
import org.simplity.fm.core.valueschema.ValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Db Handle that allows read access to the underlying RDBMS. No writes are
 * allowed.
 *
 * @author simplity.org
 *
 */
public class ReadonlyHandle implements IReadonlyHandle {
	private static final Logger logger = LoggerFactory
			.getLogger(ReadonlyHandle.class);
	protected final Connection con;

	/**
	 * to be created by DbDriver ONLY
	 *
	 * @param con
	 * @param readOnly
	 */
	ReadonlyHandle(final Connection con) {
		this.con = con;
	}

	@Override
	public boolean read(final String sql, final Record inputData,
			final Record outputData) throws SQLException {
		try (PreparedStatement ps = this.con.prepareStatement(sql)) {
			if (inputData != null) {
				DbUtil.setPsParamValues(ps, inputData);
			}
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					DbUtil.rsToRecord(rs, outputData);
					return true;
				}
				return false;
			}
		}
	}

	@Override
	public Object[] read(final String sql, final Object[] parameterValues,
			ValueType[] parameterTypes, final ValueType[] outputTypes)
			throws SQLException {
		try (PreparedStatement ps = this.con.prepareStatement(sql)) {
			DbUtil.setPsParamValues(ps, parameterValues, parameterTypes);
			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next()) {
					return null;
				}
				return DbUtil.getValuesFromRs(rs, outputTypes);
			}
		}
	}

	/**
	 * method to be used to read into a valueObject using a sql component.
	 *
	 * @param sql
	 *            non-null valid prepared statement to read from the database
	 * @param parameterValues
	 *            null if the prepared statement has no parameters. must contain
	 *            the right non-values in the right order for parameters in the
	 *            select sql
	 * @param outputTypes
	 *            non-null. must have the right types in the right order to
	 *            receive data from the result set
	 * @return extracted data as an array of rows. null if no row is read
	 * @throws SQLException
	 */
	@Override
	public Object[][] readMany(final String sql, final Object[] parameterValues,
			final ValueType[] parameterTypes, final ValueType[] outputTypes)
			throws SQLException {
		try (PreparedStatement ps = this.con.prepareStatement(sql)) {
			if (parameterValues != null) {
				DbUtil.setPsParamValues(ps, parameterValues, parameterTypes);
			}

			try (ResultSet rs = ps.executeQuery()) {
				final List<Object[]> result = new ArrayList<>();
				while (rs.next()) {
					result.add(DbUtil.getValuesFromRs(rs, outputTypes));
				}
				if (result.size() == 0) {
					return null;
				}
				return result.toArray(new Object[0][]);
			}
		}
	}

	@Override
	public int readMany(final String sql, final Object[] paramValues,
			final ValueType[] parameterTypes, final ValueType[] outputTypes,
			IRowProcessor rowProcessor) throws SQLException {
		try (PreparedStatement ps = this.con.prepareStatement(sql)) {
			if (paramValues != null) {
				DbUtil.setPsParamValues(ps, paramValues, parameterTypes);
			}

			try (ResultSet rs = ps.executeQuery()) {
				int nbr = 0;
				while (rs.next()) {
					final boolean toContinue = rowProcessor
							.process(DbUtil.getValuesFromRs(rs, outputTypes));
					nbr++;

					if (!toContinue) {
						break;
					}
				}
				return nbr;
			}
		}

	}

	@Override
	public <T extends Record> void readMany(final String sql,
			final Record inputRecord, T instanceToClone,
			final IRecordProcessor<T> processor) throws SQLException {
		try (PreparedStatement ps = this.con.prepareStatement(sql)) {
			if (inputRecord != null) {
				DbUtil.setPsParamValues(ps, inputRecord);
			}

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					@SuppressWarnings("unchecked")
					final T record = (T) instanceToClone.newInstance();
					DbUtil.rsToRecord(rs, record);
					processor.process(record);
				}
			}
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

	protected static void warn(final String sql, final ValueType[] types,
			final Object[] vals) {
		final StringBuilder sbf = new StringBuilder();
		sbf.append("RDBMS is not set up. Sql = ").append(sql);
		for (int i = 0; i < types.length; i++) {
			sbf.append('(').append(types[i]).append(", ").append(vals[i])
					.append(") ");
		}
		logger.warn(sbf.toString());
	}

	protected static void warn(final String sql) {
		logger.error("RDBMS is not set up. Sql = ", sql);
	}
}
