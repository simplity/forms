// SPDX-License-Identifier: MIT
package org.simplity.fm.core.fn;

/**
 * Concatenate strings
 *
 */
public class Sum extends NumericFunction {

	/**
	 * default constructor
	 */
	public Sum() {
		this.setNbrArgs(-1);
	}

	/**
	 *
	 * @param args
	 * @return sum of all the arguments
	 */
	@Override
	public double evaluate(final double[] args) {
		if (args == null || args.length == 0) {
			return 0;
		}

		double result = 0;
		for (final double n : args) {
			result += n;
		}

		return result;
	}
}
