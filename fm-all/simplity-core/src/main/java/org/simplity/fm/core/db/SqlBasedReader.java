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

/**
 * Base class for generating SQLS based on the meta-data designed specifically
 * for generated SQL-classes.
 *
 * @author simplity.org
 * @param <T>
 *            record returned when reading
 *
 */
public abstract class SqlBasedReader<T extends Record> extends Sql {
	protected T outputRecord;

	/**
	 * read a row using this sql
	 *
	 * @param handle
	 * @return null if read did not succeed.
	 * @throws SQLException
	 */
	protected T read(final IReadonlyHandle handle) throws SQLException {

		if (this.hasInputFields && this.inputRecord == null) {
			throw new SQLException("No inputs received for the sql");
		}

		@SuppressWarnings("unchecked")
		final T rec = (T) this.outputRecord.newInstance();
		boolean ok = handle.readIntoRecord(this.sqlText, this.inputRecord,
				this.outputRecord);
		if (ok) {
			return rec;
		}
		return null;
	}

	/**
	 * read rows using this sql
	 *
	 * @param handle
	 * @return null if read did not succeed.
	 * @throws SQLException
	 */
	protected DataTable<T> readMany(final IReadonlyHandle handle)
			throws SQLException {
		if (this.hasInputFields && this.inputRecord == null) {
			throw new SQLException("No inputs received for the sql");
		}

		@SuppressWarnings("unchecked")
		final T rec = (T) this.outputRecord.newInstance();
		DataTable<T> dt = new DataTable<>(rec);
		handle.readIntoDataTable(this.sqlText, this.inputRecord, dt);
		return dt;
	}

	/**
	 * to be used when a row is expected as per our db design, and hence the
	 * caller need not handle the case with no rows
	 *
	 * @param handle
	 * @return non-null record with the first filtered row
	 * @throws SQLException
	 *             thrown when any SQL exception, OR when no rows are filtered
	 */
	protected T readOrFail(final IReadonlyHandle handle) throws SQLException {
		final T result = this.read(handle);

		if (result == null) {
			throw new SQLException(
					"Expected one row, but none found" + this.showDetails());
		}
		return result;
	}
}
