package org.simplity.fm.example.gen.list;

import org.simplity.fm.core.validn.RuntimeList;

/**
 * InstituteList
 */
public class InstituteList extends RuntimeList {
	 private static final String NAME = "instituteList";
	 private static final String LIST_SQL = "SELECT institute_id, name FROM institutes WHERE trust_id=?";
	 private static final String CHECK_SQL = "SELECT institute_id FROM institutes WHERE institute_id=? AND trust_id=?";

/**
 * InstituteList
 */
	public InstituteList() {
		this.name = NAME;
		this.listSql = LIST_SQL;
		this.checkSql = CHECK_SQL;
		this.valueIsNumeric = true;
		this.hasKey = true;
		this.keyIsNumeric = true;
	}
}
