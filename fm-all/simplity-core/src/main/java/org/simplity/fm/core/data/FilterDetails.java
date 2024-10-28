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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.simplity.fm.core.Conventions;
import org.simplity.fm.core.Message;
import org.simplity.fm.core.db.FilterOperator;
import org.simplity.fm.core.service.IInputArray;
import org.simplity.fm.core.service.IInputData;
import org.simplity.fm.core.service.IServiceContext;
import org.simplity.fm.core.valueschema.ValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A data structure with details like fields to select, filtering and sorting
 * for a filter operation. Provides a utility method to parse these details from
 * a JSON source
 *
 * @author simplity.org
 *
 */
class FilterDetails {
	private static final Logger logger = LoggerFactory.getLogger(FilterDetails.class);
	private static final String IN = " IN (";
	private static final String LIKE = " LIKE ? escape '\\'";
	private static final String BETWEEN = " BETWEEN ";
	private static final String WILD_CARD = "%";
	private static final String ESCAPED_WILD_CARD = "\\%";
	private static final String WILD_CHAR = "_";
	private static final String ESCAPED_WILD_CHAR = "\\_";
	private static final char QN = '?';

	final private String whereClause;
	final private Object[] whereParamValues;
	final private ValueType[] whereParamTypes;
	final private DbField[] outputFields;

	/**
	 * @param whereClauseStartingWithWhere e.g. "WHERE a=? and b=?" null if all rows
	 *                                     are to be read. Best practice is to use
	 *                                     parameters rather than dynamic sql. That
	 *                                     is you should use a=? rather than a = 32
	 * @param whereParamValues             null or empty if where-clause is null or
	 *                                     has no parameters. every element MUST be
	 *                                     non-null and must be one of the standard
	 *                                     objects we use String, Long, Double,
	 *                                     Boolean, LocalDate, Instant
	 * @param whereParamTypes              value types of whereParamValues array
	 * @param outputFields                 optional. defaults to all fields. List of
	 *                                     fields to be selected. Caller's
	 *                                     responsibility to ensure that the fields
	 *                                     indeed are columns in this table/view
	 * 
	 */
	FilterDetails(final String whereClauseStartingWithWhere, final Object[] whereParamValues,
			ValueType[] whereParamTypes, final DbField[] outputFields) {
		this.whereClause = whereClauseStartingWithWhere;
		this.whereParamValues = whereParamValues;
		this.whereParamTypes = whereParamTypes;
		this.outputFields = outputFields;
	}

	String getWhereClause() {
		return this.whereClause;
	}

	Object[] getWhereParamValues() {
		return this.whereParamValues;
	}

	ValueType[] getWhereParamTypes() {
		return this.whereParamTypes;
	}

	/**
	 * 
	 * @return Fields to be selected for this filter. null implies sticking to
	 *         standard select for this record/table
	 */
	DbField[] getOutputFields() {
		return this.outputFields;
	}

	/**
	 * 
	 * @param inputObject JSON with all the required data
	 * @param fields      all fields in the record
	 * @param tenantField if the record is multi-tenant
	 * @param ctx
	 * @return Filter details that can be passed to Dba for a filter operation
	 */
	static FilterDetails parse(final IInputData inputObject, final DbField[] fields, final DbField tenantField,
			final IServiceContext ctx) {

		/*
		 * create a look-up map of fields by fieldName
		 */
		final Map<String, DbField> map = new HashMap<>();
		for (final DbField field : fields) {
			map.put(field.getName(), field);
		}

		/*
		 * let us start parsing the input, starting with max rows
		 */
		int maxRows = (int) inputObject.getInteger(Conventions.Request.TAG_MAX_ROWS);
		if (maxRows != 0 && maxRows > 0 && maxRows <= Conventions.Db.MAX_ROWS_TO_FILTER) {
			logger.info("Client requested a max of {} rows.", maxRows);
		} else {
			maxRows = Conventions.Db.MAX_ROWS_TO_FILTER;
			logger.info("As per configuration, a max of {} rows will be selected.", maxRows);
		}

		/*
		 * column names to select
		 */
		DbField[] outputFields = null;
		final IInputArray fieldNames = inputObject.getArray(Conventions.Request.TAG_FIELDS);
		if (fieldNames != null) {
			final List<DbField> fieldsList = new ArrayList<>(fieldNames.length());
			for (String name : fieldNames.toStringArray()) {
				final DbField f = map.get(name);
				if (f == null) {
					logger.warn("{} is not a valid field in this record. Field dropped from selection list.", name);
				} else {
					fieldsList.add(f);
				}
			}
			int n = fieldsList.size();
			if (n > 0) {
				outputFields = fieldsList.toArray(new DbField[0]);
			}
		}

		/*
		 * filters
		 */
		IInputArray filters = inputObject.getArray(Conventions.Request.TAG_FILTERS);
		if (filters == null || filters.length() == 0) {
			logger.warn("payload for filter has no conditions. All rows will be filtered");
			filters = null;
		}

		/*
		 * sort order
		 */
		final IInputArray sorts = inputObject.getArray(Conventions.Request.TAG_SORTS);

		final StringBuilder sql = new StringBuilder();
		final List<Object> values = new ArrayList<>();
		final List<ValueType> types = new ArrayList<>();

		/*
		 * force a condition on tenant id if required
		 */
		if (tenantField != null) {
			sql.append(tenantField.getColumnName()).append("=?");
			values.add(ctx.getTenantId());
			types.add(ValueType.Integer);
		}

		if (filters != null) {
			final boolean ok = parseConditions(map, filters, ctx, values, types, sql);
			if (!ok) {
				return null;
			}
		}

		if (sql.length() > 0) {
			sql.insert(0, " WHERE ");
		}

		if (sorts != null) {
			boolean isFirst = true;

			for (IInputData sortBy : sorts.toDataArray()) {
				String fieldName = sortBy.getString(Conventions.Request.TAG_SORT_BY_FIELD);
				final DbField field = map.get(fieldName);
				if (field == null) {
					logger.error("{} is not a field in the form. Sort order ignored", fieldName);
					continue;
				}

				if (isFirst) {
					sql.append(" ORDER BY ");
					isFirst = false;
				} else {
					sql.append(", ");
				}

				sql.append(field.getColumnName());

				boolean isDescending = sortBy.getBoolean(Conventions.Request.TAG_SORT_BY_DESCENDING);
				if (isDescending) {
					sql.append(" DESC ");
				}
			}
		}
		sql.append(" FETCH FIRST " + maxRows + " ROWS ONLY");

		final String sqlText = sql.toString();
		logger.info("filter clause is: {}", sqlText);
		final int n = values.size();
		if (n == 0) {
			logger.info("Filter clause has no parameters.");
			return new FilterDetails(sqlText, null, null, outputFields);
		}

		final StringBuilder sbf = new StringBuilder();
		for (int i = 0; i < n; i++) {
			sbf.append('\n').append(i).append("= ").append(values.get(i));
		}
		logger.info("Filter parameters : {}", sbf.toString());
		return new FilterDetails(sqlText, values.toArray(), types.toArray(new ValueType[0]), outputFields);
	}

