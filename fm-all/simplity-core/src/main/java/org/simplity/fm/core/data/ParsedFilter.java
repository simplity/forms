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
 * Utility class used by dbRecord to parse input for a filter service
 *
 * @author simplity.org
 *
 */
class ParsedFilter {
	private static final Logger logger = LoggerFactory.getLogger(ParsedFilter.class);
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

	ParsedFilter(final String whereClauseStartingWithWhere, final Object[] whereParamValues,
			ValueType[] whereParamTypes) {
		this.whereClause = whereClauseStartingWithWhere;
		this.whereParamValues = whereParamValues;
		this.whereParamTypes = whereParamTypes;
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

	static ParsedFilter parse(final IInputData inputObject, final DbField[] fields, final DbField tenantField,
			final IServiceContext ctx) {
		IInputArray conditions = inputObject.getArray(Conventions.Request.TAG_FILTERS);
		if (conditions == null || conditions.length() == 0) {
			logger.warn("payload for filter has no conditions. All rows will be filtered");
			conditions = null;
		}

		/*
		 * sort order
		 */
		final IInputArray sorts = inputObject.getArray(Conventions.Request.TAG_SORT_BY);

		final int maxRows = (int) inputObject.getInteger(Conventions.Request.TAG_MAX_ROWS);
		if (maxRows != 0) {
			logger.info("Number of max rows is set to {}. It is ignored as of now.", maxRows);
		}
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

		/*
		 * get a look-up map of fields by fieldName
		 */
		final Map<String, DbField> map = new HashMap<>();
		for (final DbField field : fields) {
			map.put(field.getName(), field);
		}

		if (conditions != null) {
			final boolean ok = parseConditions(map, conditions, ctx, values, types, sql);
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
		/*
		 * did we get anything at all?
		 */
		if (sql.length() == 0) {
			logger.info("Filter has no conditions or sort orders");
			return new ParsedFilter(null, null, null);
		}

		final String sqlText = sql.toString();
		logger.info("filter clause is: {}", sqlText);
		final int n = values.size();
		if (n == 0) {
			logger.info("Filter clause has no parameters.");
			return new ParsedFilter(sqlText, null, null);
		}

		final StringBuilder sbf = new StringBuilder();
		for (int i = 0; i < n; i++) {
			sbf.append('\n').append(i).append("= ").append(values.get(i));
		}
		logger.info("Filter parameters : {}", sbf.toString());
		return new ParsedFilter(sqlText, values.toArray(), types.toArray(new ValueType[0]));
	}

	private static boolean reportError(final String error, final IServiceContext ctx) {
		logger.error(error);
		ctx.addMessage(Message.newError(Conventions.MessageId.INVALID_DATA));
		return false;
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
		for (IInputData c : inputArray.toDataArray()) {
			i++;
			final String fieldName = c.getString(Conventions.Request.TAG_FILTER_FIELD);
			final DbField field = fields.get(fieldName);
			if (field == null) {
				return reportError("Filter field " + fieldName + " does not exist in the form/record", ctx);
			}

			final String operatorText = c.getString(Conventions.Request.TAG_FILTER_COMPARATOR);
			if (operatorText == null || operatorText.isEmpty()) {
				return reportError("filter operator is missing at index " + i, ctx);
			}

			final FilterOperator opertor = FilterOperator.parse(operatorText);
			if (opertor == null) {
				return reportError(operatorText + " is not a valid filter condition", ctx);
			}

			String value = c.getString(Conventions.Request.TAG_FILTER_VALUE);
			if (value == null) {
				return reportError("value is missing for a filter condition at index " + i, ctx);
			}
			DbField field1 = parseField(value, fields);

			String value2 = null;
			DbField field2 = null;
			if (opertor == FilterOperator.Between) {
				value2 = c.getString(Conventions.Request.TAG_FILTER_TO_VALUE);
				if (value2 == null) {
					return reportError("toValue is missing for a filter condition at index " + i, ctx);
				}
				field2 = parseField(value2, fields);
			}

			final int idx = values.size();
			if (idx > 0) {
				sql.append(" and ");
			}

			sql.append(field.getColumnName());

			final ValueType vt = field.getValueType();
			Object obj = null;

			/*
			 * complex ones first.. we have to append ? to sql, and add type and value to
			 * the lists for each case
			 */
			if ((opertor == FilterOperator.Contains || opertor == FilterOperator.StartsWith)) {
				if (vt != ValueType.Text) {
					return reportError("Condition " + opertor + " is not valid for field " + fieldName
							+ " which is of value type " + vt, ctx);
				}

				sql.append(LIKE);
				value = escapeLike(value) + WILD_CARD;
				if (opertor == FilterOperator.Contains) {
					value = WILD_CARD + value;
				}
				values.add(value);
				types.add(vt);
				continue;
			}

			if (opertor == FilterOperator.In) {
				sql.append(IN);
				boolean firstOne = true;
				for (final String part : value.split(",")) {
					obj = vt.parse(part.trim());
					if (obj == null) {
						return reportError(
								value + " is not a valid value for value type " + vt + " for field " + fieldName, ctx);
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
				sql.append(')');
				continue;
			}

			if (field1 == null) {
				obj = vt.parse(value);
				if (obj == null) {
					return reportError(value + " is not a valid value for value type " + vt + " for field " + fieldName,
							ctx);
				}
			}
			if (opertor == FilterOperator.Between) {
				Object obj2 = null;
				if (field2 != null) {
					if (value2 != null) {
						obj2 = vt.parse(value2);
					}
					if (obj2 == null) {
						return reportError(
								value2 + " is not a valid value for value type " + vt + " for the field " + fieldName,
								ctx);
					}
				}
				sql.append(BETWEEN);
				if (field1 == null) {
					values.add(obj);
					types.add(vt);
					sql.append(QN);
				} else {
					sql.append(field1.getColumnName());
				}

				sql.append(" and ");
				if (field2 == null) {
					values.add(obj2);
					types.add(vt);
					sql.append(QN);
				} else {
					sql.append(field2.getColumnName());
				}
				continue;
			}

			sql.append(' ').append(operatorText).append(" ");
			if (field1 == null) {
				sql.append(QN);
				values.add(obj);
			} else {
				sql.append(field1.getColumnName());
			}
			sql.append(' ');
		}
		return true;
	}

	private static DbField parseField(String value, Map<String, DbField> fields) {
		int lastPosn = value.length() - 1;
		if (value.startsWith("${") == false || value.charAt(lastPosn) != '}') {
			return null;
		}
		String name = value.substring(2, lastPosn);
		DbField field = fields.get(name);
		if (field == null) {
			logger.error(
					"Filter condition used {} as value. As per the convention, this implies that a field named {} is to be used as this value. However {} is not a valid field in this record",
					value, name, name);
		}
		return field;
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
