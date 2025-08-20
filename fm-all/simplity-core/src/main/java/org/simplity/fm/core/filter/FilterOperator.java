// SPDX-License-Identifier: MIT
package org.simplity.fm.core.filter;

import org.simplity.fm.core.Conventions;

/**
 *
 * @author simplity.org
 *
 */
public enum FilterOperator {
	/** equal */
	Equal(Conventions.Filter.EQ),
	/** not equal */
	NotEqual(Conventions.Filter.NE),
	/** greater. remember it is greater and not "more" */
	Greater(Conventions.Filter.GT),
	/** greater or equal */
	GreaterOrEqual(Conventions.Filter.GE),
	/**
	 * we prefer to call small rather than less because we say greater and not more
	 * :-)
	 */
	Smaller(Conventions.Filter.LT),
	/** we prefer to smaller to less than more :-) */
	SmallerOrEqual(Conventions.Filter.LE),
	/** like */
	Contains(Conventions.Filter.CONTAINS),
	/** starts with */
	StartsWith(Conventions.Filter.STARTS_WITH),
	/** between */
	Between(Conventions.Filter.BETWEEN),
	/** one in the list */
	In(Conventions.Filter.IN_LIST),
	/** translates to "is not null" in an RDBMS */
	HasValue(Conventions.Filter.HAS_VALUE),
	/** translates to "is null" in an RDBMS */
	HasNoValue(Conventions.Filter.HAS_NO_VALUE);

	private String textValue;

	FilterOperator(String text) {
		this.textValue = text;
	}

	/**
	 * get the text value of this enumeration: like "=" for Equal
	 * 
	 * @return text value of this enumeration: like "=" for Equal
	 */
	public String getText() {
		return this.textValue;
	}

	/**
	 * parse a text into enum
	 *
	 * @param text text to be parsed into enum
	 * @return filter condition, or null if there is no filter for this text
	 */
	public static FilterOperator parse(String text) {
		if (text == null || text.isEmpty() || text.equals(Equal.textValue)) {
			return Equal;
		}
		for (FilterOperator f : FilterOperator.values()) {
			if (f.textValue.equals(text)) {
				return f;
			}
		}
		return null;
	}
}
