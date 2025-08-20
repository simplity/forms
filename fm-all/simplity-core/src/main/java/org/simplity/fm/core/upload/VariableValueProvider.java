// SPDX-License-Identifier: MIT
package org.simplity.fm.core.upload;

import java.util.Map;

import org.simplity.fm.core.service.ServiceContext;

/**
 * specifies how a field in the form maps to columns in the input row
 *
 * @author simplity.org
 *
 */
public class VariableValueProvider implements InputValueProvider {
	private final String variable;
	private final String constant;

	/**
	 * Constructor to create a value provider that will first try to get a value
	 * from the input map, failing which it will return a constant value.
	 *
	 * @param variable non-null
	 * @param constant if non-null, this value is used when the value map has no
	 *                 value for the variable.
	 *
	 */
	public VariableValueProvider(String variable, String constant) {
		this.variable = variable;
		this.constant = constant;
	}

	@Override
	public String getValue(Map<String, String> input, ServiceContext ctx) {
		String result = input.get(this.variable);
		if (result != null) {
			return result;
		}
		return this.constant;
	}
}
