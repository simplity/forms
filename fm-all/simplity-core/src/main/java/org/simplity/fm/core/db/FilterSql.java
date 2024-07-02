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
import java.util.ArrayList;
import java.util.List;

import org.simplity.fm.core.data.Record;

/**
 * Component that represents a SQL that is to be used to get several rows from a
 * DB
 *
 * @author simplity.org
 * @param <T>
 *            concrete class of output value object that can be used to access
 *            the out data elements
 *
 */
public abstract class FilterSql<T extends Record> extends Sql {

	protected abstract T newOutputData();

	/**
	 * read many rows from the database
	 *
	 * @param handle
	 * @return list of records. empty, but not null if there are no rows.
	 * @throws SQLException
	 */
	public List<T> filter(final IReadonlyHandle handle) throws SQLException {
		final List<T> list = new ArrayList<>();

		handle.readMany(this.sqlText, this.inputRecord, this.newOutputData(),
				record -> {
					list.add(record);
					return true;
				});

		return list;
	}

	/**
	 * read at least one row from teh database, else throw an exception
	 *
	 * @param handle
	 * @return array of value object with output data. empty, but not null if
	 *         there are no rows.
	 * @throws SQLException
	 */
	public List<T> filterOrFail(final IReadonlyHandle handle)
			throws SQLException {
		final List<T> list = filter(handle);
		if (list.size() > 0) {
			return list;
		}
		logger.error(this.showDetails());
		throw new SQLException(
				"Sql is expected to return at least one row, but it didn't.");
	}

}
