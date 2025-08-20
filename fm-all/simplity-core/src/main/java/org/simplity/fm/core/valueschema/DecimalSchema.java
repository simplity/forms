// SPDX-License-Identifier: MIT
package org.simplity.fm.core.valueschema;

/**
 * @author simplity.org
 *
 */
public class DecimalSchema extends ValueSchema {
	private final long minValue;
	private final long maxValue;
	/**
	 * calculated based nbr decimals as a factor to round-off the value to the right
	 * decimal places
	 */
	private final long factor;

	/**
	 *
	 * @param name
	 * @param messageId
	 * @param minValue
	 * @param maxValue
	 * @param nbrDecimals
	 */
	public DecimalSchema(final String name, final String messageId, final long minValue, final long maxValue,
			final int nbrDecimals) {
		this.valueType = ValueType.Decimal;
		this.name = name;
		this.messageId = messageId;
		this.minValue = minValue;
		this.maxValue = maxValue;

		this.maxLength = ("" + this.maxValue).length();
		final int len = ("" + this.minValue).length();
		if (len > this.maxLength) {
			this.maxLength = len;
		}
		this.maxLength += nbrDecimals + 1;
		long f = 10;
		for (int i = 0; i < nbrDecimals; i++) {
			f *= 10;
		}
		this.factor = f;
	}

	@Override
	public Double parse(final String value) {
		try {
			return this.validate(Double.parseDouble(value));
		} catch (final Exception e) {
			return null;
		}
	}

	@Override
	public Double parse(final Object value) {
		if (value instanceof Number) {
			return this.validate(((Number) value).doubleValue());
		}
		return this.parse(value.toString());
	}

	@SuppressWarnings("boxing")
	private Double validate(final double d) {
		final long f = Math.round(d);
		if (f > this.maxValue || f < this.minValue) {
			return null;
		}
		return (double) (d * this.factor / this.factor);
	}
}
