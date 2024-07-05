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
 * @author simplity.org
 *
 */
public abstract class WriteSql extends Sql {
	private DataTable<Record> batchData;
	private Record[] emptyArray = new Record[0];

	/**
	 * to be used by the extended class if it uses fields for input
	 *
	 * @param handle
	 * @return number of affected rows. could be 0.
	 * @throws SQLException
	 */
	protected int write(final IReadWriteHandle handle) throws SQLException {
		this.checkBatch();
		return handle.writeFromRecord(this.sqlText, this.inputRecord);
	}

	/**
	 * to be used by the concrete class if it is based on record
	 *
	 * @param handle
	 * @return number of affected rows. could be 0.
	 * @throws SQLException
	 */
	protected int write(final IReadWriteHandle handle, Record record)
			throws SQLException {
		this.checkBatch();
		return handle.writeFromRecord(this.sqlText, record);
	}

	private void checkBatch() throws SQLException {
		if (this.batchData != null) {
			throw new SQLException(
					"Sql is prepared for batch, but write is issued.");
		}

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
	protected int writeOrFail(final IReadWriteHandle handle)
			throws SQLException {
		return this.writeOrFail(handle, this.inputRecord);
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
		this.checkBatch();
		final int n = handle.writeFromRecord(this.sqlText, record);
		if (n > 0) {
			return n;
		}
		logger.error(this.showDetails());
		throw new SQLException(
				"Sql is expected to affect at least one row, but no rows are affected.");
	}

	/**
	 *
	 * @param handle
	 * @return number of affected rows. could be 0.
	 * @throws SQLException
	 */
	protected int writeMany(final IReadWriteHandle handle,
			DataTable<Record> table) throws SQLException {
		if (this.batchData == null) {
			throw new SQLException(
					"Sql is not prepared for batch, but writeBatch is issued.");
		}
		return handle.writeFromDataTable(this.sqlText, table);
	}
}
