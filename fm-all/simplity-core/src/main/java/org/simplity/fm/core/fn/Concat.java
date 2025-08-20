// SPDX-License-Identifier: MIT
package org.simplity.fm.core.fn;

import org.simplity.fm.core.service.ServiceContext;
import org.simplity.fm.core.valueschema.ValueType;

/**
 * Concatenate strings
 *
 */
public class Concat implements FunctionDefinition {
	private static final ValueType[] TYPES = { ValueType.Text };

	/**
	 *
	 * @param args
	 * @return concat of all arguments. empty string, and not null, if argument
	 *         is null or it is empty
	 */
	@Override
	public Object eval(final Object... args) {
		if (args == null || args.length == 0) {
			return "";
		}
		final StringBuilder sbf = new StringBuilder();
		for (final Object obj : args) {
			if (obj != null) {
				sbf.append(obj.toString());
			}
		}
		return sbf.toString();
	}

	@Override
	public Object parseAndEval(final ServiceContext ctx, final String... params) {
		if (params == null) {
			return null;
		}
		final int nbr = params.length;
		final Object[] args = new Object[nbr];
		for (int i = 0; i < nbr; i++) {
			args[i] = params[i];
		}
		return this.eval(args);
	}

	@Override
	public ValueType[] getArgumentTypes() {
		return TYPES;
	}

	@Override
	public ValueType getReturnType() {
		return ValueType.Text;
	}

	@Override
	public int getNbrArguments() {
		return -1;
	}

	@Override
	public boolean acceptsVarArgs() {
		return true;
	}
}
