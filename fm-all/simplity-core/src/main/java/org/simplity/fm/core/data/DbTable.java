// SPDX-License-Identifier: MIT
package org.simplity.fm.core.data;

import java.sql.SQLException;

import org.simplity.fm.core.db.ReadWriteHandle;
import org.simplity.fm.core.db.ReadonlyHandle;
import org.simplity.fm.core.filter.FilterParams;
import org.simplity.fm.core.service.ServiceContext;

/**
 * Represents an array of <code>DbRecord</code>. This wrapper class is created
 * to provide db/persistence related functionalities
 *
 * @author simplity.org
 * @param <T> DbRecord rows this class is to contain
 *
 */
public class DbTable<T extends DbRecord> extends DataTable<DbRecord> {
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
	 * insert all rows into the db
	 *
	 * @param handle
	 * @return number true if all rows were saved. false in case of any error, in
	 *         which case the caller better roll-back the transaction rows saved
	 * @throws SQLException
	 */
	public boolean insert(final ReadWriteHandle handle) throws SQLException {
		return this.dbRecord.dba.insertAll(handle, this.rows.toArray(new Object[0][]));
	}

	/**
	 * update all the rows into the data base
	 *
	 * @param handle
	 * @return number true if all rows were saved. false in case of any error, in
	 *         which case the caller better roll-back the transaction rows saved
	 * @throws SQLException
	 */
	public boolean update(final ReadWriteHandle handle) throws SQLException {
		return this.dbRecord.dba.updateAll(handle, this.rows.toArray(new Object[0][]));
	}

	/**
	 * save the row into database. if the key is present, it is updated else it is
	 * inserted
	 *
	 * @param handle
	 * @return number true if all rows were saved. false in case of any error, in
	 *         which case the caller better roll-back the transaction rows saved
	 * @throws SQLException
	 */
	public boolean save(final ReadWriteHandle handle) throws SQLException {
		return this.dbRecord.dba.saveAll(handle, this.rows.toArray(new Object[0][]));
	}

	/**
	 * filter rows from the underlying table as per the filter conditions
	 *
	 * @param handle       readOnly handle
	 * @param filterParams required parameters for the filter operation
	 * @param ctx          In case of any errors in filterParams, they are added to
	 *                     the service context
	 * @return true if the filterParams was appropriate and the filter operation was
	 *         executed. False in case of any error in forming the sql based on the
	 *         filter parameters Note that returned value of true does not mean that
	 *         rows were added to the data table
	 * @throws SQLException
	 */
	public boolean filter(final ReadonlyHandle handle, FilterParams filterParams, ServiceContext ctx)
			throws SQLException {
		return this.dbRecord.dba.filter(handle, filterParams, this, ctx);
	}

	/**
	 * fetch is used instead of get to avoid clash with getters in generated classes
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
