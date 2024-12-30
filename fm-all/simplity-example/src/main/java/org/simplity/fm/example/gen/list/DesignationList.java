package org.simplity.fm.example.gen.list;

import org.simplity.fm.core.validn.RuntimeList;

/**
 * DesignationList
 */
public class DesignationList extends RuntimeList {
	 private static final String NAME = "designationList";
	 private static final String LIST_SQL = "SELECT designation_id, name FROM designations";
	 private static final String CHECK_SQL = "SELECT designation_id FROM designations WHERE designation_id=?";

/**
 * DesignationList
 */
	public DesignationList() {
		this.name = NAME;
		this.listSql = LIST_SQL;
		this.checkSql = CHECK_SQL;
		this.column1IsNumeric = true;
	}
}
