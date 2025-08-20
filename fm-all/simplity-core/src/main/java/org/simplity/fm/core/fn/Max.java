// SPDX-License-Identifier: MIT
package org.simplity.fm.core.fn;

/**
 * Concatenate strings
 *
 */
public class Max extends NumericFunction {
	/**
	 * default constructor
	 */
	public Max() {
		this.setNbrArgs(-1);
	}

	@Override
	public double evaluate(final double[] args) {
		if (args == null || args.length == 0) {
			return 0;
		}

		double max = Double.MIN_VALUE;
		for (final double n : args) {
			if (n > max) {
				max = n;
			}
		}

		return max;
	}
}
