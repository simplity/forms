package c:/work/fm-all/example/src/main/java/org/simplity/fm/example/gen/.list;

import org.simplity.fm.core.validn.RuntimeList;

/**
 * AssessmentSchemeList
 */
public class AssessmentSchemeList extends RuntimeList {
	 private static final String NAME = "assessmentSchemeList";
	 private static final String LIST_SQL = "SELECT assessment_scheme_id, name FROM assessment_schemes";
	 private static final String CHECK_SQL = "SELECT assessment_scheme_id FROM assessment_schemes WHERE assessment_scheme_id=?";

/**
 * AssessmentSchemeList
 */
	publicAssessmentSchemeList() {
		this.name = NAME;
		this.listSql = LIST_SQL;
		this.checkSql = CHECK_SQL;
		this.valueIsNumeric = true;
	}
}
