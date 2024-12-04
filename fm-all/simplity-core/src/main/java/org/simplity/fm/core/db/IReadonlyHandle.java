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
import java.util.List;

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
	 * @param sql             valid prepared statement to read from the database
	 * @param parameterValues null if the prepared statement has no parameters. must
	 *                        contain the right values in the right order for
	 *                        parameters in the prepared statement
	 * @param parameterTypes  value type of parameters
	 * @param outputTypes     must have the right types in the right order to
	 *                        receive data from the result set
	 * @param outputData      array into which extracted values are to be put into.
	 *                        Caller has to ensure that the array size matches the
	 *                        size of outputTypes
	 * @return true if all OK and the output data is populated. false if no data is
	 *         extracted.
	 * @throws SQLException
	 */
	public boolean read(final String sql, final Object[] parameterValues, final ValueType[] parameterTypes,
			final ValueType[] outputTypes, Object[] outputData) throws SQLException;

	/**
	 * read a row of data into a record
	 *
	 * @param sql             valid prepared statement to read from the database
	 * @param parameterValues null if the prepared statement has no parameters. must
	 *                        contain the right values in the right order for
	 *                        parameters in the prepared statement
	 * @param parameterTypes  value type of parameters
	 * @param outputRecord    that has the fields that match in number and type with
	 *                        the result set of the sql
	 * @return true if a row was indeed read, false otherwise
	 * @throws SQLException
	 */
//	public boolean readIntoRecord(final String sql, final Object[] parameterValues, final ValueType[] parameterTypes,
//			final Record outputRecord) throws SQLException;

	/**
	 * read a row of data into a record
	 *
	 * @param sql          valid prepared statement to read from the database
	 * @param inputRecord  record that has its fields that are to be used as
	 *                     parameters for the sql/prepared statement null if the
	 *                     prepared statement has no parameters.
	 * @param outputRecord that has the fields that match in number and type with
	 *                     the result set of the sql
	 * @return true if a row was indeed read, false otherwise
	 * @throws SQLException
	 */
	public boolean readIntoRecord(final String sql, final Record inputRecord, final Record outputRecord)
			throws SQLException;

	/**
	 * read one or more rows from the database
	 *
	 * @param sql             non-null valid prepared statement to read from the
	 *                        database
	 * @param parameterValues null if the prepared statement has no parameters. must
	 *                        contain the right values in the right order for
	 *                        parameters in the prepared statement
	 * @param parameterTypes  value type of parameters
	 * @param outputTypes     must have the right types in the right order to
	 *                        receive data from the result set
	 * @param outputData      list into which output rows are added
	 * @return number of rows added
	 * 
	 * @throws SQLException
	 */
	public int readMany(final String sql, final Object[] parameterValues, final ValueType[] parameterTypes,
			final ValueType[] outputTypes, List<Object[]> outputData) throws SQLException;

	/**
	 * read one or more rows from the database
	 *
	 * @param sql         non-null valid prepared statement to read from the
	 *                    database
	 * @param inputRecord record that has its fields that are to be used as
	 *                    parameters for the sql/prepared statement null if the
	 *                    prepared statement has no parameters.
	 * @param outputTypes must have the right types in the right order to receive
	 *                    data from the result set
	 * @return extracted data as an array of rows. null if no row is read
	 * @throws SQLException
	 */
