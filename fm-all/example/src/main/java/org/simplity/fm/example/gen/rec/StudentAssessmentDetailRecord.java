package org.simplity.fm.example.gen.rec;

import java.util.List;

import org.simplity.fm.core.data.DbField;
import org.simplity.fm.core.data.DbRecord;
import org.simplity.fm.core.data.Dba;
import org.simplity.fm.core.data.Field;
import org.simplity.fm.core.data.FieldType;
import org.simplity.fm.core.data.RecordMetaData;
import org.simplity.fm.core.serialize.IInputObject;
import org.simplity.fm.core.service.IServiceContext;
import org.simplity.fm.core.validn.IValidation;
import org.simplity.fm.example.gen.DefinedDataTypes;

/**
 * class that represents structure of studentAssessmentDetail
 */
public class StudentAssessmentDetailRecord extends DbRecord {
	private static final Field[] FIELDS = {
			new DbField("studentAssessmentId", 0, DefinedDataTypes.id, false,
					null, null, null, "student_assessment_id",
					FieldType.GeneratedPrimaryKey),
			new DbField("instituteId", 1, DefinedDataTypes.tenantKey, false,
					null, null, null, "institute_id", FieldType.TenantKey),
			new DbField("subjectSectionId", 2, DefinedDataTypes.id, false, null,
					null, null, "subject_section_id", FieldType.OptionalData),
			new DbField("assessmentSchemeId", 3, DefinedDataTypes.id, false,
					null, null, null, "assessment_scheme_id",
					FieldType.OptionalData),
			new DbField("assessmentSeqNo", 4, DefinedDataTypes.integer, false,
					null, null, null, "assessment_seq_no",
					FieldType.RequiredData),
			new DbField("studentId", 5, DefinedDataTypes.id, false, null, null,
					null, "student_id", FieldType.RequiredData),
			new DbField("name", 6, DefinedDataTypes.name, false, null, null,
					null, "name", FieldType.OptionalData),
			new DbField("usn", 7, DefinedDataTypes.text, false, null, null,
					null, "usn", FieldType.OptionalData),
			new DbField("hasAttended", 8, DefinedDataTypes.bool, false, "false",
					null, null, "has_attended", FieldType.OptionalData),
			new DbField("marksScored", 9, DefinedDataTypes.integer, false, "0",
					null, null, "marks_scored", FieldType.OptionalData),
			new DbField("marks", 10, DefinedDataTypes.text, false, null, null,
					null, null, FieldType.OptionalData)};
	private static final boolean[] OPERS = {true, false, false, false, true};
	private static final IValidation[] VALIDS = {};

	private static final RecordMetaData META = new RecordMetaData(
			"studentAssessmentDetail", FIELDS, VALIDS);
	/* DB related */
	private static final String SELECT = "SELECT student_assessment_id, institute_id, subject_section_id, assessment_scheme_id, assessment_seq_no, student_id, name, usn, has_attended, marks_scored, null FROM student_assessment_details";
	private static final int[] SELECT_IDX = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
	private static final String INSERT = "INSERT INTO student_assessment_details(institute_id, subject_section_id, assessment_scheme_id, assessment_seq_no, student_id, name, usn, has_attended, marks_scored, null) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	private static final int[] INSERT_IDX = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
	private static final String WHERE = " WHERE student_assessment_id=? AND institute_id=?";
	private static final int[] WHERE_IDX = {0, 1};
	private static final String UPDATE = "UPDATE student_assessment_details SET subject_section_id= ? , assessment_scheme_id= ? , assessment_seq_no= ? , student_id= ? , name= ? , usn= ? , has_attended= ? , marks_scored= ? , null= ?  WHERE student_assessment_id=? AND institute_id=?";
	private static final int[] UPDATE_IDX = {2, 3, 4, 5, 6, 7, 8, 9, 10, 0, 1};
	private static final String DELETE = "DELETE FROM student_assessment_details";

	private static final Dba DBA = new Dba(FIELDS, "student_assessment_details",
			OPERS, SELECT, SELECT_IDX, INSERT, INSERT_IDX, UPDATE, UPDATE_IDX,
			DELETE, WHERE, WHERE_IDX);

	/** default constructor */
	public StudentAssessmentDetailRecord() {
		super(DBA, META, null);
	}

