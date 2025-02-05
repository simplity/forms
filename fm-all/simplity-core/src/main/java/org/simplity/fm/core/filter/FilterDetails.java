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

package org.simplity.fm.core.filter;

import org.simplity.fm.core.valueschema.ValueType;

/**
 * A data structure with details like fields to select, filtering and sorting
 * for a filter operation. Provides a utility method to parse these details from
 * a JSON source
 *
 * @author simplity.org
 *
 */
public class FilterDetails {
	final private String whereClause;
	final private Object[] paraamValues;
	final private ValueType[] paramTypes;
	// final private DbField[] outputFields;
	final private String[] outputNames;
	final private ValueType[] outputTypes;

	/**
	 * @param sql         complete sql for fetching rows from the dab
	 * 
	 * @param paramValues null or empty if where-clause is null or has no
	 *                    parameters. every element MUST be non-null and must be one
	 *                    of the standard objects we use String, Long, Double,
	 *                    Boolean, LocalDate, Instant
	 * @param paramTypes  value types of whereParamValues array
	 * @param outputNames Names of output fields. (Not the column name sin the data
	 *                    base, but the field names as defined in the record. This
	 *                    is the list of fields being chosen by the client, or all
	 *                    the fields in the data base. Note that this does not
	 *                    include any field defined in the record that is not a
	 *                    column in the data base.
	 * @param outputTypes value type of the output fields as in the outputNames
	 *                    array
	 * 
	 */
	public FilterDetails(final String sql, final Object[] paramValues, ValueType[] paramTypes,
			final String[] outputNames, final ValueType[] outputTypes) {
		this.whereClause = sql;
		this.paraamValues = paramValues;
		this.paramTypes = paramTypes;
		this.outputNames = outputNames;
		this.outputTypes = outputTypes;
	}

	public String getSql() {
		return this.whereClause;
	}

	public Object[] getParamValues() {
		return this.paraamValues;
	}

	public ValueType[] getParamTypes() {
		return this.paramTypes;
	}

	/**
	 * Names of output fields. (Not the column name sin the data base, but the field
	 * names as defined in the record)
	 * 
	 * This is the list of fields being chosen by the client, or all the fields in
	 * the data base. Note that this does not include any field defined in the
	 * record that is not a column in the database.
	 **/

	public String[] getOutputNames() {
		return this.outputNames;
	}

	public ValueType[] getOutputTypes() {
		return this.outputTypes;
	}

}
