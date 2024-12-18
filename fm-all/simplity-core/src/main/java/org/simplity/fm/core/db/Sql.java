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

package org.simplity.fm.core.db;

import java.sql.SQLException;

import org.simplity.fm.core.data.DataTable;
import org.simplity.fm.core.data.Field;
import org.simplity.fm.core.data.Record;
import org.simplity.fm.core.valueschema.ValueType;

/**
 * Base class for generated classes that do db operations with SQL
 *
 * This class has methods for all the possible combinations for:
 *
 * operations: read or write
 *
 * input parameters: fields or record
 *
 * output : fields or record
 *
 * @author simplity.org
 *
 */
public abstract class Sql {
	protected final String sqlText;
	protected final Field[] inputFields;
	protected final ValueType[] outputTypes;

	/**
	 * derived from fields for ready-use
	 */
	protected ValueType[] inputTypes;
	protected Object[] inputValues;
	protected Object[] outputValues;

	/**
	 *
	 * @param sqlText
	 * @param inputNames
	 * @param inputTypes
	 */
	protected Sql(final String sqlText, Field[] inputFields, final ValueType[] outputTypes) {
		this.sqlText = sqlText;
		this.inputFields = inputFields;
		this.outputTypes = outputTypes;
		this.setDerivedFields();

	}

	private void setDerivedFields() {
		if (this.inputFields != null) {
			int n = this.inputFields.length;
			this.inputValues = new Object[n];
			this.inputTypes = new ValueType[n];
			for (int i = 0; i < n; i++) {
				final Field field = this.inputFields[i];
				this.inputTypes[i] = field.getValueType();
				this.inputValues[i] = field.getDefaultValue();
			}
		}

		if (this.outputTypes == null) {
			this.outputValues = null;
		} else {
			this.outputValues = new Object[this.outputTypes.length];
		}
	}

	// methods for reading with input fields and output record //

	/**
	 * to be called after setting parameter values using setters
	 *
	 * @param handle
	 * @param outputRecord into which extracted data is extracted into
	 * @return true if read was successful. false if nothing was read
	 * @throws SQLException
	 */
	protected boolean read(final IReadonlyHandle handle, Record outputRecord) throws SQLException {

		this.checkValues();
		final Object[] row = new Object[outputRecord.length()];
		final boolean ok = handle.read(this.sqlText, this.inputValues, this.inputTypes, outputRecord.fetchValueTypes(),
				row);
		if (ok) {
			outputRecord.assignRawData(row);
		}
		return ok;
	}

	/**
	 * to be called only after assigning values for all the input parameters using
	 * the setter methods. to be used when at least one row is expected as per our
	 * db design, and hence the caller need not handle the case with no rows
	 *
	 * @param handle
	 * @param outputRecord into which extracted data is extracted into
	 * @throws SQLException thrown when any SQL exception, OR when no rows are found
	 */
	protected void readOrFail(final IReadonlyHandle handle, Record outputRecord) throws SQLException {
		if (!this.read(handle, outputRecord)) {
			fail();
		}
	}

	/**
	 * to be called only after assigning values for all the input parameters using
	 * the setter methods.
	 *
	 * @param handle
	 * @param dataTable to which the rows will be appended.
	 * @throws SQLException
	 */
	protected void readMany(final IReadonlyHandle handle, DataTable<Record> dataTable) throws SQLException {
		this.checkValues();
		handle.readIntoDataTable(this.sqlText, this.inputValues, this.inputTypes, dataTable);
	}

	// read methods with input record and output record //
	/**
	 * concrete class uses a signature with extended record-instances for input and
	 * output records for example
	 *
	 * <code>
	 * public boolean read(final IReadonlyHandle handle, CustomerSelectionRecord inputParams,
	 * 			CustomerDetailsRecord outputRecord) throws SQLException{
	 * 	return super.read(handle, inputParams, outputRecord);
	 * }
	 * </code>
	 */
	protected boolean read(final IReadonlyHandle handle, Record inputRecord, Record outputRecord) throws SQLException {

		return handle.readIntoRecord(this.sqlText, inputRecord, outputRecord);
	}

	/**
	 * to be used when a row is expected as per our db design, and hence the caller
	 * need not handle the case with no rows
	 *
	 * @param handle
	 * @throws SQLException thrown when any SQL exception, OR when no rows are
	 *                      filtered
	 */
	protected void readOrFail(final IReadonlyHandle handle, Record inputRecord, Record outputRecord)
			throws SQLException {
		if (!this.read(handle, inputRecord, outputRecord)) {
			fail();
		}
	}

	/**
	 * concrete class uses this to readMany with params of specific concrete class
	 */
	protected void readMany(final IReadonlyHandle handle, Record inputRecord, DataTable<Record> dataTable)
			throws SQLException {

		handle.readIntoDataTable(this.sqlText, inputRecord, dataTable);
	}

	// read methods with input record and output fields //

