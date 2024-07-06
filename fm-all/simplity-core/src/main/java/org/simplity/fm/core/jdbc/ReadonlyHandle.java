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

import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.simplity.fm.core.data.DataTable;
import org.simplity.fm.core.data.Record;
import org.simplity.fm.core.db.DbUtil;
import org.simplity.fm.core.db.IReadonlyHandle;
import org.simplity.fm.core.db.IRecordProcessor;
import org.simplity.fm.core.db.IRowProcessor;
import org.simplity.fm.core.db.StoredProcedureResult;
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

	private static final Object[][] EMPTY_ARRAY = new Object[0][];
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

	@Override
	public boolean readIntoRecord(final String sql, final Record inputRecord,
			final Record outputRecord) throws SQLException {

		try (PreparedStatement ps = this.con.prepareStatement(sql)) {
			if (inputRecord != null) {
				DbUtil.setPsParamValues(ps, inputRecord);
			}
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					DbUtil.rsToRecord(rs, outputRecord);
					return true;
				}
				return false;
			}
		}
	}

	@Override
	public boolean readIntoRecord(String sql, Object[] parameterValues,
			ValueType[] parameterTypes, Record outputRecord)
			throws SQLException {

		try (PreparedStatement ps = this.con.prepareStatement(sql)) {
			if (parameterValues != null) {
				DbUtil.setPsParamValues(ps, parameterValues, parameterTypes);
			}
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					DbUtil.rsToRecord(rs, outputRecord);
					return true;
				}
				return false;
			}
		}
	}

	@Override
	public Object[][] readMany(final String sql, final Object[] parameterValues,
			final ValueType[] parameterTypes, final ValueType[] outputTypes)
			throws SQLException {

		try (PreparedStatement ps = this.con.prepareStatement(sql)) {
			if (parameterValues != null) {
				DbUtil.setPsParamValues(ps, parameterValues, parameterTypes);
			}

			try (ResultSet rs = ps.executeQuery()) {
				return DbUtil.getRowsFromRs(rs, outputTypes)
						.toArray(EMPTY_ARRAY);
			}
		}
	}

	@Override
	public Object[][] readMany(final String sql, final Record inputRecord,
			final ValueType[] outputTypes) throws SQLException {

		try (PreparedStatement ps = this.con.prepareStatement(sql)) {
			if (inputRecord != null) {
				DbUtil.setPsParamValues(ps, inputRecord);
			}

			try (ResultSet rs = ps.executeQuery()) {
				return DbUtil.getRowsFromRs(rs, outputTypes)
						.toArray(EMPTY_ARRAY);
			}
		}
	}

	@Override
	public <T extends Record> void readIntoDataTable(String sql,
			Record inputRecord, DataTable<T> outputTable) throws SQLException {
		try (PreparedStatement ps = this.con.prepareStatement(sql)) {
			if (inputRecord != null) {
				DbUtil.setPsParamValues(ps, inputRecord);
			}

			ValueType[] types = outputTable.fetchValueTypes();
			try (ResultSet rs = ps.executeQuery()) {
				DbUtil.processRowsFromRs(rs, types, row -> {
					outputTable.addRow(row);
					return true;
				});
			}
		}
	}

	@Override
	public <T extends Record> void readIntoDataTable(String sql,
			final Object[] parameterValues, final ValueType[] parameterTypes,
			DataTable<T> outputTable) throws SQLException {

		try (PreparedStatement ps = this.con.prepareStatement(sql)) {
			if (parameterValues != null) {
				DbUtil.setPsParamValues(ps, parameterValues, parameterTypes);
			}

			ValueType[] types = outputTable.fetchValueTypes();
			try (ResultSet rs = ps.executeQuery()) {
				DbUtil.processRowsFromRs(rs, types, row -> {
					outputTable.addRow(row);
					return true;
				});
			}
		}
	}

	@Override
	public int readWithRowProcessor(final String sql,
			final Object[] parameterValues, final ValueType[] parameterTypes,
			final ValueType[] outputTypes, IRowProcessor rowProcessor)
			throws SQLException {

		try (PreparedStatement ps = this.con.prepareStatement(sql)) {
			if (parameterValues != null) {
				DbUtil.setPsParamValues(ps, parameterValues, parameterTypes);
			}

			try (ResultSet rs = ps.executeQuery()) {
				return DbUtil.processRowsFromRs(rs, outputTypes, rowProcessor);
			}
		}

	}

	@Override
	public <T extends Record> void readWithRecordProcessor(final String sql,
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

	@Override
	public StoredProcedureResult readFromSp(String callableSql,
			Object[] parameterValues, ValueType[] parameterTypes,
			ValueType returnedValueType, ValueType[][] outputTypes)
			throws SQLException {
		return this.callStoredProcedure(callableSql, parameterValues,
				parameterTypes, returnedValueType, outputTypes);
	}

	protected StoredProcedureResult callStoredProcedure(String callableSql,
			Object[] parameterValues, ValueType[] parameterTypes,
			ValueType returnedValueType, ValueType[][] outputTypes)
			throws SQLException {
		try (CallableStatement cstmt = con.prepareCall(callableSql);) {

			int startAt = 1;
			if (returnedValueType != null) {
				DbUtil.registerOutputParam(cstmt, 1, returnedValueType);
				startAt = 2;
			}

			if (parameterValues != null) {
				DbUtil.setPsParamValues(cstmt, parameterValues, parameterTypes,
						startAt);
			}

			cstmt.execute();
			int nbrRowsAffected = cstmt.getUpdateCount();

			Object returnedValue = null;
			if (returnedValueType != null) {
				returnedValue = DbUtil.getValueFromCs(cstmt, startAt,
						returnedValueType);
			}

			Object[][][] outputData = null;
			if (outputTypes != null) {
				int nbr = outputTypes.length;
				outputData = new Object[nbr][][];
				for (int i = 0; i < nbr; i++) {
					ValueType[] types = outputTypes[i];
					if (types == null) {
						// this is a write statement
						int nbrRows = cstmt.getUpdateCount();
						if (nbrRows == -1) {
							throw new SQLException(
									"Improper output specification for stored procedure at index "
											+ i
											+ ". ValueTypes are null implying an update operation but -1 is returned by the JDBC driver");
						}
						Object[][] row = {{nbrRows}};
						outputData[i] = row;
						continue;
					}

					try (ResultSet rs = cstmt.getResultSet()) {
						if (rs == null) {
							throw new SQLException(
									"Improper output specification for stored procedure at index "
											+ i
											+ ". ValueTypes are specified but no result set is returned by the JDBC driver");
						}
						outputData[i] = DbUtil.getRowsFromRs(rs, types)
								.toArray(EMPTY_ARRAY);
					}
				}
			}
			return new StoredProcedureResult(returnedValue, nbrRowsAffected,
					outputData);
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
