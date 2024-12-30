package org.simplity.fm.example.gen.list;

import org.simplity.fm.core.validn.RuntimeList;

/**
 * ProgramLevelList
 */
public class ProgramLevelList extends RuntimeList {
	 private static final String NAME = "programLevelList";
	 private static final String LIST_SQL = "SELECT level_id, name FROM program_levels WHERE program_id=?";
	 private static final String CHECK_SQL = "SELECT level_id FROM program_levels WHERE level_id=? AND program_id=?";

/**
 * ProgramLevelList
 */
	public ProgramLevelList() {
		this.name = NAME;
		this.listSql = LIST_SQL;
		this.checkSql = CHECK_SQL;
		this.column1IsNumeric = true;
		this.hasKey = true;
		this.keyIsNumeric = true;
	}
}
