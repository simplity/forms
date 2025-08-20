package org.simplity.fm.core.service;

import java.time.Instant;
import java.time.LocalDate;

import org.simplity.fm.core.json.JsonUtil;

/**
 * Represents a value that may be null, supporting multiple types such as
 * String, Integer, Boolean, Date, Timestamp, Array, and Data.
 * <p>
 * Provides type-safe accessors and null-checking for framework input values.
 * </p>
 *
 * @see InputValueType
 * @see InputArray
 * @see InputData
 */
public class NullableValue {
	private static final String NULL_STRING = "";
	private static final long NULL_INTEGER = 0;
	private static final double NULL_DECIMAL = 0;
	private static final boolean NULL_BOOLEAN = false;
	private static final LocalDate NULL_DATE = LocalDate.ofEpochDay(0);
	private static final Instant NULL_TIMESTAMP = Instant.ofEpochMilli(0);

	private final InputValueType valueType;
	private final Object value;

	/**
	 * Constructs a null value.
	 */
	public NullableValue() {
		this.value = null;
		this.valueType = InputValueType.NoData;
	}

	/**
	 * Constructs a value from a String.
	 *
	 * @param value the string value, may be null
	 */
	public NullableValue(String value) {
		if (value == null) {
			this.value = null;
			this.valueType = null;
			return;
		}
		this.valueType = InputValueType.Text;
		this.value = value;
	}

	/**
	 * Constructs a value from an Instant.
	 *
	 * @param value the timestamp value, may be null
	 */
	public NullableValue(Instant value) {
		if (value == null) {
			this.value = null;
			this.valueType = null;
			return;
		}
		this.valueType = InputValueType.Timestamp;
		this.value = value;
	}

	/**
	 * Constructs a value from a LocalDate.
	 *
	 * @param value the date value, may be null
	 */
	public NullableValue(LocalDate value) {
		if (value == null) {
			this.value = null;
			this.valueType = null;
			return;
		}
		this.valueType = InputValueType.Date;
		this.value = value;
	}

	/**
	 * Constructs a value from a boolean.
	 *
	 * @param value the boolean value
	 */
	@SuppressWarnings("boxing")
	public NullableValue(boolean value) {
		this.valueType = InputValueType.Boolean;
		this.value = value;
	}

	/**
	 * Constructs a value from a long.
	 *
	 * @param value the integer value
	 */
	@SuppressWarnings("boxing")
	public NullableValue(long value) {
		this.valueType = InputValueType.Integer;
		this.value = value;
	}

	/**
	 * Constructs a value from a double.
	 *
	 * @param value the decimal value
	 */
	@SuppressWarnings("boxing")
	public NullableValue(double value) {
		this.valueType = InputValueType.Decimal;
		this.value = value;
	}

	/**
	 * Constructs a value from an InputArray.
	 *
	 * @param value the array value
	 */
	public NullableValue(InputArray value) {
		this.valueType = InputValueType.Array;
		this.value = value;
	}

	/**
	 * Constructs a value from an InputData.
	 *
	 * @param value the data value
	 */
	public NullableValue(InputData value) {
		this.valueType = InputValueType.Data;
		this.value = value;
	}

	/**
	 * Checks if the value is null.
	 *
	 * @return true if the value is null, false otherwise
	 */
	public boolean isNull() {
		return this.valueType == null;
	}

	/**
	 * Gets the type of the value.
	 *
	 * @return the value type
	 * @see InputValueType
	 */
	public InputValueType getValueType() {
		return this.valueType;
	}

	/**
	 * Gets the raw value object.
	 *
	 * @return the value as Object
	 */
	public Object getValue() {
		return this.value;
	}

	/**
	 * Gets the value as a long integer.
	 *
	 * @return the integer value, or 0 if not applicable
	 */
	public long getInteger() {
		if (this.valueType == InputValueType.Integer || this.valueType == InputValueType.Decimal) {
			return ((Number) this.value).longValue();
		}
		return NULL_INTEGER;
	}

	/**
	 * Gets the value as a boolean.
	 *
	 * @return the boolean value, or false if not applicable
	 */
	@SuppressWarnings("boxing")
	public boolean getBoolean() {
		if (this.valueType == InputValueType.Boolean) {
			return (Boolean) this.value;
		}
		return NULL_BOOLEAN;
	}

	/**
	 * Gets the value as a String.
	 *
	 * @return the string value, or empty string if not applicable
	 */
	public String getString() {
		if (this.value == null) {
			return NULL_STRING;
		}
		return this.value.toString();
	}

	/**
	 * Gets the value as a double.
	 *
	 * @return the decimal value, or 0.0 if not applicable
	 */
	public double getDecimal() {
		if (this.valueType == InputValueType.Integer || this.valueType == InputValueType.Decimal) {
			return ((Number) this.value).doubleValue();
		}
		return NULL_DECIMAL;
	}

	/**
	 * Gets the value as a LocalDate.
	 *
	 * @return the date value, or epoch date if not applicable
	 */
	public LocalDate getDate() {
		if (this.valueType == InputValueType.Timestamp) {
			return (LocalDate) this.value;
		}
		return NULL_DATE;
	}

	/**
	 * Gets the value as an Instant.
	 *
	 * @return the timestamp value, or epoch if not applicable
	 */
	public Instant getTimestamp() {
		if (this.valueType == InputValueType.Timestamp) {
			return (Instant) this.value;
		}
		return NULL_TIMESTAMP;
	}

	/**
	 * Gets the value as an InputArray.
	 *
	 * @return the array value, or empty array if not applicable
	 * @see InputArray
	 */
	public InputArray getArray() {
		if (this.valueType == InputValueType.Array) {
			return (InputArray) this.value;
		}
		return JsonUtil.newInputArray();
	}

	/**
	 * Gets the value as an InputData.
	 *
	 * @return the data value, or empty data if not applicable
	 * @see InputData
	 */
	public InputData getData() {
		if (this.valueType == InputValueType.Array) {
			return (InputData) this.value;
		}
		return JsonUtil.newInputData();
	}

	/**
	 * Returns the string representation of the value.
	 *
	 * @return string representation
	 */
	@Override
	public String toString() {
		return this.getString();
	}
}
