package org.simplity.fm.example.gen.list;

import org.simplity.fm.core.validn.ValueList;

/**
 * RelationType
 */

public class RelationType extends ValueList {
	private static final Object[][] VALUES = { 
		{"Mother", "Mother"}, 
		{"Father", "Father"}, 
		{"Legal Guardian", "Legal Guardian"}
	};
	 private static final String NAME = "relationType";

	/**
	 * @param name
	 * @param valueList
	 */
	public RelationType(String name, Object[][] valueList) {
		super(name, valueList);
	}

	/**
	 *relationType
	 */
	public RelationType() {
		super(NAME, VALUES);
	}
}
