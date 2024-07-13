package org.simplity.fm.core.db;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.simplity.fm.core.data.DataTable;
import org.simplity.fm.core.data.Record;
import org.simplity.fm.core.valueschema.ValueType;

/**
 * Represents a Stored Procedure of an RDBMS.
 *
 * 1. All the parameters for the procedure are to be Input. That is no Output or
 * In-out parameters. (this feature will be developed on a need basis)
 *
 * 2. return value, if any, can only be a simple value. complex structures like
 * arrays and tables are not handled.
 *
 * 3. procedure can output one or more result sets
 *
 *
 * If the SP is used like a normal sql, that is, returned value is not used, and
 * no multiple outputs, then the APIs are identical to those for regular sql
 * statement.
 *
 * @author org.simplity
 *
 */
public abstract class StoredProcedure extends Sql implements IProcessSpOutput {

	/**
	 * additional fields required for SP
	 */

	/**
	 * unique name across all defined procedures
	 */
	protected final String procedureName;

	/**
	 * value type of the value being returned by this procedure. null if no
	 * value is returned
	 */
	protected final ValueType returnedType;

	/**
	 * to be used only if there are more than one out puts. In this case,
	 * outputRecord/outputTypes must be null.
	 *
	 * Each array element must be designed to receive rows from the
	 * corresponding output. An element is left as null if that output is a
	 * non-select sql
	 */
	protected final DataTable<?>[] outputTables;
	protected final int[] uodateCounts;
	protected int outputIdx;

	protected StoredProcedure(String procedureName, ValueType returnedType,
			Class<?>[] outputTableClasses, final String[] parameterNames,
			final ValueType[] parameterTypes, final ValueType[] outputTypes)
			throws SQLException {
		super(makeSql(procedureName, returnedType, parameterNames),
				parameterNames, parameterTypes, outputTypes);
		this.procedureName = procedureName;
		this.returnedType = returnedType;
		if (outputTableClasses == null) {
			this.outputTables = null;
			this.uodateCounts = null;
		} else {
			this.outputTables = getOutputTables(outputTableClasses);;
			this.uodateCounts = new int[outputTableClasses.length];
		}

	}

	private static DataTable<?>[] getOutputTables(Class<?>[] classes)
			throws SQLException {
		DataTable<?>[] tables = new DataTable<?>[classes.length];
		for (int i = 0; i < tables.length; i++) {
			Class<?> cls = classes[i];
			if (cls == null) {
				continue;
			}

			Record record = null;
			try {
				record = (Record) cls.getDeclaredConstructor().newInstance();
			} catch (Exception e) { // will throw sqlException later}
				if (record == null) {
					throw new SQLException("Class " + cls.getName()
							+ " failed to create an instance of a Record");
				}
			}
		}
		return tables;
	}
	/**
	 * {?= proc_name(?,?....)}
	 */
	private static String makeSql(String procName, ValueType retType,
			String[] names) {
		StringBuilder sbf = new StringBuilder("{ ");
		if (retType != null) {
			sbf.append("? = ");
		}

		sbf.append("call ").append(procName).append('(');
		if (names != null) {
			int n = names.length;
			while (n > 0) {
				sbf.append("?,"); // we will remove the last comma later
				n--;
			}
			sbf.setLength(sbf.length() - 1);
		}
		sbf.append(")}");
		return sbf.toString();
	}

	/*
	 * we provide methods that mimic the of a simple SQL if the procedure is
	 * doing just that And the most complex one that exposes all the possible
	 * features of a full-blown SP
	 */

	@Override
	protected boolean read(final IReadonlyHandle handle, Record outRec)
			throws SQLException {

		this.checkValues();

		final boolean[] ok = {false};
		handle.callStoredProcedure(this.sqlText, this.parameterValues,
				this.parameterTypes, null, new IProcessSpOutput() {

					@Override
					public boolean nextResult(ResultSet rs, int updateCount)
							throws SQLException {
						if (rs != null) {
							DbUtil.rsToRecord(rs, outRec);
							ok[0] = true;
						}
						return false;
					}
				});
		return ok[0];
	}

