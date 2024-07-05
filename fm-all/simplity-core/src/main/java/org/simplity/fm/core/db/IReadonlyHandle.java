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
 * A DB Handle that carries out read operations only.
 *
 * @author simplity.org
 *
 */
public interface IReadonlyHandle {

	/**
	 * read a row of data as an array of values
	 *
	 * @param sql
	 *            valid prepared statement to read from the database
	 * @param parameterValues
	 *            null if the prepared statement has no parameters. must contain
	 *            the right values in the right order for parameters in the
	 *            prepared statement
	 * @param parameterTypes
	 *            value type of parameters
	 * @param outputTypes
	 *            must have the right types in the right order to receive data
	 *            from the result set
	 * @return extracted data as an array of objects. Array length matches with
	 *         that of the outputTypes. null if no row is read
	 * @throws SQLException
	 */
	public Object[] read(final String sql, final Object[] parameterValues,
			final ValueType[] parameterTypes, final ValueType[] outputTypes)
			throws SQLException;

	/**
	 * read a row of data into a record
	 *
	 * @param sql
	 *            valid prepared statement to read from the database
	 * @param parameterValues
	 *            null if the prepared statement has no parameters. must contain
	 *            the right values in the right order for parameters in the
	 *            prepared statement
	 * @param parameterTypes
	 *            value type of parameters
	 * @param outputRecord
	 *            that has the fields that match in number and type with the
	 *            result set of the sql
	 * @return true if a row was indeed read, false otherwise
	 * @throws SQLException
	 */
	public boolean readIntoRecord(final String sql,
			final Object[] parameterValues, final ValueType[] parameterTypes,
			final Record outputRecord) throws SQLException;

	/**
	 * read a row of data into a record
	 *
	 * @param sql
	 *            valid prepared statement to read from the database
	 * @param inputRecord
	 *            record that has its fields that are to be used as parameters
	 *            for the sql/prepared statement null if the prepared statement
	 *            has no parameters.
	 * @param outputRecord
	 *            that has the fields that match in number and type with the
	 *            result set of the sql
	 * @return true if a row was indeed read, false otherwise
	 * @throws SQLException
	 */
	public boolean readIntoRecord(final String sql, final Record inputRecord,
			final Record outputRecord) throws SQLException;
	/**
	 * read one or more rows from the database
	 *
	 * @param sql
	 *            non-null valid prepared statement to read from the database
	 * @param parameterValues
	 *            null if the prepared statement has no parameters. must contain
	 *            the right values in the right order for parameters in the
	 *            prepared statement
	 * @param parameterTypes
	 *            value type of parameters
	 * @param outputTypes
	 *            must have the right types in the right order to receive data
	 *            from the result set
	 * @return extracted data as an array of rows. null if no row is read
	 * @throws SQLException
	 */
	public Object[][] readMany(final String sql, final Object[] parameterValues,
			final ValueType[] parameterTypes, final ValueType[] outputTypes)
			throws SQLException;

	/**
	 * read one or more rows from the database
	 *
	 * @param sql
	 *            non-null valid prepared statement to read from the database
	 * @param inputRecord
	 *            record that has its fields that are to be used as parameters
	 *            for the sql/prepared statement null if the prepared statement
	 *            has no parameters.
	 * @param outputTypes
	 *            must have the right types in the right order to receive data
	 *            from the result set
	 * @return extracted data as an array of rows. null if no row is read
	 * @throws SQLException
	 */
	public Object[][] readMany(final String sql, final Record inputRecord,
			final ValueType[] outputTypes) throws SQLException;

