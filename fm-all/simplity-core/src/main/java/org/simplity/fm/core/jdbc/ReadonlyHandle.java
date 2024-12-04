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
import java.util.List;

import org.simplity.fm.core.data.DataTable;
import org.simplity.fm.core.data.Record;
import org.simplity.fm.core.db.DbUtil;
import org.simplity.fm.core.db.IProcessSpOutput;
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
	private static final Logger logger = LoggerFactory.getLogger(ReadonlyHandle.class);

	@SuppressWarnings("resource")
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
	public boolean read(final String sql, final Object[] parameterValues, ValueType[] parameterTypes,
			final ValueType[] outputTypes, Object[] outputData) throws SQLException {

		try (PreparedStatement ps = this.con.prepareStatement(sql)) {
			DbUtil.setPsParamValues(ps, parameterValues, parameterTypes);
			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next()) {
					return false;
				}
				return DbUtil.getValuesFromRs(rs, outputTypes, outputData);
			}
		}
	}

	@Override
	public boolean readIntoRecord(final String sql, final Record inputRecord, final Record outputRecord)
			throws SQLException {

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

//	@Override
//	public boolean readIntoRecord(String sql, Object[] parameterValues, ValueType[] parameterTypes, Record outputRecord)
//			throws SQLException {
//
//		try (PreparedStatement ps = this.con.prepareStatement(sql)) {
//			if (parameterValues != null) {
//				DbUtil.setPsParamValues(ps, parameterValues, parameterTypes);
//			}
//			try (ResultSet rs = ps.executeQuery()) {
//				if (rs.next()) {
//					DbUtil.rsToRecord(rs, outputRecord);
//					return true;
//				}
//				return false;
//			}
//		}
//	}

	@Override
	public int readMany(final String sql, final Object[] parameterValues, final ValueType[] parameterTypes,
			final ValueType[] outputTypes, List<Object[]> outputData) throws SQLException {

		try (PreparedStatement ps = this.con.prepareStatement(sql)) {
			if (parameterValues != null) {
				DbUtil.setPsParamValues(ps, parameterValues, parameterTypes);
			}

			try (ResultSet rs = ps.executeQuery()) {
				return DbUtil.getRowsFromRs(rs, outputTypes, outputData);
			}
		}
	}

//	@Override
//	public int readMany(final String sql, final Record inputRecord, final ValueType[] outputTypes, List<Object[]> rows)
//			throws SQLException {
//
//		try (PreparedStatement ps = this.con.prepareStatement(sql)) {
//			if (inputRecord != null) {
//				DbUtil.setPsParamValues(ps, inputRecord);
//			}
//
//			try (ResultSet rs = ps.executeQuery()) {
//				return DbUtil.getRowsFromRs(rs, outputTypes).toArray(EMPTY_ARRAY);
//			}
//		}
//	}
//
	@Override
	public <T extends Record> void readIntoDataTable(String sql, Record inputRecord, DataTable<T> outputTable)
			throws SQLException {
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
	public <T extends Record> void readIntoDataTable(String sql, final Object[] parameterValues,
			final ValueType[] parameterTypes, DataTable<T> outputTable) throws SQLException {

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
	public int readWithRowProcessor(final String sql, final Object[] parameterValues, final ValueType[] parameterTypes,
			final ValueType[] outputTypes, IRowProcessor rowProcessor) throws SQLException {

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
	public <T extends Record> void readWithRecordProcessor(final String sql, final Record inputRecord,
			T instanceToClone, final IRecordProcessor<T> processor) throws SQLException {

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
	public Object callStoredProcedure(String callableSql, Object[] parameterValues, ValueType[] parameterTypes,
			ValueType returnedValueType, IProcessSpOutput fn) throws SQLException {
		try (CallableStatement cstmt = this.con.prepareCall(callableSql);) {

			int startAt = 1;
			if (returnedValueType != null) {
				DbUtil.registerOutputParam(cstmt, 1, returnedValueType);
				startAt = 2;
			}

			if (parameterValues != null) {
				DbUtil.setPsParamValues(cstmt, parameterValues, parameterTypes, startAt);
			}

			boolean hasResult = cstmt.execute();
			while (hasResult) {
				try (ResultSet rs = cstmt.getResultSet()) {
					int updateCount = cstmt.getUpdateCount();
					boolean toContinue = fn.nextResult(rs, updateCount);
					if (toContinue == false) {
						break;
					}
					hasResult = cstmt.getMoreResults();
				}
			}

			if (returnedValueType == null) {
				return null;
			}
			return DbUtil.getValueFromCs(cstmt, 1, returnedValueType);
		}

	}

	@Override
	public Object callStoredProcedure(String callableSql, Record inRec, ValueType returnedValueType,
			IProcessSpOutput fn) throws SQLException {
		return this.callStoredProcedure(callableSql, inRec.fetchRawData(), inRec.fetchValueTypes(), returnedValueType,
				fn);

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

	protected static void warn(final String sql, final ValueType[] types, final Object[] vals) {
		final StringBuilder sbf = new StringBuilder();
		sbf.append("RDBMS is not set up. Sql = ").append(sql);
		for (int i = 0; i < types.length; i++) {
			sbf.append('(').append(types[i]).append(", ").append(vals[i]).append(") ");
		}
		logger.warn(sbf.toString());
	}

	protected static void warn(final String sql) {
		logger.error("RDBMS is not set up. Sql = ", sql);
	}
}
