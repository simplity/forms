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

package org.simplity.fm.core.data;

import java.io.IOException;
import java.util.Iterator;

import com.google.gson.stream.JsonWriter;

/**
 * @author simplity.org
 *
 */
public abstract class FormDataTable implements Iterable<FormData> {
	protected final Form form;
	protected SchemaDataTable dataTable;
	protected final Object[][] fieldValues;
	protected final FormDataTable[][] linkedData;

	protected FormDataTable(final Form form, final SchemaDataTable dataTable, final Object[][] fieldValues,
			final FormDataTable[][] linkedData) {
		this.form = form;
		if (dataTable == null) {
			this.dataTable = form.getSchema().newSchemaDataTable();
		} else {
			this.dataTable = dataTable;
		}
		this.fieldValues = fieldValues;
		this.linkedData = linkedData;
	}

	/**
	 *
	 * @return data table associated with this form table
	 */
	public SchemaDataTable getDataTable() {
		return this.dataTable;
	}

	/**
	 *
	 * @return local field values associated with this form table
	 */
	public Object[][] getFiedValues() {
		return this.fieldValues;
	}

	/**
	 * iterator for data rows
	 */
	@Override
	public Iterator<FormData> iterator() {
		final int nbr = this.dataTable.length();
		return new Iterator<FormData>() {
			private int idx = 0;

			@Override
			public boolean hasNext() {
				return this.idx < nbr;
			}

			@Override
			public FormData next() {
				return FormDataTable.this.getFormData(this.idx++);
			}
		};
	}

	protected FormData getFormData(final int idx) {
		Object[] vals = null;
		FormDataTable[] link = null;
		if (this.fieldValues != null) {
			vals = this.fieldValues[idx];
		}
		if (this.linkedData != null) {
			link = this.linkedData[idx];
		}
		return this.form.newFormData(this.dataTable.getSchemaData(idx), vals, link);
	}

	/**
	 * @param writer
	 * @throws IOException
	 */
	public void serializeRows(final JsonWriter writer) throws IOException {
		writer.beginArray();
		for (final FormData fd : this) {
			writer.beginObject();
			fd.serializeFields(writer);
			writer.endObject();
		}
		writer.endArray();
	}

	/**
	 * @return number of rows in this data table
	 */
	public int length() {
		if (this.dataTable == null) {
			return 0;
		}
		return this.dataTable.length();
	}
}
