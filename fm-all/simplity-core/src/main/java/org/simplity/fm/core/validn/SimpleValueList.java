// SPDX-License-Identifier: MIT
package org.simplity.fm.core.validn;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.simplity.fm.core.service.ServiceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for defining a set of enumerations as valid values of a field.
 * This class is extended by the generated ValueList classes
 *
 * @author simplity.org
 */
public class SimpleValueList implements ValueList {
	private static final Logger logger = LoggerFactory.getLogger(SimpleValueList.class);
	/*
	 * it is object, to allow keyed-list to re-use it as its collection
	 */
	protected Object name;
	protected Set<Object> values;
	protected boolean authenticationRequired;
	/*
	 * [object,string][] first element could be either number or text, but the
	 * second one always is text
	 */
	protected Object[][] valueList;

	/**
	 *
	 * @param name      non-null unique name
	 * @param valueList non-null non-empty [Object, String][]
	 */
	public SimpleValueList(final Object name, final Object[][] valueList) {
		this.name = name;
		this.valueList = valueList;
		this.values = new HashSet<>();
		for (final Object[] arr : valueList) {
			this.values.add(arr[0]);
		}
	}

	@Override
	public Object getName() {
		return this.name;
	}

	@Override
	public boolean isKeyBased() {
		return false;
	}

	@Override
	public boolean isValid(final Object fieldValue, final Object keyValue, final ServiceContext ctx) {
		final boolean ok = this.values.contains(fieldValue);
		if (!ok) {
			logger.error("{} is not found in list {}", fieldValue, this.name);
		}
		return ok;
	}

	@Override
	public Object[][] getList(final Object keyValue, final ServiceContext ctx) {
		return this.valueList;
	}

	@Override
	public Map<String, String> getAllEntries(final ServiceContext ctx) {
		final Map<String, String> result = new HashMap<>();
		for (final Object[] row : this.valueList) {
			result.put(row[1].toString(), row[0].toString());
		}
		return result;
	}

	@Override
	public Map<String, Object[][]> getAllLists(ServiceContext ctx) {
		return null;
	}

	@Override
	public boolean authenticationRequired() {
		return this.authenticationRequired;
	}

}