// SPDX-License-Identifier: MIT
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
import org.simplity.fm.core.db.SpOutputProcessor;
import org.simplity.fm.core.db.ReadonlyHandle;
import org.simplity.fm.core.db.RecordProcessor;
import org.simplity.fm.core.db.RowProcessor;
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
public class JdbcReadonlyHandle implements ReadonlyHandle {
	private static final Logger logger = LoggerFactory.getLogger(JdbcReadonlyHandle.class);

	@SuppressWarnings("resource")
	protected final Connection con;

	/**
	 * to be created by DbDriver ONLY
	 *
	 * @param con
	 * @param readOnly
	 */
	JdbcReadonlyHandle(final Connection con) {
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
			final ValueType[] outputTypes, RowProcessor rowProcessor) throws SQLException {

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
			T instanceToClone, final RecordProcessor<T> processor) throws SQLException {

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
			ValueType returnedValueType, SpOutputProcessor fn) throws SQLException {
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
			SpOutputProcessor fn) throws SQLException {
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
