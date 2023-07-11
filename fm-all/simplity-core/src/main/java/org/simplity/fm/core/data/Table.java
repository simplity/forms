package org.simplity.fm.core.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.simplity.fm.core.service.IOutputData;

/**
 * A Tabular data structure that contains meta data about the fields iin each
 * row
 *
 * @param <T>
 */
public class Table<T extends Record> implements Iterable<T> {
	private final T record;
	protected List<Object[]> rows = new ArrayList<>();
	/**
	 * construct with an instance of the underlying Record
	 *
	 * @param record
	 */
	public Table(final T record) {
		this.record = record;
	}
	/**
	 * add a record
	 *
	 * @param rec
	 */
	public void addRecord(final T rec) {
		this.rows.add(rec.fieldValues.clone());
	}

	protected void addRow(final Object[] row) {
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
	 * fetch is used instead of get to avoid clash with getters in generated
	 * classes
	 *
	 * @param idx
	 * @return record at 0-based index. null if the index is not valid
	 */
	@SuppressWarnings("unchecked")
	public T fetchRecord(final int idx) {
		final Object[] row = this.rows.get(idx);
		if (row == null) {
			return null;
		}
		return (T) this.record.newInstance(row);
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
			outData.addRecord(rec);
			outData.endObject();
		}
		outData.endArray();
	}

	@Override
	public Iterator<T> iterator() {
		final List<Object[]> r = this.rows;
		return new Iterator<>() {
			private int idx = 0;

			@Override
			public boolean hasNext() {
				return this.idx < r.size();
			}

			@Override
			public T next() {
				return Table.this.fetchRecord(this.idx++);
			}
		};
	}

}
