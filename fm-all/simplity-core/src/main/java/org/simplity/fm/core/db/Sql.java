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

import java.io.StringWriter;

import org.simplity.fm.core.data.DataTable;
import org.simplity.fm.core.data.Record;
import org.simplity.fm.core.json.JsonUtil;
import org.simplity.fm.core.service.IOutputData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for App-specific SQL-classes. There are two ways to specify input.
 * Field[] or a named record if fields are used, the generator has to generate
 * setter method for all the fields. If record is specified, then the record
 * itself has the setters.
 *
 * @author simplity.org
 *
 */
public abstract class Sql {
	protected static final Logger logger = LoggerFactory.getLogger(Sql.class);
	protected boolean hasInputFields;
	protected String sqlText;
	protected Record inputRecord;
	protected DataTable<Record> batchData;

	/**
	 * to be used by a concrete class to implement setters for the input fields
	 *
	 * @param idx
	 * @param value
	 */

	protected void setInputValue(final int idx, final Object value) {
		this.inputRecord.assignValue(idx, value);
	}

	/**
	 * to be used by a concrete class that uses a named-(and hence generated)
	 * record. the generated method uses the appropriate named record as the
	 * parameter
	 *
	 * @param record
	 */
	protected void assignRecord(Record record) {
		this.inputRecord = record;
	}

	/**
	 *
	 * @return does this sql require input parameter values?
	 */
	public boolean requiresInput() {
		return this.hasInputFields;
	}

	/**
	 * @return string that shows this SQL with values from the current input
	 *         context
	 */
	public String showDetails() {
		final StringWriter sw = new StringWriter();
		String str = "SQL= " + this.sqlText;

		if (this.hasInputFields == false) {
			return str;
		}

		final IOutputData outData = JsonUtil.newOutputData(sw);
		outData.beginObject();
		outData.addRecord(this.inputRecord);
		outData.endObject();
		return str + "\n" + outData.toString();
	}
}
