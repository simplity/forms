package org.simplity.fm.example.gen.list;

import org.simplity.fm.core.validn.RuntimeList;

/**
 * AssessmentItemList
 */
public class AssessmentItemList extends RuntimeList {
	 private static final String NAME = "assessmentItemList";
	 private static final String LIST_SQL = "SELECT seq_no, name FROM assessment_items WHERE assessment_scheme_id=?";
	 private static final String CHECK_SQL = "SELECT seq_no FROM assessment_items WHERE seq_no=? AND assessment_scheme_id=?";

/**
 * AssessmentItemList
 */
	public AssessmentItemList() {
		this.name = NAME;
		this.listSql = LIST_SQL;
		this.checkSql = CHECK_SQL;
		this.column1IsNumeric = true;
		this.hasKey = true;
		this.keyIsNumeric = true;
	}
}
