// SPDX-License-Identifier: MIT
package org.simplity.fm.core.fn;

/**
 * Minimum of the numbers
 *
 */
public class Min extends NumericFunction {
	/**
	 * default constructor
	 */
	public Min() {
		this.setNbrArgs(-1);
	}

	@Override
	public double evaluate(final double[] args) {
		if (args == null || args.length == 0) {
			return 0;
		}

		double min = Double.MAX_VALUE;
		for (final double n : args) {
			if (n < min) {
				min = n;
			}
		}

		return min;
	}
}
