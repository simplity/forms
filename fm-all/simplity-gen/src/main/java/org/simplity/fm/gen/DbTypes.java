package org.simplity.fm.gen;

/**
 * data structure with details about column types for data-base design
 */
public class DbTypes {
	boolean optionalTextIsNullable;
	boolean optionalNumberIsNullable;
	boolean optionalDateIsNullable;
	boolean optionalTimestampIsNullable;
	IntegerDbType[] integerTypes;
	DecimalDbType[] decimalTypes;
}
