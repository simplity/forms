package org.simplity.fm.core.service;

import java.time.Instant;
import java.time.LocalDate;

/**
 * represents a data structure to hold a primitive value that can be null. This
 * design is meant to deal with with an unknown-input and to deal with null in a
 * structured way. Since there is a method to check the actual type of value,
 * including whether it is null or not, getters return a non-null value always,
 * providing a non-null default when the actual value is null
 *
 */
public interface INullableValue {
	/**
	 * same as checking getValueType() == null
	 *
	 * @return true if the value is null, false otherwise
	 */
	boolean isNull();

	/**
	 *
	 * @return non-null
	 */
	InputValueType getValueType();

	/**
	 *
	 * @return can-be null if the value is null.
	 *         long/double/boolean/String/LocalDate/Instant/IInputArray/IINputData
	 */
	Object getValue();

	/**
	 *
	 * @return 0 if the actual value is 0, or if it is null or the valueType is
	 *         non-numeric
	 */
	long getInteger();

	/**
	 *
	 * @return true if the value is of type boolean AND its value is true. false
	 *         otherwise
	 */
	boolean getBoolean();

	/**
	 *
	 * @return non-null string. actual string value if the value type is text. EMpty
	 *         string if it is null. string-value of the value in case it is
	 *         non-text. it is null.
	 */
	String getString();

	/**
	 *
	 * @return 0 if the actual value is 0, or if it is null or the valueType is
	 *         non-numeric
	 */
	double getDecimal();

	/**
	 *
	 * @return date if the value is a valid date, else 0-epoch
	 */
	LocalDate getDate();

	/**
	 *
	 * @return non-null. Instant for 0-epoch in case the value is not a valid
	 *         instant
	 *
	 */
	Instant getTimestamp();

	/**
	 *
	 * @return non-null. if this is not actually an array, then an empty array is
	 *         returned.
	 */
	IInputArray getArray();

	/**
	 *
	 * @return non-null. if this is not actually a a data-object, then an empty data
	 *         is returned.
	 */
	IInputData getData();
}
