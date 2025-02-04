package org.simplity.fm.example.gen.list;

import org.simplity.fm.core.validn.RuntimeList;

/**
 * LevelSectionList
 */
public class LevelSectionList extends RuntimeList {
	 private static final String NAME = "levelSectionList";
	 private static final String LIST_SQL = "SELECT level_section_id, name FROM level_section_details WHERE level_id=?";
	 private static final String CHECK_SQL = "SELECT level_section_id FROM level_section_details WHERE level_section_id=? AND level_id=?";

/**
 * LevelSectionList
 */
	public LevelSectionList() {
		this.name = NAME;
		this.listSql = LIST_SQL;
		this.checkSql = CHECK_SQL;
		this.column1IsNumeric = true;
		this.hasKey = true;
		this.keyIsNumeric = true;
	}
}
