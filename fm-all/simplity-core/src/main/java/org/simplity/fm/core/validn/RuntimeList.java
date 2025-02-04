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

package org.simplity.fm.core.validn;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.simplity.fm.core.app.AppManager;
import org.simplity.fm.core.service.IServiceContext;
import org.simplity.fm.core.valueschema.ValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * represents meta data for a value list to be fetched at run time
 *
 * @author simplity.org
 *
 */
public class RuntimeList implements IValueList {
	protected static final Logger logger = LoggerFactory.getLogger(RuntimeList.class);
	private static final ValueType[] TYPES_FOR_ALL_ENTRIES = { ValueType.Text, ValueType.Text, ValueType.Text };
	private static final ValueType[] TYPES_FOR_KEYS = { ValueType.Text };
	private static final ValueType[] TYPES_FOR_VALIDATION = {};

	protected String name;
	/**
	 * sql that returns all the rows for a given key
	 */
	protected String listSql;

	/**
	 * sql that returns all rows, across all keys
	 */
	protected String allSql;
	/**
	 * validation sql
	 */
	protected String checkSql;
	/**
	 * sql that returns unique keys
	 */
	protected String allKeysSql;
	protected boolean hasKey;
	protected boolean keyIsNumeric;
	protected boolean column1IsNumeric;
	protected boolean isTenantSpecific;
	protected boolean authenticationRequired;

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public boolean isKeyBased() {
		return this.hasKey;
	}

	@Override
	public Object[][] getList(final Object key, final IServiceContext ctx) {
		Object tenantId = ctx.getTenantId();
		long numericKey = 0;
		if (this.hasKey) {
			if (key == null) {
				logger.error("List {} requires value for its key. Value not received", this.name);
				return null;
			}
			if (this.keyIsNumeric) {
				try {
					numericKey = Long.parseLong(key.toString());
				} catch (Exception e) {
					logger.error("List {} requires a numeric value for its key, but {} is set as the value of the key",
							this.name, key.toString());
					return null;
				}
			}
		}
		/*
		 * we may have 0,1 or 2 params
		 */
		Object[] params = new Object[2];
		ValueType[] paramTypes = new ValueType[2];
		int nbr = 0;

		if (this.hasKey) {
			if (this.keyIsNumeric) {
				paramTypes[0] = ValueType.Integer;
				params[0] = numericKey;
			} else {
				params[0] = key.toString();
				paramTypes[0] = ValueType.Text;
			}
			nbr = 1;
		}

		if (tenantId != null) {
			params[nbr] = tenantId;
			paramTypes[nbr] = ValueType.Integer;
			nbr++;
		}

		if (nbr != 2) {
			params = Arrays.copyOf(params, nbr);
			paramTypes = Arrays.copyOf(paramTypes, nbr);
		}

		final ValueType[] typesForList = { this.column1IsNumeric ? ValueType.Integer : ValueType.Text, ValueType.Text };
		final Object[] finalParams = params;
		final ValueType[] finalTypes = paramTypes;
		// list to accumulate list entries
		final List<Object[]> list = new ArrayList<>();

		try {
			AppManager.getApp().getDbDriver().doReadonlyOperations(handle -> {
				handle.readMany(this.listSql, finalParams, finalTypes, typesForList, list);
				return true;
			});

		} catch (final SQLException e) {
			final String msg = e.getMessage();
			logger.error("Error while getting values for list {}. ERROR: {} ", this.name, msg);
			return null;
		}

		Object[][] emptyList = new Object[0][];
		if (list.size() == 0) {
			logger.warn("No data found for list {} with key {}", this.name, key);
			return emptyList;
		}
		return list.toArray(emptyList);
	}