//	public Object[][] readMany(final String sql, final Record inputRecord, final ValueType[] outputTypes)
//			throws SQLException;

	/**
	 * fetch one or more rows from the database, and invoke the row processor for
	 * each of the row. It s possible for the row-processor to abandon further
	 * reading at any point
	 *
	 * @param sql             non-null valid prepared statement to read from the
	 *                        database
	 * @param parameterValues null if the prepared statement has no parameters. must
	 *                        contain the right non-values in the right order for
	 *                        parameters in the select sql
	 * @param parameterTypes  value type of parameters
	 * @param outputTypes     must have the right types in the right order to
	 *                        receive data from the result set
	 * @param rowProcessor    lambda function to process one row at a time from the
	 *                        result set
	 * @return number of rows processed
	 * @throws SQLException
	 */
	public int readWithRowProcessor(final String sql, final Object[] parameterValues, final ValueType[] parameterTypes,
			final ValueType[] outputTypes, IRowProcessor rowProcessor) throws SQLException;

	/**
	 * read rows from the db as records and process each with the processor
	 *
	 * @param <T>             App-specific extended (generally generated) class that
	 *                        is to be used for output fields of the query
	 * @param sql             prepared statement for the read operation
	 * @param inputRecord     with fields that match in number and in type the
	 *                        parameters of the sql. null if the sql does not use
	 *                        any parameters
	 * @param instanceToClone this instance is cloned for each row in the sql output
	 * @param processor       record processor that supplies a new instance of
	 *                        record for each row, and processes it after it is
	 *                        populated with the extracted values
	 * @throws SQLException
	 */
	public <T extends Record> void readWithRecordProcessor(final String sql, final Record inputRecord,
			T instanceToClone, final IRecordProcessor<T> processor) throws SQLException;

	/**
	 * read rows from the db into a DataTable
	 *
	 * @param <T>         underlying record for the output table
	 * @param sql         prepared statement for the read operation
	 * @param inputRecord with fields that match in number and in type the
	 *                    parameters of the sql. null if the sql does not use any
	 *                    parameters
	 * @param outputTable
	 * @throws SQLException
	 */
	public <T extends Record> void readIntoDataTable(final String sql, final Record inputRecord,
			DataTable<T> outputTable) throws SQLException;

	/**
	 * read rows from the db into a DataTable
	 *
	 * @param <T>             underlying record for the output table
	 * @param sql             prepared statement for the read operation
	 * @param parameterValues null if the prepared statement has no parameters. must
	 *                        contain the right non-values in the right order for
	 *                        parameters in the select sql
	 * @param parameterTypes  value type of parameters
	 * @param outputTable
	 * @throws SQLException
	 */
	public <T extends Record> void readIntoDataTable(String sql, final Object[] parameterValues,
			final ValueType[] parameterTypes, DataTable<T> outputTable) throws SQLException;

	/**
	 * A stored procedure may produce one or more outputs as well as return a value.
	 * While the returned values, if any, is returned by this method, the outputs
	 * are handled by the lambda function (call-back object) supplied b the caller
	 *
	 * Caller should use the right handler, ReadOnly or ReadWrite, to ensure that
	 * transaction processing, if any is respected.
	 *
	 * @param callableSql       sql of the form {?= call proecudureName(?,?,...)}
	 * @param parameterValues   array of right-typed values for all the parameters
	 *                          of the stored procedure. null if the procedure
	 *                          receives no parameters
	 * @param parameterTypes    value-types of the parameters. null if the procedure
	 *                          accepts no parameters
	 * @param returnedValueType value types of the output. null if the procedure
	 *                          does not return any values, or the returned value is
	 *                          not be used
	 * @param fn                function that is called for each result in the
	 *                          output
	 * @return returned value from the stored procedure. null if the procedure does
	 *         not return value
	 * @throws SQLException
	 */
	public Object callStoredProcedure(String callableSql, Object[] parameterValues, ValueType[] parameterTypes,
			ValueType returnedValueType, IProcessSpOutput fn) throws SQLException;

	/**
	 * A stored procedure may produce one or more outputs as well as return a value.
	 * While the returned values, if any, is returned by this method, the outputs
	 * are handled by the lambda function (call-back object) supplied b the caller
	 *
	 * Caller should use the right handler, ReadOnly or ReadWrite, to ensure that
	 * transaction processing, if any is respected.
	 *
	 * @param callableSql       sql of the form {?= call proecudureName(?,?,...)}
	 * @param inputRecord       that has the parameters to be set into the
	 *                          parameters of the stored procedure Note that this
	 *                          DOES NOT include the return-value.
	 * @param returnedValueType value types of the output. null if the procedure
	 *                          does not return any values, or the returned value is
	 *                          not be used
	 * @param fn                function that is called for each result in the
	 *                          output
	 * @return returned value from the stored procedure. null if the procedure does
	 *         not return value
	 * @throws SQLException
	 */
	public Object callStoredProcedure(String callableSql, Record inputRecord, ValueType returnedValueType,
			IProcessSpOutput fn) throws SQLException;
}