	/**
	 * concrete class uses a signature with extended record-instances for input and
	 * output records for example
	 *
	 * <code>
	 * public boolean read(final IReadonlyHandle handle, CustomerSelectionRecord inputParams,
	 * 			CustomerDetailsRecord outputRecord) throws SQLException{
	 * 	return super.read(handle, inputParams, outputRecord);
	 * }
	 * </code>
	 */
	protected boolean readIn(final IReadonlyHandle handle, Record inputRecord) throws SQLException {

		Object[] row = new Object[this.outputTypes.length];
		final boolean ok = handle.read(this.sqlText, inputRecord.fetchRawData(), inputRecord.fetchValueTypes(),
				this.outputTypes, row);
		if (ok) {
			this.outputValues = row;
		}
		return ok;
	}

	/**
	 * to be used when a row is expected as per our db design, and hence the caller
	 * need not handle the case with no rows
	 *
	 * @param handle
	 * @throws SQLException thrown when any SQL exception, OR when no rows are
	 *                      filtered
	 */
	protected void readInOrFail(final IReadonlyHandle handle, Record inputRecord) throws SQLException {
		if (!this.readIn(handle, inputRecord)) {
			fail();
		}
	}

	// read methods with input fields and output fields //
	/**
	 * to be called after setting parameter values using setters output fields can
	 * be extracted using getters aftr reading
	 *
	 * @param handle
	 * @param outputRecord into which extracted data is extracted into
	 * @return true if read was successful. false if nothing was read
	 * @throws SQLException
	 */
	protected boolean readIn(final IReadonlyHandle handle) throws SQLException {

		this.checkValues();

		Object[] row = new Object[this.outputTypes.length];
		final boolean ok = handle.read(this.sqlText, this.inputValues, this.inputTypes, this.outputTypes, row);
		if (ok) {
			this.outputValues = row;
		}
		return ok;
	}

	/**
	 * to be called only after assigning values for all the input parameters using
	 * the setter methods. to be used when at least one row is expected as per our
	 * db design, and hence the caller need not handle the case with no rows
	 *
	 * @param handle
	 * @param outputRecord into which extracted data is extracted into
	 * @throws SQLException thrown when any SQL exception, OR when no rows are found
	 */
	protected void readInOrFail(final IReadonlyHandle handle) throws SQLException {
		if (!this.readIn(handle)) {
			fail();
		}
	}

	// write methods with input record //
	protected int write(final IReadWriteHandle handle, Record record) throws SQLException {
		return handle.writeFromRecord(this.sqlText, record);
	}

	/**
	 * caller expects at least one row to be affected, failing which we are to raise
	 * an exception
	 *
	 * @param handle
	 * @return non-zero number of affected rows.
	 * @throws SQLException if number of affected rows 0, or on any sql exception
	 */
	protected int writeOrFail(final IReadWriteHandle handle, Record record) throws SQLException {
		final int n = handle.writeFromRecord(this.sqlText, record);
		if (n > 0) {
			return n;
		}
		fail();
		return 0;
	}

	/**
	 *
	 * @param handle
	 * @return number of affected rows. could be 0.
	 * @throws SQLException
	 */
	protected int writeMany(final IReadWriteHandle handle, DataTable<Record> table) throws SQLException {
		return handle.writeFromDataTable(this.sqlText, table);
	}

	// write methods with input fields //
	/**
	 * Update/insert/delete operation. To be called after setting values for all the
	 * fields using setters
	 *
	 * @param handle
	 * @return number of rows affected
	 * @throws SQLException
	 */
	protected int write(final IReadWriteHandle handle) throws SQLException {
		this.checkValues();
		return handle.write(this.sqlText, this.inputValues, this.inputTypes);
	}

	/**
	 * Update/insert/delete one row. To be called after setting values for all the
	 * fields using setters
	 *
	 * @param handle
	 * @throws SQLException if no rows are affected, or any sql error
	 */
	protected int writeOrFail(final IReadWriteHandle handle) throws SQLException {
		final int n = this.write(handle);
		if (n > 0) {
			return n;
		}
		fail();
		return 0;
	}

	protected void checkValues() throws SQLException {
		if (this.inputFields == null) {
			return;
		}

		for (int i = 0; i < this.inputFields.length; i++) {
			final Field field = this.inputFields[i];
			final Object value = this.inputValues[i];
			if (value == null) {
				if (field.isRequired()) {
					throw new SQLException(
							" No value provided for parameter " + field.getName() + ". Sql not executed");
				}
				continue;
			}
			// Object v = field.getValueSchema().parse(value);
			Object v = field.parse(value.toString(), null, null, 0);
			if (v == null) {
				throw new SQLException("A value of " + value + " is not valid for the field " + field.getName()
						+ " as per value schema " + field.getValueSchema().getName() + ". Sql not executed");
			}
			this.inputValues[i] = v;
		}
	}

	protected static void fail() throws SQLException {
		throw new SQLException("Sql is expected to affect at least one row, but no rows are affected.");

	}

}
