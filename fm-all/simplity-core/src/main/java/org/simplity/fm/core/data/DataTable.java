package org.simplity.fm.core.data;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.simplity.fm.core.ApplicationError;
import org.simplity.fm.core.db.IRowProcessor;
import org.simplity.fm.core.service.IOutputData;
import org.simplity.fm.core.valueschema.ValueType;

/**
 * A Tabular data structure that contains meta data about the fields iin each
 * row
 *
 * @param <T>
 */
public class DataTable<T extends Record> implements Iterable<T>, IRowProcessor {
	private final T record;
	private final Constructor<T> constructor;
	protected List<Object[]> rows = new ArrayList<>();

	/**
	 * construct with an instance of the underlying Record
	 *
	 * @param record
	 */
	@SuppressWarnings("unchecked")
	public DataTable(final T record) {
		this.record = record;
		try {
			this.constructor = (Constructor<T>) record.getClass().getConstructor();
		} catch (Exception e) {
			throw new ApplicationError("error while getting a constructor for a Record instance", e);
		}
	}

	/**
	 *
	 * @return value types of the fields/columns of this data table
	 */
	public ValueType[] fetchValueTypes() {
		return this.record.fetchValueTypes();
	}

	/**
	 * add a record
	 *
	 * @param rec
	 */
	public void addRecord(final T rec) {
		this.rows.add(rec.fieldValues.clone());
	}

	/**
	 * TO BE USED BY UTILITY PROGRAMS ONLY. caller MUST ensure that the values in
	 * the row are of the right types
	 *
	 * @param row
	 */
	public void addRow(final Object[] row) {
		this.rows.add(row);
	}

	/**
	 * clear all existing data
	 */
	public void clear() {
		this.rows.clear();
	}

	/**
	 * @return number of data rows in this data table.
	 */
	public int length() {
		return this.rows.size();
	}

	/**
	 * fetch is used instead of get to avoid clash with getters in generated classes
	 *
	 * @param idx
	 * @return record at 0-based index. null if the index is not valid
	 */

	public T fetchRecord(final int idx) {
		final Object[] row = this.rows.get(idx);
		if (row == null) {
			return null;
		}
		try {
			T rec = this.constructor.newInstance();
			rec.assignRawData(row);
			return rec;
		} catch (Exception e) {
			throw new ApplicationError("Error while creating a new instance of " + this.record.getClass().getName(), e);
		}
	}

	/**
	 * serialized into an array [{},{}....]
	 *
	 * @param outData
	 * @throws IOException
	 */
	public void writeOut(final IOutputData outData) throws IOException {
		outData.beginArray();
		for (final T rec : this) {
			outData.beginObject();
			rec.writeOut(outData);
			outData.endObject();
		}
		outData.endArray();
	}

	/**
	 * write out as name:[{..}, {...}....]
	 * 
	 * @param memberName
	 * @param outputData
	 * @throws IOException
	 */
	public void writeOutAsMember(final String memberName, final IOutputData outputData) throws IOException {
		outputData.addName(memberName);
		this.writeOut(outputData);
	}

	@Override
	public Iterator<T> iterator() {
		final List<Object[]> r = this.rows;
		return new Iterator<T>() {
			private int idx = 0;

			@Override
			public boolean hasNext() {
				return this.idx < r.size();
			}

			@Override
			public T next() {
				return DataTable.this.fetchRecord(this.idx++);
			}
		};
	}

	@Override
	public boolean process(Object[] row) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

}
