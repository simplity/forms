package org.simplity.fm.core.db;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.simplity.fm.core.data.DataTable;
import org.simplity.fm.core.data.Field;
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

	/*
	 * method that is triggering the call-back
	 */
	private static final int READ_ONE = 0;
	private static final int READ_MANY = 0;
	private static final int WRITE = 0;
	// private static final int CALL = 0;

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
	protected final ValueType returnType;

	/**
	 * null if the procedure has only one input, and it is invoked as if it is a
	 * simple sql.
	 *
	 * class instances corresponding to each result of the stored procedure. an
	 * element is null if that result is for updateCOunt, and not a result set
	 */
	protected final Class<?>[] outputRecordClasses;

	/**
	 * to be used only if there are more than one results. In this case,
	 * outputRecord/outputTypes must be null.
	 *
	 * Each array element must be designed to receive rows from the
	 * corresponding output. An element is left as null if that output is a
	 * non-select sql
	 */
	protected DataTable<?>[] resultTables;
	protected int[] updateCounts;

	/*
	 * following attributes are used by the call back method to process the
	 * multiple results produced by the stored procedure
	 */
	/**
	 * what method is being invoked?
	 */
	private int operation;
	/**
	 * outputRecord received by the invoking method, if any
	 */
	private Record receivedRecord;
	/**
	 * outputTable received by the invoking method, if any
	 */
	private DataTable<?> receivedTable;
	/**
	 * current result being processed
	 */
	private int resultIdx;

	/**
	 * all ok with the operation for read operations
	 */
	private boolean readSuccessful;
	/**
	 * updatedCOunt for write operations
	 */
	private int nbrCount;

	protected StoredProcedure(String procedureName, ValueType returnType,
			Class<?>[] outputRecordClasses, final Field[] inputFields,
			final ValueType[] outputTypes) {
		super(makeSql(procedureName, returnType, inputFields), inputFields,
				outputTypes);
		this.procedureName = procedureName;
		this.returnType = returnType;
		this.outputRecordClasses = outputRecordClasses;
	}

	/**
	 * {?= proc_name(?,?....)}
	 */
	private static String makeSql(String procName, ValueType retType,
			Field[] fields) {
		StringBuilder sbf = new StringBuilder("{ ");
		if (retType != null) {
			sbf.append("? = ");
		}

		sbf.append("call ").append(procName).append('(');
		if (fields != null) {
			int n = fields.length;
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
	 * we provide methods that mimic a simple SQL if the procedure is doing just
	 * that. And the most complex API that exposes all the possible features of
	 * a full-blown SP
	 */

	@Override
	protected boolean read(final IReadonlyHandle handle, Record outRec)
			throws SQLException {

		this.checkValues();

		this.operation = READ_ONE;
		this.receivedRecord = outRec;
		handle.callStoredProcedure(this.sqlText, this.inputValues,
				this.inputTypes, null, this);
		return this.readSuccessful;
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

		this.operation = READ_MANY;
		this.receivedTable = dataTable;
		handle.callStoredProcedure(this.sqlText, this.inputValues,
				this.inputTypes, null, this);
	}

	// read methods with input record and output record //

	@Override
	protected boolean read(final IReadonlyHandle handle, Record inRec,
			Record outRec) throws SQLException {

		this.operation = READ_ONE;
		this.receivedRecord = outRec;
		handle.callStoredProcedure(this.sqlText, inRec, null, this);
		return this.readSuccessful;
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

		this.operation = READ_MANY;
		this.receivedTable = dataTable;
		handle.callStoredProcedure(this.sqlText, inRec, null, this);
	}

	@Override
	protected boolean readIn(final IReadonlyHandle handle, Record inRec)
			throws SQLException {

		this.operation = READ_ONE;
		handle.callStoredProcedure(this.sqlText, inRec, null, this);
		return this.readSuccessful;
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

		this.operation = READ_ONE;

		handle.callStoredProcedure(this.sqlText, this.inputValues,
				this.inputTypes, null, this);
		return this.readSuccessful;
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

		this.operation = WRITE;
		handle.callStoredProcedure(this.sqlText, inRec, null, this);
		return this.nbrCount;
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

		this.operation = WRITE;
		handle.callStoredProcedure(this.sqlText, this.inputValues,
				this.inputTypes, null, this);
		return this.nbrCount;
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

		this.resetResultFields();
		/**
		 * we are suing "this" itself as the as the call-back. Control
		 */
		return handle.callStoredProcedure(this.sqlText, inRec, this.returnType,
				this);

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
		this.resetResultFields();

		/**
		 * we are suing "this" itself as the as the call-back. Control
		 */
		return handle.callStoredProcedure(this.sqlText, this.inputValues,
				this.inputTypes, this.returnType, this);
	}

	private static final String MSG1 = "Stored procedure is to return an update count, but a result set is received";
	private static final String MSG2 = "Stored procedure is to return a result set but an updtate count is recived";
	/**
	 * lambda function to process a result from the called stored procedure
	 */
	@Override
	public boolean nextResult(ResultSet rs, int updateCount)
			throws SQLException {
		if (this.operation == WRITE) {
			if (updateCount == -1) {
				throw new SQLException(MSG1);
			}
			this.nbrCount = updateCount;
			return false;
		}

		if (this.operation == READ_ONE || this.operation == READ_MANY) {
			if (rs == null) {
				throw new SQLException(MSG2);
			}

			this.readSuccessful = true;
			if (this.operation == READ_ONE) {
				if (this.receivedRecord != null) {
					DbUtil.rsToRecord(rs, receivedRecord);
				} else {
					this.outputValues = DbUtil.getValuesFromRs(rs,
							this.outputTypes);
				}
			} else {
				DbUtil.rsToDataTable(rs, this.receivedTable);
			}
			return false;
		}

		DataTable<?> dt = this.resultTables[this.resultIdx];
		if (dt == null) {
			if (rs == null) {
				throw new SQLException(
						"Stored procedure returned updated count at index "
								+ this.resultIdx
								+ " but it is expected to return a result set");
			}
			DbUtil.rsToDataTable(rs, dt);
		} else {
			if (rs != null) {
				throw new SQLException(
						"Stored procedure returned a result set at index "
								+ this.resultIdx
								+ " but it is expected to return an updateCount");
			}
			this.updateCounts[this.resultIdx] = updateCount;
		}
		return true;

	}

	/**
	 * to be invoked only after a successful invocation of callSp(). This will
	 * return the update counts from the stored procedure
	 *
	 * @return array of dataTables that contain the output rows from each of the
	 *         results. An array element is null if the corresponding result did
	 *         not have a resultSte (but an updateCount instead) The array is
	 *         null if the stored procedure did not succeed. The array
	 *         DataTables correspond to the Records that re specified as output
	 *         parameters in the SQL specification.
	 */
	protected DataTable<?>[] fetchResultsTable() {
		return this.resultTables;
	}

	/**
	 * to be invoked only after a successful invocation of callSp(). This will
	 * return the update counts from the stored procedure
	 *
	 * @return array of integers that represent the updateCounts of each result
	 *         of this stored procedure. -1 implies that this result did not
	 *         produce an updateCOunt (but a resultSet)
	 *
	 *         null if the stored procedure was not invoked or the stored
	 *         procedure has no results
	 */
	protected int[] fetchUpdateCounts() {
		return this.updateCounts;
	}

	private void resetResultFields() throws SQLException {
		if (outputRecordClasses != null) {
			this.resultTables = createTableInstances(this.outputRecordClasses);
			this.updateCounts = new int[outputRecordClasses.length];
		}

	}
	/**
	 * create DataTable instances
	 *
	 * @param classes
	 * @return
	 * @throws SQLException
	 */
	private static DataTable<?>[] createTableInstances(Class<?>[] classes)
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
			} catch (Exception e) {
			} // will throw sqlException later

			if (record == null) {
				throw new SQLException("Class " + cls.getName()
						+ " failed to create an instance of a Record");
			}
			tables[i] = new DataTable<>(record);
		}
		return tables;
	}

}
