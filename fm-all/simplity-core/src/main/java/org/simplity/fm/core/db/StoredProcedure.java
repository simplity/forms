package org.simplity.fm.core.db;

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
public abstract class StoredProcedure extends Sql {

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
	 * outputRecord/outputTypes must be null null if the procedure does not
	 * produce any result sets. element is null for an element if it corresponds
	 * to an SQL that is meant for data manipulation
	 */
	protected final ValueType[][] spOutputTypes;

	protected StoredProcedure(String procedureName, ValueType returnedType,
			ValueType[][] spOutputTypes, final String sqlText,
			final String[] parameterNames, final ValueType[] parameterTypes,
			final ValueType[] outputTypes) {
		super(sqlText, parameterNames, parameterTypes, outputTypes);
		this.procedureName = procedureName;
		this.returnedType = returnedType;
		this.spOutputTypes = spOutputTypes;

	}

	/**
	 * to be called
	 */
	protected void init() {
		StringBuilder sbf = new StringBuilder("{ ");
		if (this.returnedType != null) {
			sbf.append("? = ");
		}

		sbf.append("call ").append(this.procedureName).append('(');
		if (this.parameterNames != null) {
			int n = this.parameterNames.length;
			while (n > 0) {
				sbf.append("?,"); // we will remove the last comma later
				n--;
			}
			sbf.setLength(sbf.length() - 1);
		}
		sbf.append(")}");
		// this.sqlText = sbf.toString();
	}

	// read/write methods when the SP is used as a simple SQL, (with no returned
	// value and no multiple outputs //

	@Override
	protected boolean read(final IReadonlyHandle handle, Record outRec)
			throws SQLException {

		this.checkValues();

		final ValueType[][] outTypes = {outRec.fetchValueTypes()};
		final StoredProcedureResult result = handle.readFromSp(this.sqlText,
				this.parameterValues, this.parameterTypes, null, outTypes);
		return resultToRec(result, outRec);
	}

	@Override
	protected void readMany(final IReadonlyHandle handle,
			DataTable<Record> dataTable) throws SQLException {
		this.checkValues();
		final ValueType[][] outTypes = {dataTable.fetchValueTypes()};
		final StoredProcedureResult result = handle.readFromSp(this.sqlText,
				this.parameterValues, this.parameterTypes, null, outTypes);
		resultToDt(result, dataTable);
	}

	@Override
	protected void readOrFail(final IReadonlyHandle handle, Record outRec)
			throws SQLException {
		if (!this.read(handle, outRec)) {
			throw new SQLException(
					"At least one row was expected, but non found");
		}
	}

	// read methods with input record and output record //

	@Override
	protected boolean read(final IReadonlyHandle handle, Record inRec,
			Record outRec) throws SQLException {

		final ValueType[][] outTypes = {outRec.fetchValueTypes()};
		final StoredProcedureResult result = handle.readFromSp(this.sqlText,
				inRec.fetchRawData(), inRec.fetchValueTypes(), null, outTypes);
		return resultToRec(result, outRec);
	}

	@Override
	protected void readMany(final IReadonlyHandle handle, Record inRec,
			DataTable<Record> dataTable) throws SQLException {
		final ValueType[][] outTypes = {dataTable.fetchValueTypes()};
		final StoredProcedureResult result = handle.readFromSp(this.sqlText,
				inRec.fetchRawData(), inRec.fetchValueTypes(), null, outTypes);
		resultToDt(result, dataTable);
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
	protected boolean readIn(final IReadonlyHandle handle, Record inRec)
			throws SQLException {

		final ValueType[][] outTypes = {this.outputTypes};
		final StoredProcedureResult result = handle.readFromSp(this.sqlText,
				inRec.fetchRawData(), inRec.fetchValueTypes(), null, outTypes);
		return this.resultToRec(result, null);
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

		final ValueType[][] outTypes = {this.outputTypes};
		final StoredProcedureResult result = handle.readFromSp(this.sqlText,
				this.parameterValues, this.parameterTypes, null, outTypes);
		return this.resultToRec(result, null);
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

		final StoredProcedureResult result = handle.writeUsingStoredProcedure(
				this.sqlText, inRec.fetchRawData(), inRec.fetchValueTypes(),
				null, null);
		return result.nbrRowsAffected;
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
		final StoredProcedureResult result = handle.writeUsingStoredProcedure(
				this.sqlText, this.parameterValues, this.parameterTypes, null,
				null);
		return result.nbrRowsAffected;
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
	 * call the stored procedure after setting all input fields
	 *
	 * @param handle
	 * @return result of this call process
	 * @throws SQLException
	 */
	protected StoredProcedureResult callSp(IReadonlyHandle handle)
			throws SQLException {

		this.checkValues();

		return handle.readFromSp(this.sqlText, this.parameterValues,
				this.parameterTypes, this.returnedType, this.spOutputTypes);
	}

	/**
	 * call the stored procedure with the required record as input values
	 *
	 * @param handle
	 * @return result of this call process
	 * @throws SQLException
	 */
	protected StoredProcedureResult callSp(IReadonlyHandle handle, Record inRec)
			throws SQLException {

		return handle.readFromSp(this.sqlText, inRec.fetchRawData(),
				inRec.fetchValueTypes(), this.returnedType, this.spOutputTypes);
	}

	private boolean resultToRec(StoredProcedureResult result, Record record)
			throws SQLException {

		if (result == null) {
			return false;
		}
		final Object[][][] d = result.outputData;
		if (d == null || d.length == 0 || d[0] == null || d[0].length == 0) {
			return false;
		}
		// we expect one row of data as the first element
		final Object[] row = d[0][0];
		if (row == null) {
			return false;
		}

		int expectedLength = record == null
				? this.outputTypes.length
				: record.length();
		if (row.length != expectedLength) {
			throw new SQLException("Output record has " + row.length
					+ " fields while the Stored Procedure has " + expectedLength
					+ " fields");
		}

		if (record == null) {
			this.outputValues = row;
		} else {
			record.assignRawData(row);
		}
		return true;

	}

	private static void resultToDt(StoredProcedureResult result,
			DataTable<Record> table) throws SQLException {
		if (result == null) {
			return;
		}
		final Object[][][] d = result.outputData;
		if (d == null || d.length == 0 || d[0] == null || d[0].length == 0) {
			return;
		}
		// we expect one row of data as the first element
		final Object[][] rows = d[0];
		final Object[] firstRow = rows[0];
		if (firstRow == null) {
			return;
		}
		if (firstRow.length != table.fetchValueTypes().length) {
			throw new SQLException("Output record has " + firstRow.length
					+ " fields while the Stored Procedure has "
					+ table.fetchValueTypes().length + " fields");
		}

		for (Object[] row : rows) {
			table.addRow(row);;
		}

	}
}
