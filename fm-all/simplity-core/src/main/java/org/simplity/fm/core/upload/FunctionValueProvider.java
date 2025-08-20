// SPDX-License-Identifier: MIT
package org.simplity.fm.core.upload;

import java.util.Map;

import org.simplity.fm.core.fn.FunctionDefinition;
import org.simplity.fm.core.service.ServiceContext;

/**
 * Defines a function that evaluates to give a string
 *
 * @author simplity.org
 *
 */
class FunctionValueProvider implements InputValueProvider {
	final FunctionDefinition function;
	final InputValueProvider[] params;

	/**
	 *
	 * @param function
	 *            non-null function to be executed
	 * @param params
	 *            value providers for the parameters. number must match the
	 *            desired number of params for the function
	 */
	FunctionValueProvider(final FunctionDefinition function, final InputValueProvider[] params) {
		this.function = function;
		this.params = params;
	}

	@Override
	public String getValue(final Map<String, String> input, final ServiceContext ctx) {
		String[] values = null;
		if (this.params != null) {
			values = new String[this.params.length];
			for (int i = 0; i < values.length; i++) {
				values[i] = this.params[i].getValue(input, ctx);
			}
		}
		final Object obj = this.function.parseAndEval(ctx, values);
		if (obj == null) {
			return "";
		}
		return obj.toString();
	}
}