	private static void reportError(final String error, final IServiceContext ctx) {
		logger.error(error);
		ctx.addMessage(Message.newError(Conventions.MessageId.INVALID_DATA));
	}

	private static boolean parseConditions(final Map<String, DbField> fields, final IInputArray inputArray,
			final IServiceContext ctx, final List<Object> values, final List<ValueType> types,
			final StringBuilder sql) {

		/*
		 * fairly long inside the loop for each field. But it is just serial code. Hence
		 * left it that way
		 */
		/*
		 * condition has attributes field, operator, value and optional value2
		 */

		int i = -1;
		boolean allOk = true;
		for (IInputData c : inputArray.toDataArray()) {
			i++;

			/*
			 * Do we all the inputs ?
			 */
			final String fieldName = c.getString(Conventions.Request.TAG_FILTER_FIELD);
			final DbField field = fields.get(fieldName);
			if (field == null) {
				reportError("Filter field " + fieldName + " does not exist in the form/record", ctx);
				allOk = false;
			}

			final String operatorText = c.getString(Conventions.Request.TAG_FILTER_COMPARATOR);
			if (operatorText == null || operatorText.isEmpty()) {
				reportError("filter operator is missing at index " + i, ctx);
				allOk = false;
			}

			final FilterOperator opertor = FilterOperator.parse(operatorText);
			if (opertor == null) {
				reportError(operatorText + " is not a valid filter condition", ctx);
				allOk = false;
			}

			String value1 = c.getString(Conventions.Request.TAG_FILTER_VALUE);
			if (value1 == null) {
				reportError("value is missing for a filter condition at index " + i, ctx);
				allOk = false;
			}

			String value2 = null;
			if (opertor == FilterOperator.Between) {
				value2 = c.getString(Conventions.Request.TAG_FILTER_TO_VALUE);
				if (value2 == null) {
					reportError("toValue is missing for a filter condition at index " + i, ctx);
					allOk = false;
				}
			}

			/*
			 * operator == null is unnecessary, but added to avoid null-check-errors
			 */
			if (!allOk || opertor == null || value1 == null) {
				// skip even checking semantic errors because we start building the sql along
				// with further checks..
				continue;
			}

			String column = null;
			ValueType vt = null;
			if (field != null) {
				column = field.getColumnName();
				vt = field.getValueType();
			}
			if (column == null || vt == null) {
				reportError("Filter field " + fieldName + " is not a column in the table/view", ctx);
				allOk = false;
				continue;
			}

			String column1 = parseField(value1, fields, vt, ctx);
			if (column1 != null && column1.isEmpty()) {
				allOk = false;
				continue;
			}

			String column2 = null;
			if (value2 != null) {
				column2 = parseField(value2, fields, vt, ctx);
				if (column2 != null && column2.isEmpty()) {
					allOk = false;
					continue;
				}
			}

			/*
			 * we do all our string comparisons as case-insensitive.
			 * 
			 * This is because, in the business context 'case' of a letter has no meaning,
			 * but used for formatting.
			 * 
			 * e.g. John is john, and is also JOHN.
			 * 
			 * TODO: Of course, this argument is not valid for a field that is a computer
			 * generated unique text code. We will offer some feature to handle this
			 * exception
			 */
			if (vt == ValueType.Text) {
				column = toUpper(column);
				column1 = toUpper(column1);
				column2 = toUpper(column2);
			}
			sql.append(column);

			/*
			 * complex ones first.. we have to append ? to sql, and add type and value to
			 * the lists for each case
			 */
			if ((opertor == FilterOperator.Contains || opertor == FilterOperator.StartsWith)) {
				if (vt != ValueType.Text) {
					reportError("Condition " + opertor + " is not valid for field " + fieldName
							+ " which is of value type " + vt, ctx);
					allOk = false;
					continue;
				}
				if (column1 != null) {
					reportError(
							"Operator " + opertor.name() + " can not be used with a field as the second operand." + vt,
							ctx);
					allOk = false;
					continue;
				}

				sql.append(LIKE);
				value1 = escapeLike(value1) + WILD_CARD;
				if (opertor == FilterOperator.Contains) {
					value1 = WILD_CARD + value1;
				}
				values.add(value1);
				types.add(vt);
				continue;
			}

			if (opertor == FilterOperator.In) {
				if (column1 != null) {
					reportError(
							"Operator " + opertor.name() + " can not be used with a field as the second operand." + vt,
							ctx);
					allOk = false;
					continue;
				}
				sql.append(IN);
				boolean firstOne = true;
				boolean ok = true;
				for (final String part : value1.split(",")) {
					Object obj = vt.parse(part.trim());
					if (obj == null) {
						reportError(value1 + " is not a valid value for value type " + vt + " for field " + fieldName,
								ctx);
						ok = false;
						break;
					}
					if (firstOne) {
						sql.append(QN);
						firstOne = false;
					} else {
						sql.append(",?");
					}
					values.add(obj);
					types.add(vt);
				}
				if (ok) {
					sql.append(')');
				} else {
					allOk = false;
				}
				continue;
			}

			Object obj1 = null;
			if (column1 == null) {
				obj1 = vt.parse(value1);
				if (obj1 == null) {
					reportError(value1 + " is not a valid value for value type " + vt + " for field " + fieldName, ctx);
					continue;
				}
			}

			if (opertor == FilterOperator.Between) {
				sql.append(BETWEEN);
				if (column1 == null) {
					sql.append(QN);
					values.add(obj1);
					types.add(vt);
				} else {
					sql.append(column1);
				}
				sql.append(" AND ");
				if (column2 == null) {
					Object obj2 = vt.parse(value2);
					if (obj2 == null) {
						reportError(
								value2 + " is not a valid value for value type " + vt + " for the field " + fieldName,
								ctx);
						continue;
					}
					sql.append(QN);
					values.add(obj2);
					types.add(vt);
				} else {
					sql.append(column2);
				}

				continue;
			}

			sql.append(' ').append(operatorText).append(" ");
			if (column1 == null) {
				sql.append(QN);
				values.add(obj1);
				types.add(vt);
			} else {
				sql.append(column1);
			}
			sql.append(' ');
		}
		return allOk;
	}

