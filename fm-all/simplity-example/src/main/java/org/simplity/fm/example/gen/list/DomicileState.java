package org.simplity.fm.example.gen.list;

import org.simplity.fm.core.validn.SimpleValueList;

/**
 * DomicileState
 */

public class DomicileState extends SimpleValueList {
	private static final Object[][] VALUES = { 
		{"Karnataka", "Karnataka"}, 
		{"Non-Karnataka", "Non-Karnataka"}, 
		{"Foreign", "Foreign"}
	};
	 private static final String NAME = "domicileState";

	/**
	 * @param name
	 * @param valueList
	 */
	public DomicileState(String name, Object[][] valueList) {
		super(name, valueList);
	}

	/**
	 *domicileState
	 */
	public DomicileState() {
		super(NAME, VALUES);
	}
}