	/**
	 * fetch one or more rows from the database, and invoke the row processor
	 * for each of the row. It s possible for the row-processor to abandon
	 * further reading at any point
	 *
	 * @param sql
	 *            non-null valid prepared statement to read from the database
	 * @param parameterValues
	 *            null if the prepared statement has no parameters. must contain
	 *            the right non-values in the right order for parameters in the
	 *            select sql
	 * @param parameterTypes
	 *            value type of parameters
	 * @param outputTypes
	 *            must have the right types in the right order to receive data
	 *            from the result set
	 * @param rowProcessor
	 *            lambda function to process one row at a time from the result
	 *            set
	 * @return number of rows processed
	 * @throws SQLException
	 */
	public int readWithRowProcessor(final String sql,
			final Object[] parameterValues, final ValueType[] parameterTypes,
			final ValueType[] outputTypes, IRowProcessor rowProcessor)
			throws SQLException;

	/**
	 * read rows from the db as records and process each with the processor
	 *
	 * @param <T>
	 *            App-specific extended (generally generated) class that is to
	 *            be used for output fields of the query
	 * @param sql
	 *            prepared statement for the read operation
	 * @param inputRecord
	 *            with fields that match in number and in type the parameters of
	 *            the sql. null if the sql does not use any parameters
	 * @param instanceToClone
	 *            this instance is cloned for each row in the sql output
	 * @param processor
	 *            record processor that supplies a new instance of record for
	 *            each row, and processes it after it is populated with the
	 *            extracted values
	 * @throws SQLException
	 */
	public <T extends Record> void readWithRecordProcessor(final String sql,
			final Record inputRecord, T instanceToClone,
			final IRecordProcessor<T> processor) throws SQLException;

	/**
	 * read rows from the db into a DataTable
	 *
	 * @param <T>
	 *            underlying record for the output table
	 * @param sql
	 *            prepared statement for the read operation
	 * @param inputRecord
	 *            with fields that match in number and in type the parameters of
	 *            the sql. null if the sql does not use any parameters
	 * @param outputTable
	 * @throws SQLException
	 */
	public <T extends Record> void readIntoDataTable(final String sql,
			final Record inputRecord, DataTable<T> outputTable)
			throws SQLException;

	/**
	 * read rows from the db into a DataTable
	 *
	 * @param <T>
	 *            underlying record for the output table
	 * @param sql
	 *            prepared statement for the read operation
	 * @param parameterValues
	 *            null if the prepared statement has no parameters. must contain
	 *            the right non-values in the right order for parameters in the
	 *            select sql
	 * @param parameterTypes
	 *            value type of parameters
	 * @param outputTable
	 * @throws SQLException
	 */
	public <T extends Record> void readIntoDataTable(String sql,
			final Object[] parameterValues, final ValueType[] parameterTypes,
			DataTable<T> outputTable) throws SQLException;

	/**
	 * executes the stored-procedure and returns the result. A stored procedure
	 * may return a simple value as the returned-value. A stored procedure may
	 * execute more than one sql, there by producing several output.
	 *
	 * Caller should use the right handler, ReadOnly or ReadWrite, to ensure
	 * that transaction processing, if any is respected.
	 *
	 * @param callableSql
	 *            sql of the form {?= call proecudureName(?,?,...)}
	 * @param parameterValues
	 *            array of right-typed values for all the parameters of the
	 *            stored procedure. null if the procedure receives no parameters
	 * @param parameterTypes
	 *            value-types of the parameters. null if the procedure accepts
	 *            no parameters
	 * @param returnedValueType
	 *            value types of the output. null if the procedure does not
	 *            return any values, or the returned value is not be used
	 * @param outputTypes
	 *            an array with each element to take the result of a sql
	 *            statement. (A stored procedure may execute several statement,
	 *            there by producing many results) If the sql is a select
	 *            statement, then the array-element is an array of value types
	 *            representing a row of data. If the sql a non-select, then the
	 *            array element must be null.
	 * @return result
	 * @throws SQLException
	 */
	public StoredProcedureResult readFromStoredProcedure(String callableSql,
			Object[] parameterValues, ValueType[] parameterTypes,
			ValueType returnedValueType, ValueType[][] outputTypes)
			throws SQLException;
}
