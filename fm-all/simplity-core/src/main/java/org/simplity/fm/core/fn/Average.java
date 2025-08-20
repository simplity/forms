// SPDX-License-Identifier: MIT
package org.simplity.fm.core.fn;

/**
 * Concatenate strings
 *
 */
public class Average extends NumericFunction {

	/**
	 * default constructor
	 */
	public Average() {
		this.setNbrArgs(-1);
	}

	@Override
	public double evaluate(final double[] args) {
		if (args == null || args.length == 0) {
			return 0;
		}

		double sum = 0;
		for (final double n : args) {
			sum += n;
		}

		return sum / args.length;
	}
}
