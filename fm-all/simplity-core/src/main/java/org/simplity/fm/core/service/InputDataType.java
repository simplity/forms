package org.simplity.fm.core.service;

/**
 *
 * Types of value a data-member may have
 *
 */
public enum InputDataType {
	/**
	 * value. Could be String, integral, decimal or boolean. Dates are formatted
	 * as string.
	 */
	Value,
	/** array of nodes. Each node is of the same type */
	Array,
	/** a generic data structure */
	DataStructure,
	/** No data */
	NoData,

	/** null, as in Java and JavaScript */
	Null

}