	@Override
	protected void readOrFail(final IReadonlyHandle handle, Record outRec)
			throws SQLException {
		if (!this.read(handle, outRec)) {
			throw new SQLException(
					"At least one row was expected, but non found");
		}
	}

	@Override
	protected void readMany(final IReadonlyHandle handle,
			DataTable<Record> dataTable) throws SQLException {
		this.checkValues();
		handle.callStoredProcedure(this.sqlText, parameterValues,
				parameterTypes, null, new IProcessSpOutput() {

					@Override
					public boolean nextResult(ResultSet rs, int updateCount)
							throws SQLException {
						if (rs != null) {
							DbUtil.rsToDataTable(rs, dataTable);
						}
						return false;
					}
				});
	}

	// read methods with input record and output record //

	@Override
	protected boolean read(final IReadonlyHandle handle, Record inRec,
			Record outRec) throws SQLException {

		final boolean[] ok = {false};
		handle.callStoredProcedure(this.sqlText, inRec.fetchRawData(),
				inRec.fetchValueTypes(), null, new IProcessSpOutput() {

					@Override
					public boolean nextResult(ResultSet rs, int updateCount)
							throws SQLException {
						if (rs != null) {
							DbUtil.rsToRecord(rs, outRec);
							ok[0] = true;
						}
						return false;
					}
				});
		return ok[0];
	}

	@Override
	protected void readOrFail(final IReadonlyHandle handle, Record inRec,
			Record outputRecord) throws SQLException {
		if (!this.read(handle, inRec, outputRecord)) {
			throw new SQLException(
					"At least one row was expected, but non found");
		}
	}

	@Override
	protected void readMany(final IReadonlyHandle handle, Record inRec,
			DataTable<Record> dataTable) throws SQLException {

		handle.callStoredProcedure(this.sqlText, inRec.fetchRawData(),
				inRec.fetchValueTypes(), null, new IProcessSpOutput() {

					@Override
					public boolean nextResult(ResultSet rs, int updateCount)
							throws SQLException {
						if (rs != null) {
							DbUtil.rsToDataTable(rs, dataTable);
						}
						return false;
					}
				});
	}

	@Override
	protected boolean readIn(final IReadonlyHandle handle, Record inRec)
			throws SQLException {

		final boolean[] ok = {false};
		final Object[][] values = new Object[1][];
		final ValueType[] vts = this.outputTypes;
		handle.callStoredProcedure(this.sqlText, inRec.fetchRawData(),
				inRec.fetchValueTypes(), null, new IProcessSpOutput() {

					@Override
					public boolean nextResult(ResultSet rs, int updateCount)
							throws SQLException {
						if (rs != null) {
							values[0] = DbUtil.getValuesFromRs(rs, vts);
						}
						return false;
					}
				});
		return ok[0];
	}

	@Override
	protected void readInOrFail(final IReadonlyHandle handle, Record inRec)
			throws SQLException {
		if (!this.readIn(handle, inRec)) {
			throw new SQLException("Expected one row, but none found");
		}
	}

	@Override
	protected boolean readIn(final IReadonlyHandle handle) throws SQLException {

		this.checkValues();

		final ValueType[] outTypes = this.outputTypes;
		final Object[][] outValues = new Object[1][];

		handle.callStoredProcedure(this.sqlText, this.parameterValues,
				this.parameterTypes, null, new IProcessSpOutput() {

					@Override
					public boolean nextResult(ResultSet rs, int updateCount)
							throws SQLException {
						if (rs != null) {
							outValues[0] = DbUtil.getValuesFromRs(rs, outTypes);
						}
						return false;
					}
				});
		Object[] result = outValues[0];
		if (result == null) {
			return false;
		}
		this.outputValues = result;
		return true;
	}

