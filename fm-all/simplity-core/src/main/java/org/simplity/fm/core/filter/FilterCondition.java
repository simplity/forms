package org.simplity.fm.core.filter;

/**
 * data structure to specify a filter condition like eventDate between date1 and
 * date2
 */
public class FilterCondition {
	/**
	 * field that is to be used for comparison
	 */
	public String field;
	/**
	 * as per the standard like '=', '<=' etc..
	 */
	public String comparator;
	/**
	 * value to be compared with. This is the text value of the right type. for
	 * example, it is "true" or "false' This can also be of the form ${fieldName} to
	 * use another field in the record for comparison
	 */
	public String value;
	/**
	 * used if the comparator is between
	 */
	public String toValue;
}