	/**
	 * @param values
	 *            initial values
	 */
	public StudentAssessmentDetailRecord(Object[] values) {
		super(DBA, META, values);
	}

	@Override
	public StudentAssessmentDetailRecord newInstance(final Object[] values) {
		return new StudentAssessmentDetailRecord(values);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<StudentAssessmentDetailRecord> parseTable(
			final IInputObject inputObject, String memberName,
			final boolean forInsert, final IServiceContext ctx) {
		return (List<StudentAssessmentDetailRecord>) super.parseTable(
				inputObject, memberName, forInsert, ctx);
	}

	/**
	 * set value for studentAssessmentId
	 *
	 * @param value
	 *            to be assigned to studentAssessmentId
	 */
	public void setStudentAssessmentId(String value) {
		this.fieldValues[0] = value;
	}

	/**
	 * @return value of studentAssessmentId
	 */
	public String getStudentAssessmentId() {
		return super.fetchStringValue(0);
	}

	/**
	 * set value for instituteId
	 *
	 * @param value
	 *            to be assigned to instituteId
	 */
	public void setInstituteId(String value) {
		this.fieldValues[1] = value;
	}

	/**
	 * @return value of instituteId
	 */
	public String getInstituteId() {
		return super.fetchStringValue(1);
	}

	/**
	 * set value for subjectSectionId
	 *
	 * @param value
	 *            to be assigned to subjectSectionId
	 */
	public void setSubjectSectionId(String value) {
		this.fieldValues[2] = value;
	}

	/**
	 * @return value of subjectSectionId
	 */
	public String getSubjectSectionId() {
		return super.fetchStringValue(2);
	}

	/**
	 * set value for assessmentSchemeId
	 *
	 * @param value
	 *            to be assigned to assessmentSchemeId
	 */
	public void setAssessmentSchemeId(String value) {
		this.fieldValues[3] = value;
	}

	/**
	 * @return value of assessmentSchemeId
	 */
	public String getAssessmentSchemeId() {
		return super.fetchStringValue(3);
	}

	/**
	 * set value for assessmentSeqNo
	 *
	 * @param value
	 *            to be assigned to assessmentSeqNo
	 */
	public void setAssessmentSeqNo(String value) {
		this.fieldValues[4] = value;
	}

	/**
	 * @return value of assessmentSeqNo
	 */
	public String getAssessmentSeqNo() {
		return super.fetchStringValue(4);
	}

	/**
	 * set value for studentId
	 *
	 * @param value
	 *            to be assigned to studentId
	 */
	public void setStudentId(String value) {
		this.fieldValues[5] = value;
	}

	/**
	 * @return value of studentId
	 */
	public String getStudentId() {
		return super.fetchStringValue(5);
	}

	/**
	 * set value for name
	 *
	 * @param value
	 *            to be assigned to name
	 */
	public void setName(String value) {
		this.fieldValues[6] = value;
	}

	/**
	 * @return value of name
	 */
	public String getName() {
		return super.fetchStringValue(6);
	}

	/**
	 * set value for usn
	 *
	 * @param value
	 *            to be assigned to usn
	 */
	public void setUsn(String value) {
		this.fieldValues[7] = value;
	}

	/**
	 * @return value of usn
	 */
	public String getUsn() {
		return super.fetchStringValue(7);
	}

	/**
	 * set value for hasAttended
	 *
	 * @param value
	 *            to be assigned to hasAttended
	 */
	public void setHasAttended(String value) {
		this.fieldValues[8] = value;
	}

	/**
	 * @return value of hasAttended
	 */
	public String getHasAttended() {
		return super.fetchStringValue(8);
	}

	/**
	 * set value for marksScored
	 *
	 * @param value
	 *            to be assigned to marksScored
	 */
	public void setMarksScored(String value) {
		this.fieldValues[9] = value;
	}

	/**
	 * @return value of marksScored
	 */
	public String getMarksScored() {
		return super.fetchStringValue(9);
	}

	/**
	 * set value for marks
	 *
	 * @param value
	 *            to be assigned to marks
	 */
	public void setMarks(String value) {
		this.fieldValues[10] = value;
	}

	/**
	 * @return value of marks
	 */
	public String getMarks() {
		return super.fetchStringValue(10);
	}
}
