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

import org.simplity.fm.core.data.Record;
import org.simplity.fm.core.valueschema.ValueType;

/**
 * Db Handle that takes care of all DB operations
 *
 * @author simplity.org
 *
 */
public interface IReadWriteHandle extends IReadonlyHandle {

	/**
	 * @param sql
	 *            a prepared statement that manipulates data.
	 * @param inputRecord
	 *            with fields that match in number and type with the parameters
	 *            in the SQL
	 * @return number of rows affected by this operation. -1 if the driver was
	 *         unable to count the affected rows.
	 * @throws SQLException
	 */
	public int write(final String sql, final Record inputRecord)
			throws SQLException;

	/**
	 * @param sql
	 *            a prepared statement that manipulates data.
	 * @param paramaterValues
	 *            parameters to be set the prepared statement
	 * @param parameterTypes
	 * @return number of rows affected by this operation. -1 if the driver was
	 *         unable to count the affected rows.
	 * @throws SQLException
	 */
	public int write(final String sql, final Object[] paramaterValues,
			ValueType[] parameterTypes) throws SQLException;

	/**
	 * insert a row based on the data in a record and update the generated key
	 * filed in the record
	 *
	 * @param sql
	 *            a prepared statement that manipulates data.
	 * @param parameterValues
	 *            values to be set to the query parameters in the prepared
	 *            statement
	 * @param parameterTypes
	 *            value-types for the parameters, in the right number and order
	 * @param generatedColumnName
	 *            the database column name that is generated when a row is
	 *            inserted
	 * @param generatedKeys
	 *            array with at least one element. 0-the element is populated
	 *            with the generated key. it is not touched if no key is
	 *            generated with the specified column name
	 * @return number of affected rows. -1 if the driver was unable to determine
	 *         it
	 * @throws SQLException
	 */
	public int insertWithKeyGeneration(final String sql,
			final Object[] parameterValues, ValueType[] parameterTypes,
			String generatedColumnName, long[] generatedKeys)
			throws SQLException;

	/**
	 * @param sql
	 *            a prepared statement that manipulates data.
	 * @param inputRecords
	 *            each with fields that match in number and type with the
	 *            parameters in the SQL
	 * @return number of rows affected by this operation. -1 if the driver was
	 *         unable to count the affected rows.
	 * @throws SQLException
	 */
	public int writeMany(final String sql, final Record[] inputRecords)
			throws SQLException;

	/**
	 * execute a prepared statement repeatedly for each record in the input
	 * records collection
	 *
	 * @param sql
	 *            a prepared statement that manipulates data.
	 * @param parameterValues
	 *            Each element is a non-null array that contains values in the
	 *            right order for the query parameters in the prepared statement
	 * @param parameterTypes
	 *            value types corresponding to the values in each row
	 * @return number of affected rows. Not reliable. If the driver returns -1,
	 *         we assume it to be 1 for the sake of counting
	 * @throws SQLException
	 */
	public int writeMany(final String sql, final Object[][] parameterValues,
			ValueType[] parameterTypes) throws SQLException;

	/**
	 * insert many rows and return the primary keys generated for these rows
	 *
	 * @param sql
	 *            a prepared statement that manipulates data.
	 * @param rowsToInsert
	 *            rows of data to be inserted into the database
	 * @param parameterTypes
	 *            value-types for the parameters, in the right number and order
	 * @param generatedColumnName
	 *            the database column name that is generated when a row is
	 *            inserted
	 * @param generatedKeys
	 *            array with at least one element. 0-the element is populated
	 *            with the generated key. it is not touched if no key is
	 *            generated with the specified column name
	 * @return number of affected rows. -1 if the driver was unable to determine
	 *         it
	 * @throws SQLException
	 */
	public int insertWithKeyGenerations(final String sql,
			final Object[][] rowsToInsert, ValueType[] parameterTypes,
			String generatedColumnName, long[] generatedKeys)
			throws SQLException;

	/**
	 * executes the stored-procedure. A stored procedure may return a simple
	 * value as the returned-value. A stored procedure may execute more than one
	 * sql, there by producing several output.
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
	public StoredProcedureResult writeUsingStoredProcedure(String callableSql,
			Object[] parameterValues, ValueType[] parameterTypes,
			ValueType returnedValueType, ValueType[][] outputTypes)
			throws SQLException;
}
