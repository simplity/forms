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
	 * method to be used to read into a valueObject using a sql component.
	 *
	 * @param sql
	 *            non-null valid prepared statement to read from the database
	 * @param inputData
	 *            null if the prepared statement has no parameters. must contain
	 *            the right values in the right order
	 * @param outputData
	 *            non-null. must have the right fields in the right order to
	 *            receive data from the result set
	 * @return true if a row was indeed read. false otherwise
	 * @throws SQLException
	 */
	public boolean read(final String sql, final Record inputData,
			final Record outputData) throws SQLException;

	/**
	 * method to be used to read into a valueObject using a sql component.
	 *
	 * @param sql
	 *            non-null valid prepared statement to read from the database
	 * @param paramValues
	 *            null if the prepared statement has no parameters. must contain
	 *            the right non-values in the right order for parameters in the
	 *            select sql
	 * @param outputTypes
	 *            non-null. must have the right types in the right order to
	 *            receive data from the result set
	 * @return extracted data as an array of objects. null if no row is read
	 * @throws SQLException
	 */
	public Object[] read(final String sql, final Object[] paramValues,
			final ValueType[] outputTypes) throws SQLException;

	/**
	 * method to be used to read into a valueObject using a sql component.
	 *
	 * @param sql
	 *            non-null valid prepared statement to read from the database
	 * @param paramValues
	 *            null if the prepared statement has no parameters. must contain
	 *            the right non-values in the right order for parameters in the
	 *            select sql
	 * @param outputTypes
	 *            non-null. must have the right types in the right order to
	 *            receive data from the result set
	 * @return extracted data as an array of rows. null if no row is read
	 * @throws SQLException
	 */
	public Object[][] readMany(final String sql, final Object[] paramValues,
			final ValueType[] outputTypes) throws SQLException;

	/**
	 * method to be used to process each of the row returned by the query,
	 * instead of returning them
	 *
	 * @param sql
	 *            non-null valid prepared statement to read from the database
	 * @param paramValues
	 *            null if the prepared statement has no parameters. must contain
	 *            the right non-values in the right order for parameters in the
	 *            select sql
	 * @param outputTypes
	 *            non-null. must have the right types in the right order to
	 *            receive data from the result set
	 * @param rowProcessor
	 *            lambda function to process one row at a time from the result
	 *            set
	 * @return number of rows processed
	 * @throws SQLException
	 */
	public int readMany(final String sql, final Object[] paramValues,
			final ValueType[] outputTypes, IRowProcessor rowProcessor)
			throws SQLException;

	/**
	 * method to be used to read possibly more than one rows into a valueTable
	 * using a prepared statement
	 *
	 * @param sql
	 *            non-null valid prepared statement for reading from the
	 *            database
	 * @param inputData
	 *            null if the prepared statement has no parameters. must contain
	 *            the right values in the right order
	 * @param outputInstance
	 *            an instance of the VO for output. This instance is not
	 *            modified, but used to create instances of new VOs
	 * @return list of output Vos. could be empty, but not null
	 * @throws SQLException
	 */
	public <T extends Record> List<T> readMany(final String sql,
			final Record inputData, final T outputInstance) throws SQLException;

	/**
	 * Most flexible way to read from db. Caller has full control of what and
	 * how to read.
	 *
	 * @param reader
	 *            instance that wants to read from the database
	 * @return number of rows actually read by the reader.
	 * @throws SQLException
	 *
	 */
	// public int read(final IDbReader reader) throws SQLException;

}
