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

/**
 * Db Handle that takes care of all DB operations
 *
 * @author simplity.org
 *
 */
public interface IReadWriteHandle extends IReadonlyHandle {

	/**
	 * API that is close to the JDBC API for updating/inserting/deleting
	 *
	 * @param sql
	 *            a prepared statement that manipulates data.
	 * @param paramValues
	 *            parameters to be set the prepared statement
	 * @return number of affected rows. -1 if the driver was unable to determine
	 *         it
	 * @throws SQLException
	 */
	public int write(final String sql, final Object[] paramValues)
			throws SQLException;

	/**
	 * API that is close to the JDBC API for updating/inserting/deleting
	 *
	 * @param sql
	 *            a prepared statement that manipulates data.
	 * @param paramValues
	 *            parameters to be set the prepared statement
	 * @param generatedColumnName
	 *            null if generated primary key is not required
	 * @param generatedKeys
	 *            null if keys are not required. array must have required number
	 *            of elements based on the query being executed
	 * @return number of affected rows. -1 if the driver was unable to determine
	 *         it
	 * @throws SQLException
	 */
	public int writeWithKeyGeneration(final String sql,
			final Object[] paramValues, String generatedColumnName,
			long[] generatedKeys) throws SQLException;

	/**
	 * use a prepared statement, and values for the parameters to run it
	 *
	 * @param sql
	 *            a prepared statement that manipulates data.
	 * @param paramValues
	 *            Each element is a non-null array that contains non-null values
	 *            for each parameter in the prepared statement. to be set to the
	 *            prepared statement.
	 * @return number of affected rows. Not reliable. If driver returns -1, we
	 *         assume it to be 1
	 * @throws SQLException
	 */
	public int writeMany(final String sql, final Object[][] paramValues)
			throws SQLException;

	/**
	 * use a prepared statement, and values for the parameters to run it
	 *
	 * @param sql
	 *            a prepared statement that manipulates data.
	 * @param paramValues
	 *            Each element is a non-null array that contains non-null values
	 *            for each parameter in the prepared statement. to be set to the
	 *            prepared statement.
	 * @param generatedColumnName
	 *            null if generated primary key is not required
	 * @param generatedKeys
	 *            null if keys are not required. array must have required number
	 *            of elements based on the query being executed
	 * @return number of affected rows. Not reliable. If driver returns -1, we
	 *         assume it to be 1
	 * @throws SQLException
	 */
	public int writeManyWithKeyGeneration(final String sql,
			final Object[][] paramValues, String generatedColumnName,
			long[] generatedKeys) throws SQLException;

}
