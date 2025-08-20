// SPDX-License-Identifier: MIT
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
	final private String sql;
	final private Object[] paraamValues;
	final private ValueType[] paramTypes;
	// final private DbField[] outputFields;
	final private String[] outputNames;
	final private ValueType[] outputTypes;

	/**
	 * @param sql         complete sql for fetching rows from the db
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
		this.sql = sql;
		this.paraamValues = paramValues;
		this.paramTypes = paramTypes;
		this.outputNames = outputNames;
		this.outputTypes = outputTypes;
	}

	/**
	 *
	 * @return sql for fetching rows from the db.
	 */
	public String getSql() {
		return this.sql;
	}

	/**
	 *
	 * @return Array of values for the sql parameters. null or empty if where-clause
	 *         is null or has no parameters.
	 */
	public Object[] getParamValues() {
		return this.paraamValues;
	}

	/**
	 *
	 * @return array of value types for the sql parameters.
	 */
	public ValueType[] getParamTypes() {
		return this.paramTypes;
	}

	/**
	 * @return Names of output fields. (Not the column names in the data base, but
	 *         the field names as defined in the record)
	 *
	 *         This is the list of fields being chosen by the client, or all the
	 *         fields in the data base. Note that this does not include any field
	 *         defined in the record that is not a column in the database.
	 **/

	public String[] getOutputNames() {
		return this.outputNames;
	}

	/**
	 * @return value type of the output fields as in the outputNames array
	 */
	public ValueType[] getOutputTypes() {
		return this.outputTypes;
	}

}
