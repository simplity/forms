package org.simplity.fm.core.service;

/**
 *
 * Types of value a data-member may have
 *
 */
public enum InputValueType {
	/** No data, either no member with the name, or is null */
	NoData,
	/**
	 * Text value. If the text value is of the form yyyy-mm-dd, then it is
	 * treated as Date, and if it is yyyy-mm-ddThh:MM:ss.fff then it is treated
	 * as timestamp
	 */
	Text,
	/**
	 * true/false
	 */
	Boolean,
	/**
	 * numeric value with no fraction. 1.0 is considered decimal
	 */
	Integer,
	/**
	 * numeric with fraction. 1.0 is considered decimal
	 */
	Decimal,
	/**
	 * String of the form yyyy-mm-dd is parsed into local date
	 */
	Date,

	/**
	 * string of the form yyyy-mm-ddThh:MM:ss.fff
	 */
	Timestamp,
	/** array of nodes. Each node is of the same type */
	Array,
	/** a generic data structure */
	Data,

}
