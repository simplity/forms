// SPDX-License-Identifier: MIT
package org.simplity.fm.core.validn;

import java.util.HashMap;
import java.util.Map;

import org.simplity.fm.core.service.ServiceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class to specify an enumeration of valid values for a field. The
 * enumeration are further restricted based on a key field. This class is
 * extended by the generated key value list classes
 *
 * @author simplity.org
 */
public class KeyedValueList implements ValueList {
	protected static final Logger logger = LoggerFactory
			.getLogger(KeyedValueList.class);
	protected String name;
	protected boolean authenticationRequired;
	protected Map<Object, SimpleValueList> values = new HashMap<>();

	@Override
	public boolean isValid(final Object fieldValue, final Object keyValue,
			final ServiceContext ctx) {
		final SimpleValueList vl = this.values.get(keyValue);
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
			final ServiceContext ctx) {
		final SimpleValueList vl = this.values.get(keyValue);
		if (vl == null) {
			logger.error(
					"Key {} is not valid for keyed list {}. Null list returned.",
					keyValue, this.name);
			return null;
		}
		return vl.valueList;
	}

	@Override
	public Map<String, String> getAllEntries(final ServiceContext ctx) {
		final Map<String, String> result = new HashMap<>();
		for (final Map.Entry<Object, SimpleValueList> entry : this.values
				.entrySet()) {
			final String key = entry.getKey().toString() + '|';
			for (final Object[] row : entry.getValue().valueList) {
				result.put(key + row[1].toString(), row[0].toString());
			}
		}
		return result;
	}

	@Override
	public Map<String, Object[][]> getAllLists(ServiceContext ctx) {
		final Map<String, Object[][]> result = new HashMap<>();
		for (final Map.Entry<Object, SimpleValueList> entry : this.values
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