	@Override
	protected void readInOrFail(final IReadonlyHandle handle)
			throws SQLException {
		if (!this.readIn(handle)) {
			throw new SQLException(
					"At least one row was expected, but non found");
		}
	}

	@Override
	protected int write(final IReadWriteHandle handle, Record inRec)
			throws SQLException {

		final int[] counts = {0};
		handle.callStoredProcedure(this.sqlText, inRec.fetchRawData(),
				inRec.fetchValueTypes(), null, new IProcessSpOutput() {

					@Override
					public boolean nextResult(ResultSet rs, int updateCount)
							throws SQLException {
						if (updateCount != -1) {
							counts[0] = updateCount;
						}
						return false;
					}
				});
		return counts[0];
	}

	@Override
	protected int writeOrFail(final IReadWriteHandle handle, Record inRec)
			throws SQLException {
		final int n = this.write(handle, inRec);
		if (n == 0) {
			fail();
		}
		return n;
	}

	@Override
	protected int write(final IReadWriteHandle handle) throws SQLException {
		this.checkValues();

		final int[] counts = {0};
		handle.callStoredProcedure(this.sqlText, this.parameterValues,
				this.parameterTypes, null, new IProcessSpOutput() {

					@Override
					public boolean nextResult(ResultSet rs, int updateCount)
							throws SQLException {
						if (updateCount != -1) {
							counts[0] = updateCount;
						}
						return false;
					}
				});
		return counts[0];
	}

	@Override
	protected int writeOrFail(final IReadWriteHandle handle)
			throws SQLException {
		final int n = this.write(handle);
		if (n == 0) {
			fail();
		}
		return n;
	}

	/**
	 * call the stored procedure with the required record as input values
	 *
	 * @param handle
	 * @param inRec
	 * @param numbersOfRowsAffected
	 *            array to receive for each output, if applicable
	 * @return returned-value if the procedure defines one. Number of affected
	 *         rows if the procedure is designed for it, or null if none of
	 *         these
	 * @throws SQLException
	 */
	protected Object callSp(IReadonlyHandle handle, Record inRec)
			throws SQLException {

		/**
		 * we are suing "this" itself as the as the call-back. Control
		 */
		return handle.callStoredProcedure(this.sqlText, inRec.fetchRawData(),
				inRec.fetchValueTypes(), this.returnedType, this);

	}

	/**
	 * call the stored procedure after setting all input fields
	 *
	 * @param handle
	 * @param numbersOfRowsAffected
	 *            array to receive for each output, if applicable
	 * @return returned-value if the procedure defines one. Number of affected
	 *         rows if the procedure is designed for it, or null if none of
	 *         these
	 *
	 * @throws SQLException
	 */
	protected Object callSp(IReadonlyHandle handle) throws SQLException {

		this.checkValues();

		/**
		 * we are suing "this" itself as the as the call-back. Control
		 */
		return handle.callStoredProcedure(this.sqlText, this.parameterValues,
				this.parameterTypes, this.returnedType, this);
	}

	protected DataTable<?>[] getOutputTables() {
		return this.outputTables;
	}

	protected int[] getNbrRowsAffected() {
		return this.uodateCounts;
	}

	/**
	 * lambda function to process a result from the called stored procedure
	 */
	@Override
	public boolean nextResult(ResultSet rs, int updateCount)
			throws SQLException {
		DataTable<?> dt = this.outputTables[this.outputIdx];
		if (dt == null) {
			if (rs == null) {
				throw new SQLException(
						"Stored procedure returned updated count at index "
								+ this.outputIdx
								+ " but it is expected to return a result set");
			}
			DbUtil.rsToDataTable(rs, dt);
		} else {
			if (rs != null) {
				throw new SQLException(
						"Stored procedure returned a result set at index "
								+ this.outputIdx
								+ " but it is expected to return an updateCount");
			}
			this.uodateCounts[this.outputIdx] = updateCount;
		}
		return true;
	}

}
