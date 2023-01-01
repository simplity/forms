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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.simplity.fm.core.app.AppManager;
import org.simplity.fm.core.rdb.IDbReader;
import org.simplity.fm.core.service.IServiceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * represents meta data for a value list to be fetched at run time
 *
 * @author simplity.org
 *
 */
public class RuntimeList implements IValueList {
	protected static final Logger logger = LoggerFactory
			.getLogger(RuntimeList.class);
	protected String name;
	/**
	 * sql that returns all the rows for a given key
	 */
	protected String listSql;

	/**
	 * sql that returns all rows, across all keys
	 */
	protected String allSql;
	protected String checkSql;
	/**
	 * sql that returns unique keys
	 */
	protected String allKeysSql;
	protected boolean hasKey;
	protected boolean keyIsNumeric;
	protected boolean valueIsNumeric;
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
		if (this.hasKey) {
			if (key == null) {
				logger.error(
						"ist {} requires value for its key. Value not receoved",
						this.name);
				return null;
			}
		}
		long l = 0;
		if (this.keyIsNumeric) {
			try {
				l = Long.parseLong(key.toString());
			} catch (final Exception e) {
				logger.error(
						"Key should be numeric value for list {} but we got {}",
						this.name, key);
				return null;
			}
		}

		final List<Object[]> list = new ArrayList<>();
		ListReader asst = new ListReader(key.toString(), l, list, ctx);

		try {
			AppManager.getAppInfra().getDbDriver().read(handle -> {
				handle.read(asst);
			});
		} catch (final SQLException e) {
			final String msg = e.getMessage();
			logger.error("Error while getting values for list {}. ERROR: {} ",
					this.name, msg);
			return null;
		}