	/**
	 * 
	 * @param name can be null, in which case null is returned
	 * @return SQL text syntax to get the upper-case value of this column
	 */
	private static String toUpper(String name) {
		if (name == null) {
			return null;
		}
		return "UPPER(" + name + ")";
	}

	/**
	 * returns null string if this is not of the form ${name}.
	 * 
	 * column name if the field valid.
	 * 
	 * Empty string if the field invalid. Appropriate error message is pushed to the
	 * ctx
	 * 
	 */
	private static String parseField(String value, Map<String, DbField> fields, ValueType vt, IServiceContext ctx) {
		int lastPosn = value.length() - 1;
		if (value.startsWith("${") == false || value.charAt(lastPosn) != '}') {
			return null;
		}
		String msg = null;
		String name = value.substring(2, lastPosn);
		DbField field = fields.get(name);
		if (field == null) {
			msg = "This field does not exist";
		} else {

			final ValueType valueType = field.getValueType();
			if (valueType == vt) {
				final String columnName = field.getColumnName();
				if (columnName != null) {
					return columnName;
				}
				msg = "This field is not a column in the table/view";
			} else {
				msg = "This field is of value type " + valueType.name() + "but a value of type " + vt.name()
						+ "is expected";
			}
		}

		msg = "Filter condition uses '" + value + "' indicating that the field '" + name
				+ "' to be used for comparison. " + msg;
		reportError(msg, ctx);
		return "";

	}

	/**
	 * NOTE: Does not work for MS-ACCESS. but we are fine with that!!!
	 *
	 * @param string
	 * @return string that is escaped for a LIKE sql operation.
	 */
	private static String escapeLike(final String string) {
		return string.replaceAll(WILD_CARD, ESCAPED_WILD_CARD).replaceAll(WILD_CHAR, ESCAPED_WILD_CHAR);
	}
}
