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

import java.util.HashMap;
import java.util.Map;

import org.simplity.fm.core.service.IServiceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class to specify an enumeration of valid values for a field. The
 * enumeration are further restricted based on a key field. This class is
 * extended by the generated key value list classes
 *
 * @author simplity.org
 */
public class KeyedValueList implements IValueList {
	protected static final Logger logger = LoggerFactory
			.getLogger(KeyedValueList.class);
	protected String name;
	protected boolean authenticationRequired;
	protected Map<Object, ValueList> values = new HashMap<>();

	@Override
	public boolean isValid(final Object fieldValue, final Object keyValue,
			final IServiceContext ctx) {
		final ValueList vl = this.values.get(keyValue);
		if (vl == null) {
			logger.error("Key {} is not valid for keyed list {}", keyValue,
					this.name);
			return false;
		}
		final boolean ok = vl.isValid(fieldValue, null, ctx);
		if (!ok) {
			logger.error("{} is not in the list for key {} is keyed list {}",
					fieldValue, keyValue, this.name);
		}
		return ok;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public boolean isKeyBased() {
		return true;
	}

	@Override
	public Object[][] getList(final Object keyValue,
			final IServiceContext ctx) {
		final ValueList vl = this.values.get(keyValue);
		if (vl == null) {
			logger.error(
					"Key {} is not valid for keyed list {}. Null list returned.",
					keyValue, this.name);
			return null;
		}
		return vl.valueList;
	}

	@Override
	public Map<String, String> getAllEntries(final IServiceContext ctx) {
		final Map<String, String> result = new HashMap<>();
		for (final Map.Entry<Object, ValueList> entry : this.values
				.entrySet()) {
			final String key = entry.getKey().toString() + '|';
			for (final Object[] row : entry.getValue().valueList) {
				result.put(key + row[1].toString(), row[0].toString());
			}
		}
		return result;
	}

	@Override
	public Map<String, Object[][]> getAllLists(IServiceContext ctx) {
		final Map<String, Object[][]> result = new HashMap<>();
		for (final Map.Entry<Object, ValueList> entry : this.values
				.entrySet()) {
			result.put(entry.getKey().toString(), entry.getValue().valueList);
		}
		return result;
	}

	@Override
	public boolean authenticationRequired() {
		return this.authenticationRequired;
	}
}