		Object[][] emptyList = new Object[0][];
		if (list.size() == 0) {
			logger.warn("No data found for list {} with key {}", this.name,
					key);
			return emptyList;
		}
		return list.toArray(emptyList);
	}

	@Override
	public boolean isValid(final Object fieldValue, final Object keyValue,
			final IServiceContext ctx) {
		if (this.hasKey) {
			if (keyValue == null) {
				logger.error("Key should have value for list {}", this.name);
				return false;
			}
		}

		final Validator validator = new Validator(fieldValue, keyValue, ctx);

		try {
			AppManager.getAppInfra().getDbDriver().read(handle -> {
				handle.read(validator);
			});
		} catch (final SQLException e) {
			final String msg = e.getMessage();
			logger.error("Error while getting values for list {}. ERROR: {} ",
					this.name, msg);
			return false;
		}
		return validator.isValid();
	}

	/**
	 * this is specifically for batch operations where id is to be inserted in
	 * place of name.
	 *
	 * @param ctx
	 * @return map to get id from name
	 */
	@Override
	public Map<String, String> getAllEntries(final IServiceContext ctx) {
		final Map<String, String> list = new HashMap<>();

		try {
			AppManager.getAppInfra().getDbDriver().read(handle -> {
				EntriesReader reader = new EntriesReader(list, ctx);
				handle.read(reader);
			});
		} catch (final SQLException e) {
			final String msg = e.getMessage();
			logger.error("Error while getting values for list {}. ERROR: {} ",
					this.name, msg);
		}
		return list;
	}

	@Override
	public Map<String, Object[][]> getAllLists(IServiceContext ctx) {
		if (this.allKeysSql == null) {
			return null;
		}
		Map<String, Object[][]> lists = new HashMap<>();

		try {
			AppManager.getAppInfra().getDbDriver().read(handle -> {
				List<String> keys = new ArrayList<>();
				KeysReader keysAsst = new KeysReader(keys);
				handle.read(keysAsst);

				for (String key : keys) {
					long l = 0;
					if (this.keyIsNumeric) {
						l = Long.parseLong(key);
					}
					List<Object[]> list = new ArrayList<>();
					ListReader allAsst = new ListReader(key, l, list, ctx);
					handle.read(allAsst);
					lists.put(key, (Object[][]) list.toArray());
				}
			});

		} catch (final SQLException e) {
			final String msg = e.getMessage();
			logger.error("Error while getting values for list {}. ERROR: {} ",
					this.name, msg);
		}
		return lists;
	}

	private class KeysReader implements IDbReader {
		private final List<String> keys;

		protected KeysReader(List<String> keys) {
			this.keys = keys;
		}

		@Override
		public String getPreparedStatement() {
			return RuntimeList.this.allKeysSql;
		}

		@Override
		public void setParams(PreparedStatement ps) throws SQLException {
		}

		@Override
		public boolean readARow(ResultSet rs) throws SQLException {
			if (RuntimeList.this.keyIsNumeric) {
				keys.add("" + rs.getLong(1));
			} else {
				keys.add(rs.getString(1));

			}
			return true;
		}

	}

	private class ListReader implements IDbReader {
		private final IServiceContext ctx;
		private final String stringKey;
		private final long numericKey;
		private final List<Object[]> list;
		protected ListReader(String stringKey, long numericKey,
				List<Object[]> list, IServiceContext ctx) {
			this.stringKey = stringKey;
			this.numericKey = numericKey;
			this.list = list;
			this.ctx = ctx;
		}
		@Override
		public String getPreparedStatement() {
			return RuntimeList.this.listSql;
		}

		@Override
		public void setParams(final PreparedStatement ps) throws SQLException {
			int posn = 1;
			if (RuntimeList.this.hasKey) {
				if (RuntimeList.this.keyIsNumeric) {
					ps.setLong(posn, numericKey);
				} else {
					ps.setString(posn, stringKey);
				}
				posn++;
			}
			if (RuntimeList.this.isTenantSpecific) {
				ps.setLong(posn, (long) ctx.getTenantId());
				posn++;
			}
		}

		@Override
		public boolean readARow(final ResultSet rs) throws SQLException {
			final Object[] row = new Object[2];
			if (RuntimeList.this.valueIsNumeric) {
				row[0] = rs.getLong(1);
			} else {
				row[0] = rs.getString(1);
			}
			row[1] = rs.getString(2);
			list.add(row);
			return true;
		}

	}

	private class EntriesReader implements IDbReader {

		private final Map<String, String> entries;
		private final IServiceContext ctx;

		public EntriesReader(Map<String, String> entries, IServiceContext ctx) {
			this.entries = entries;
			this.ctx = ctx;
		}
		@Override
		public String getPreparedStatement() {
			return RuntimeList.this.allSql;
		}

		@Override
		public void setParams(final PreparedStatement ps) throws SQLException {
			if (RuntimeList.this.isTenantSpecific) {
				ps.setLong(1, (long) ctx.getTenantId());
			}
		}

		@Override
		public boolean readARow(final ResultSet rs) throws SQLException {
			final String id = rs.getString(1);
			final String nam = rs.getString(2);
			final String key = rs.getString(3);
			entries.put(key + '|' + nam, id);
			return true;
		}
	}

	private class Validator implements IDbReader {

		private boolean isValid;

		private final Object fieldValue;
		private final Object keyValue;
		private final IServiceContext ctx;

		protected Validator(Object fieldValue, Object keyValue,
				IServiceContext ctx) {
			this.fieldValue = fieldValue;
			this.keyValue = keyValue;
			this.ctx = ctx;
		}

		protected boolean isValid() {
			return this.isValid;
		}

		@Override
		public String getPreparedStatement() {
			return RuntimeList.this.checkSql;
		}

		@Override
		public void setParams(final PreparedStatement ps) throws SQLException {
			if (RuntimeList.this.valueIsNumeric) {
				ps.setLong(1, (Long) fieldValue);
			} else {
				ps.setString(1, (String) fieldValue);
			}

			if (RuntimeList.this.hasKey) {
				if (RuntimeList.this.keyIsNumeric) {
					ps.setLong(2, (Long) keyValue);
				} else {
					ps.setString(2, (String) keyValue);
				}
			}

			if (RuntimeList.this.isTenantSpecific) {
				ps.setLong(3, (long) ctx.getTenantId());
			}

		}

		@Override
		public boolean readARow(final ResultSet rs) throws SQLException {
			this.isValid = true;
			return false;
		}

	}
	@Override
	public boolean authenticationRequired() {
		return this.authenticationRequired;
	}

}