	@Override
	public boolean isValid(final Object fieldValue, final Object keyValue, final IServiceContext ctx) {
		if (this.hasKey && keyValue == null) {
			logger.error("Key should have value for list {}", this.name);
			return false;
		}
		boolean isValid = false;
		try {
			isValid = AppManager.getApp().getDbDriver().doReadonlyOperations(handle -> {
				/*
				 * we may have 1 or 2 params
				 */
				Object[] params = new Object[1];
				ValueType[] paramTypes = new ValueType[1];

				if (this.isTenantSpecific) {
					params = new Object[2];
					paramTypes = new ValueType[2];
					params[1] = ctx.getTenantId();
					paramTypes[1] = ValueType.Integer;
				}
				params[0] = fieldValue;
				paramTypes[0] = this.column1IsNumeric ? ValueType.Integer : ValueType.Text;

				/**
				 * we actually want to check if there is at least one row.
				 */

				Object[] result = new Object[0];
				return handle.read(this.checkSql, params, paramTypes, TYPES_FOR_VALIDATION, result);
			});

		} catch (final SQLException e) {
			final String msg = e.getMessage();
			logger.error("Error while getting values for list {}. ERROR: {} ", this.name, msg);
			return false;
		}
		return isValid;
	}

	/**
	 * this is specifically for batch operations where id is to be inserted in place
	 * of name.
	 *
	 * @param ctx
	 * @return map to get id from name
	 */
	@Override
	public Map<String, String> getAllEntries(final IServiceContext ctx) {

		final Map<String, String> entries = new HashMap<>();

		try {
			AppManager.getApp().getDbDriver().doReadonlyOperations(handle -> {
				Object[] params = { ctx.getTenantId() };
				ValueType[] paramTypes = { ValueType.Integer };
				if (this.isTenantSpecific == false) {
					params = null;
					paramTypes = null;
				}

				handle.readWithRowProcessor(this.allSql, params, paramTypes, TYPES_FOR_ALL_ENTRIES, row -> {
					entries.put(row[0].toString() + "|" + row[1].toString(), row[2].toString());
					return true;
				});
				return true;
			});
		} catch (final SQLException e) {
			final String msg = e.getMessage();
			logger.error("Error while getting values for list {}. ERROR: {} ", this.name, msg);
		}
		return entries;
	}

	@Override
	public Map<String, Object[][]> getAllLists(IServiceContext ctx) {
		if (this.allKeysSql == null) {
			return null;
		}

		final Map<String, Object[][]> lists = new HashMap<>();
		final Object tenantId = ctx.getTenantId();

		try {
			AppManager.getApp().getDbDriver().doReadonlyOperations(handle -> {
				// get all the keys first
				Object[] params = { tenantId };
				ValueType[] paramTypes = { ValueType.Integer };
				if (tenantId == null) {
					params = null;
					paramTypes = null;
				}

				final List<String> keys = new ArrayList<>();
				handle.readWithRowProcessor(this.allKeysSql, params, paramTypes, TYPES_FOR_KEYS, row -> {
					keys.add(row[0].toString());
					return true;
				});

				if (tenantId == null) {
					params = new Object[1];
					paramTypes = new ValueType[1];
				} else {
					params = new Object[2];
					params[1] = tenantId;
					paramTypes = new ValueType[2];
					paramTypes[1] = ValueType.Integer;
				}

				final ValueType[] typesForList = { ValueType.Text, ValueType.Text };
				if (this.keyIsNumeric) {
					typesForList[0] = ValueType.Integer;
				}
				for (String key : keys) {
					params[0] = this.keyIsNumeric ? Long.parseLong(key) : key;
					final List<Object[]> list = new ArrayList<>();
					handle.readWithRowProcessor(this.listSql, params, paramTypes, typesForList, row -> {
						list.add(row);
						return true;
					});
					lists.put(key, list.toArray(new Object[0][]));
				}
				return true;
			});

		} catch (final SQLException e) {
			final String msg = e.getMessage();
			logger.error("Error while getting values for list {}. ERROR: {} ", this.name, msg);
		}
		return lists;
	}

	@Override
	public boolean authenticationRequired() {
		return this.authenticationRequired;
	}
}
