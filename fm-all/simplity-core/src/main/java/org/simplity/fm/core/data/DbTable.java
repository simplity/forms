/*
 * Copyright (c) 2020 simplity.org
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

package org.simplity.fm.core.data;

import java.sql.SQLException;

import org.simplity.fm.core.rdb.ReadWriteHandle;
import org.simplity.fm.core.rdb.ReadonlyHandle;

/**
 * Represents an array of <code>DbRecord</code>. This wrapper class is created
 * to provide db/persistence related functionalities
 *
 * @author simplity.org
 * @param <T>
 *            DbRecord rows this class is to contain
 *
 */
public class DbTable<T extends DbRecord> extends Table<DbRecord> {
	private final T dbRecord;

	/**
	 * construct with an instance of the underlying dbRecord
	 *
	 * @param dbRecord
	 */
	public DbTable(final T dbRecord) {
		super(dbRecord);
		this.dbRecord = dbRecord;
	}

	/**
	 * To be used by utility programs. End-programmers should not use as this is
	 * not type-safe. ENd-programmers should use FilterSqls instead
	 *
	 * @param whereClauseStartingWithWhere
	 * @param valuesForWhereClause
	 * @param handle
	 * @return true if at least row is filtered. false if no rows.
	 * @throws SQLException
	 */
	public boolean filter(final String whereClauseStartingWithWhere,
			final Object[] valuesForWhereClause, final ReadonlyHandle handle)
			throws SQLException {
		this.rows = this.dbRecord.dba.filter(whereClauseStartingWithWhere,
				valuesForWhereClause, handle);
		return this.rows.size() > 0;
	}

	/**
	 * insert all rows into the db
	 *
	 * @param handle
	 * @return number true if all rows were saved. false in case of any error,
	 *         in which case the caller better roll-back the transaction rows
	 *         saved
	 * @throws SQLException
	 */
	public boolean insert(final ReadWriteHandle handle) throws SQLException {
		return this.dbRecord.dba.insertAll(handle,
				this.rows.toArray(new Object[0][]));
	}

	/**
	 * update all the rows into the data base
	 *
	 * @param handle
	 * @return number true if all rows were saved. false in case of any error,
	 *         in which case the caller better roll-back the transaction rows
	 *         saved
	 * @throws SQLException
	 */
	public boolean update(final ReadWriteHandle handle) throws SQLException {
		return this.dbRecord.dba.updateAll(handle,
				this.rows.toArray(new Object[0][]));
	}

	/**
	 * save the row into database. if the key is present, it is updated else it
	 * is inserted
	 *
	 * @param handle
	 * @return number true if all rows were saved. false in case of any error,
	 *         in which case the caller better roll-back the transaction rows
	 *         saved
	 * @throws SQLException
	 */
	public boolean save(final ReadWriteHandle handle) throws SQLException {
		return this.dbRecord.dba.saveAll(handle,
				this.rows.toArray(new Object[0][]));
	}

	/**
	 * fetch is used instead of get to avoid clash with getters in generated
	 * classes
	 *
	 * @param idx
	 * @return record at 0-based index. null if the index is not valid
	 */
	@Override
	@SuppressWarnings("unchecked")
	public T fetchRecord(final int idx) {
		final Object[] row = this.rows.get(idx);
		if (row == null) {
			return null;
		}
		return (T) this.dbRecord.newInstance(row);
	}

}
