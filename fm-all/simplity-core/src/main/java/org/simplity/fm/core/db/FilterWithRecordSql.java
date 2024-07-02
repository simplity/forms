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
 * A Sql that is designed to filter rows from the RDBMS. That is, result may
 * contain more than one rows
 *
 * @author simplity.org
 * @param <T>
 *            record that describes the result-set row returned by this sql
 *
 */
public abstract class FilterWithRecordSql<T extends Record> extends Sql {
	protected T outputRecord;

	/**
	 * filter rows into a data table
	 *
	 * @param handle
	 * @return non-null data table that has all the rows filtered. could be
	 *         empty
	 * @throws SQLException
	 */
	public List<T> filter(final IReadonlyHandle handle) throws SQLException {
		final List<T> list = new ArrayList<>();

		handle.readMany(this.sqlText, this.inputRecord, this.outputRecord,
				record -> {
					list.add(record);
					return true;
				});
		return list;
	}

	/**
	 * to be used when at least one row is expected as per our db design, and
	 * hence the caller need not handle the case with no rows
	 *
	 * @param handle
	 * @return non-null non-empty dbTable with all filtered with the first
	 *         filtered row
	 * @throws SQLException
	 *             thrown when any SQL exception, OR when no rows are filtered
	 */
	public List<T> filterOrFail(final IReadonlyHandle handle)
			throws SQLException {
		List<T> result = this.filter(handle);
		if (result.size() == 0) {
			throw new SQLException(
					"Filter did not return any row. " + this.showDetails());
		}
		return result;
	}

	/**
	 * iterator on the result of filtering. To be used if we have no need to get
	 * the entire dataTable,
	 *
	 * @param handle
	 * @param recordProcessor
	 *            call back function that takes record as parameter, and returns
	 *            true to continue to read, and false if it is not interested in
	 *            getting any more rows
	 * @throws SQLException
	 */
	public void forEach(final IReadonlyHandle handle,
			final IRecordProcessor<T> recordProcessor) throws SQLException {
		handle.readMany(this.sqlText, this.inputRecord, this.outputRecord,
				recordProcessor);
	}

}
