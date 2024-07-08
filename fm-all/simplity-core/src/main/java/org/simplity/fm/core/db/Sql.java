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
	protected final String[] parameterNames;
	protected final ValueType[] parameterTypes;
	protected final ValueType[] outputTypes;

	/**
	 * array to hold input and output values, in case fields are specified, and
	 * not records
	 */
	protected final Object[] parameterValues;
	protected Object[] outputValues;

	/**
	 *
	 * @param sqlText
	 * @param parameterNames
	 * @param parameterTypes
	 */
	protected Sql(final String sqlText, final String[] parameterNames,
			final ValueType[] parameterTypes, final ValueType[] outputTypes) {
		this.sqlText = sqlText;
		this.parameterNames = parameterNames;
		this.parameterTypes = parameterTypes;
		this.outputTypes = outputTypes;

		if (parameterNames == null) {
			this.parameterValues = null;
		} else {
			this.parameterValues = new Object[parameterTypes.length];
		}

		if (outputTypes == null) {
			this.outputValues = null;
		} else {
			this.outputValues = new Object[outputTypes.length];
		}
	}

	// methods for reading with input fields and output record //

	/**
	 * to be called after setting parameter values using setters
	 *
	 * @param handle
	 * @param outputRecord
	 *            into which extracted data is extracted into
	 * @return true if read was successful. false if nothing was read
	 * @throws SQLException
	 */
	protected boolean read(final IReadonlyHandle handle, Record outputRecord)
			throws SQLException {

		this.checkValues();

		return handle.readIntoRecord(this.sqlText, this.parameterValues,
				this.parameterTypes, outputRecord);
	}

	/**
	 * to be called only after assigning values for all the input parameters
	 * using the setter methods. to be used when at least one row is expected as
	 * per our db design, and hence the caller need not handle the case with no
	 * rows
	 *
	 * @param handle
	 * @param outputRecord
	 *            into which extracted data is extracted into
	 * @throws SQLException
	 *             thrown when any SQL exception, OR when no rows are found
	 */
	protected void readOrFail(final IReadonlyHandle handle, Record outputRecord)
			throws SQLException {
		if (!this.read(handle, outputRecord)) {
			fail();
		}
	}

	/**
	 * to be called only after assigning values for all the input parameters
	 * using the setter methods.
	 *
	 * @param handle
	 * @param dataTable
	 *            to which the rows will be appended.
	 * @throws SQLException
	 */
	protected void readMany(final IReadonlyHandle handle,
			DataTable<Record> dataTable) throws SQLException {
		this.checkValues();
		handle.readIntoDataTable(this.sqlText, this.parameterValues,
				this.parameterTypes, dataTable);
	}

	// read methods with input record and output record //
	/**
	 * concrete class uses a signature with extended record-instances for input
	 * and output records for example
	 *
	 * <code>
	 * public boolean read(final IReadonlyHandle handle, CustomerSelectionRecord inputParams,
	 * 			CustomerDetailsRecord outputRecord) throws SQLException{
	 * 	return super.read(handle, inputParams, outputRecord);
	 * }
	 * </code>
	 */
	protected boolean read(final IReadonlyHandle handle, Record inputRecord,
			Record outputRecord) throws SQLException {

		return handle.readIntoRecord(this.sqlText, inputRecord, outputRecord);
	}

	/**
	 * to be used when a row is expected as per our db design, and hence the
	 * caller need not handle the case with no rows
	 *
	 * @param handle
	 * @throws SQLException
	 *             thrown when any SQL exception, OR when no rows are filtered
	 */
	protected void readOrFail(final IReadonlyHandle handle, Record inputRecord,
			Record outputRecord) throws SQLException {
		if (!this.read(handle, inputRecord, outputRecord)) {
			fail();
		}
	}

	/**
	 * concrete class uses this to readMany with params of specific concrete
	 * class
	 */
	protected void readMany(final IReadonlyHandle handle, Record inputRecord,
			DataTable<Record> dataTable) throws SQLException {

		handle.readIntoDataTable(this.sqlText, inputRecord, dataTable);
	}

	// read methods with input record and output fields //

	/**
	 * concrete class uses a signature with extended record-instances for input
	 * and output records for example
	 *
	 * <code>
	 * public boolean read(final IReadonlyHandle handle, CustomerSelectionRecord inputParams,
	 * 			CustomerDetailsRecord outputRecord) throws SQLException{
	 * 	return super.read(handle, inputParams, outputRecord);
	 * }
	 * </code>
	 */
	protected boolean readIn(final IReadonlyHandle handle, Record inputRecord)
			throws SQLException {

		Object[] row = handle.read(this.sqlText, inputRecord.fetchRawData(),
				inputRecord.fetchValueTypes(), this.outputTypes);
		if (row == null) {
			return false;
		}
		this.outputValues = row;
		return true;
	}

	/**
	 * to be used when a row is expected as per our db design, and hence the
	 * caller need not handle the case with no rows
	 *
	 * @param handle
	 * @throws SQLException
	 *             thrown when any SQL exception, OR when no rows are filtered
	 */
	protected void readInOrFail(final IReadonlyHandle handle,
			Record inputRecord) throws SQLException {
		if (!this.readIn(handle, inputRecord)) {
			fail();
		}
	}

	// read methods with input fields and output fields //
	/**
	 * to be called after setting parameter values using setters output fields
	 * can be extracted using getters aftr reading
	 *
	 * @param handle
	 * @param outputRecord
	 *            into which extracted data is extracted into
	 * @return true if read was successful. false if nothing was read
	 * @throws SQLException
	 */
	protected boolean readIn(final IReadonlyHandle handle) throws SQLException {

		this.checkValues();

		Object[] row = handle.read(this.sqlText, this.parameterValues,
				this.parameterTypes, this.outputTypes);
		if (row == null) {
			return false;
		}
		this.outputValues = row;
		return true;
	}

	/**
	 * to be called only after assigning values for all the input parameters
	 * using the setter methods. to be used when at least one row is expected as
	 * per our db design, and hence the caller need not handle the case with no
	 * rows
	 *
	 * @param handle
	 * @param outputRecord
	 *            into which extracted data is extracted into
	 * @throws SQLException
	 *             thrown when any SQL exception, OR when no rows are found
	 */
	protected void readInOrFail(final IReadonlyHandle handle)
			throws SQLException {
		if (!this.readIn(handle)) {
			fail();
		}
	}

	// write methods with input record //

	// write methods with input fields //
	/**
	 * Update/insert/delete operation. To be called after setting values for all
	 * the fields using setters
	 *
	 * @param handle
	 * @return number of rows affected
	 * @throws SQLException
	 */
	protected int write(final IReadWriteHandle handle) throws SQLException {
		this.checkValues();
		return handle.write(this.sqlText, this.parameterValues,
				this.parameterTypes);
	}

	/**
	 * Update/insert/delete one row. To be called after setting values for all
	 * the fields using setters
	 *
	 * @param handle
	 * @throws SQLException
	 *             if no rows are affected, or any sql error
	 */
	protected int writeOrFail(final IReadWriteHandle handle)
			throws SQLException {
		final int n = this.write(handle);
		if (n > 0) {
			return n;
		}
		fail();
		return 0;
	}

	protected int write(final IReadWriteHandle handle, Record record)
			throws SQLException {
		return handle.writeFromRecord(this.sqlText, record);
	}

	/**
	 * caller expects at least one row to be affected, failing which we are to
	 * raise an exception
	 *
	 * @param handle
	 * @return non-zero number of affected rows.
	 * @throws SQLException
	 *             if number of affected rows 0, or on any sql exception
	 */
	protected int writeOrFail(final IReadWriteHandle handle, Record record)
			throws SQLException {
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
	protected int writeMany(final IReadWriteHandle handle,
			DataTable<Record> table) throws SQLException {
		return handle.writeFromDataTable(this.sqlText, table);
	}

	protected void checkValues() throws SQLException {
		if (this.parameterValues == null) {
			return;
		}

		for (int i = 0; i < this.parameterValues.length; i++) {
			if (this.parameterValues[i] == null) {
				throw new SQLException(" No value provided for parameter "
						+ this.parameterNames[i] + ". Sql not executed");
			}
		}
	}

	protected static void fail() throws SQLException {
		throw new SQLException(
				"Sql is expected to affect at least one row, but no rows are affected.");

	}

}
