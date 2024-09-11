package org.simplity.fm.core.validn;

public enum InterFieldValidationType {
	/**
	 * either both fields have values or both are empty
	 */
	BothOrNone,
	/**
	 * if first one has a value, second one also must have a value. If first one has
	 * no value, then there is no restriction on the second one.
	 */
	BothOrSecond,
	/**
	 * both must have the same value
	 */
	Equal,
	/**
	 * the two must have different values
	 */
	Different,
	/**
	 * one and only of the two must have a value
	 */
	OneOf,
	/**
	 * from-to-pair. Field2 must have a value greater than the value of the first
	 * one
	 */
	Range,
	/**
	 * field2 must have a value greater than or equal to the first one
	 */
	RangeOrEqual;
}
