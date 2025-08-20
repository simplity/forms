// SPDX-License-Identifier: MIT
package org.simplity.fm.core.valueschema;

/**
 * validation parameters for a an integral value
 *
 * @author simplity.org
 *
 */
public class IntegerSchema extends ValueSchema {
	private final long minValue;
	private final long maxValue;

	/**
	 *
	 * @param name
	 * @param messageId
	 * @param minValue
	 * @param maxValue
	 */
	public IntegerSchema(String name, String messageId, long minValue, long maxValue) {
		this.valueType = ValueType.Integer;
		this.name = name;
		this.messageId = messageId;
		this.minValue = minValue;
		this.maxValue = maxValue;

		this.maxLength = ("" + this.maxValue).length();
		int len = ("" + this.minValue).length();
		if (len > this.maxLength) {
			this.maxLength = len;
		}
	}

	@SuppressWarnings("boxing")
	private Long validate(long value) {
		if (value >= this.minValue && value <= this.maxValue) {
			return value;
		}
		return null;
	}

	@Override
	public Long parse(Object object) {
		if (object instanceof Number) {
			return this.validate(((Number) object).longValue());
		}
		if (object instanceof String) {
			return this.parse((String) object);
		}
		return null;
	}

	@Override
	public Long parse(String value) {
		try {
			return this.validate(Long.parseLong(value));
		} catch (Exception e) {
			return null;
		}
	}
}
