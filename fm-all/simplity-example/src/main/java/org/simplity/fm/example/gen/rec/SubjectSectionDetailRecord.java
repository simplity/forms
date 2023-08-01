package org.simplity.fm.example.gen.rec;

import java.util.List;

import org.simplity.fm.core.data.DbField;
import org.simplity.fm.core.data.DbRecord;
import org.simplity.fm.core.data.Dba;
import org.simplity.fm.core.data.Field;
import org.simplity.fm.core.data.FieldType;
import org.simplity.fm.core.data.RecordMetaData;
import org.simplity.fm.core.service.IInputData;
import org.simplity.fm.core.service.IServiceContext;
import org.simplity.fm.core.validn.IValidation;
import org.simplity.fm.example.gen.DefinedValueSchemas;

/**
 * class that represents structure of subjectSectionDetail
 */
public class SubjectSectionDetailRecord extends DbRecord {
	private static final Field[] FIELDS = {
			new DbField("subjectSectionId", 0, DefinedValueSchemas.id, false, null,
					null, null, "subject_section_id", FieldType.PrimaryKey),
			new DbField("instituteId", 1, DefinedValueSchemas.tenantKey, false,
					null, null, null, "institute_id", FieldType.TenantKey),
			new DbField("offeredSubjectId", 2, DefinedValueSchemas.id, false, "0",
					null, null, "offered_subject_id", FieldType.OptionalData),
			new DbField("subjectId", 3, DefinedValueSchemas.id, false, "0", null,
					null, "subject_id", FieldType.OptionalData),
			new DbField("levelSectionId", 4, DefinedValueSchemas.id, false, "0",
					null, null, "level_section_id", FieldType.OptionalData),
			new DbField("departmentId", 5, DefinedValueSchemas.id, false, null,
					null, null, "department_id", FieldType.OptionalData),
			new DbField("sectionId", 6, DefinedValueSchemas.id, false, null, null,
					null, "section_id", FieldType.OptionalData),
			new DbField("subjectName", 7, DefinedValueSchemas.name, false, null,
					null, null, "subject_name", FieldType.OptionalData),
			new DbField("subjectCode", 8, DefinedValueSchemas.name, false, null,
					null, null, "subject_code", FieldType.OptionalData),
			new DbField("sectionName", 9, DefinedValueSchemas.name, false, null,
					null, null, "section_name", FieldType.OptionalData),
			new DbField("totalClasses", 10, DefinedValueSchemas.integer, false,
					null, null, null, "total_classes", FieldType.OptionalData),
			new DbField("attendanceFrozen", 11, DefinedValueSchemas.bool, false,
					"false", null, null, "attendance_frozen",
					FieldType.OptionalData),
			new DbField("cieFrozen", 12, DefinedValueSchemas.bool, false, "false",
					null, null, "cie_frozen", FieldType.OptionalData),
			new DbField("isOffered", 13, DefinedValueSchemas.bool, false, "false",
					null, null, "is_offered", FieldType.OptionalData)};
	private static final boolean[] OPERS = {true, false, true, false, true};
	private static final IValidation[] VALIDS = {};

	private static final RecordMetaData META = new RecordMetaData(
			"subjectSectionDetail", FIELDS, VALIDS);
	/* DB related */
	private static final String SELECT = "SELECT subject_section_id, institute_id, offered_subject_id, subject_id, level_section_id, department_id, section_id, subject_name, subject_code, section_name, total_classes, attendance_frozen, cie_frozen, is_offered FROM subject_section_details";
	private static final int[] SELECT_IDX = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
			11, 12, 13};
	private static final String INSERT = "INSERT INTO subject_section_details(subject_section_id, institute_id, offered_subject_id, subject_id, level_section_id, department_id, section_id, subject_name, subject_code, section_name, total_classes, attendance_frozen, cie_frozen, is_offered) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	private static final int[] INSERT_IDX = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
			11, 12, 13};
	private static final String WHERE = " WHERE subject_section_id=? AND institute_id=?";
	private static final int[] WHERE_IDX = {0, 1};
	private static final String UPDATE = "UPDATE subject_section_details SET offered_subject_id= ? , subject_id= ? , level_section_id= ? , department_id= ? , section_id= ? , subject_name= ? , subject_code= ? , section_name= ? , total_classes= ? , attendance_frozen= ? , cie_frozen= ? , is_offered= ?  WHERE subject_section_id=? AND institute_id=?";
	private static final int[] UPDATE_IDX = {2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12,
			13, 0, 1};
	private static final String DELETE = "DELETE FROM subject_section_details";

	private static final Dba DBA = new Dba(FIELDS, "subject_section_details",
			OPERS, SELECT, SELECT_IDX, INSERT, INSERT_IDX, UPDATE, UPDATE_IDX,
			DELETE, WHERE, WHERE_IDX);

	/** default constructor */
	public SubjectSectionDetailRecord() {
		super(DBA, META, null);
	}

	/**
	 * @param values
	 *            initial values
	 */
	public SubjectSectionDetailRecord(Object[] values) {
		super(DBA, META, values);
	}

	@Override
	public SubjectSectionDetailRecord newInstance(final Object[] values) {
		return new SubjectSectionDetailRecord(values);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<SubjectSectionDetailRecord> parseTable(
			final IInputData inputObject, String memberName,
			final boolean forInsert, final IServiceContext ctx) {
		return (List<SubjectSectionDetailRecord>) super.parseTable(inputObject,
				memberName, forInsert, ctx);
	}

	/**
	 * set value for subjectSectionId
	 *
	 * @param value
	 *            to be assigned to subjectSectionId
	 */
	public void setSubjectSectionId(String value) {
		this.fieldValues[0] = value;
	}

	/**
	 * @return value of subjectSectionId
	 */
	public String getSubjectSectionId() {
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
	 * set value for offeredSubjectId
	 *
	 * @param value
	 *            to be assigned to offeredSubjectId
	 */
	public void setOfferedSubjectId(String value) {
		this.fieldValues[2] = value;
	}

	/**
	 * @return value of offeredSubjectId
	 */
	public String getOfferedSubjectId() {
		return super.fetchStringValue(2);
	}

	/**
	 * set value for subjectId
	 *
	 * @param value
	 *            to be assigned to subjectId
	 */
	public void setSubjectId(String value) {
		this.fieldValues[3] = value;
	}

	/**
	 * @return value of subjectId
	 */
	public String getSubjectId() {
		return super.fetchStringValue(3);
	}

	/**
	 * set value for levelSectionId
	 *
	 * @param value
	 *            to be assigned to levelSectionId
	 */
	public void setLevelSectionId(String value) {
		this.fieldValues[4] = value;
	}

	/**
	 * @return value of levelSectionId
	 */
	public String getLevelSectionId() {
		return super.fetchStringValue(4);
	}

	/**
	 * set value for departmentId
	 *
	 * @param value
	 *            to be assigned to departmentId
	 */
	public void setDepartmentId(String value) {
		this.fieldValues[5] = value;
	}

	/**
	 * @return value of departmentId
	 */
	public String getDepartmentId() {
		return super.fetchStringValue(5);
	}

	/**
	 * set value for sectionId
	 *
	 * @param value
	 *            to be assigned to sectionId
	 */
	public void setSectionId(String value) {
		this.fieldValues[6] = value;
	}

	/**
	 * @return value of sectionId
	 */
	public String getSectionId() {
		return super.fetchStringValue(6);
	}

	/**
	 * set value for subjectName
	 *
	 * @param value
	 *            to be assigned to subjectName
	 */
	public void setSubjectName(String value) {
		this.fieldValues[7] = value;
	}

	/**
	 * @return value of subjectName
	 */
	public String getSubjectName() {
		return super.fetchStringValue(7);
	}

	/**
	 * set value for subjectCode
	 *
	 * @param value
	 *            to be assigned to subjectCode
	 */
	public void setSubjectCode(String value) {
		this.fieldValues[8] = value;
	}

	/**
	 * @return value of subjectCode
	 */
	public String getSubjectCode() {
		return super.fetchStringValue(8);
	}

	/**
	 * set value for sectionName
	 *
	 * @param value
	 *            to be assigned to sectionName
	 */
	public void setSectionName(String value) {
		this.fieldValues[9] = value;
	}

	/**
	 * @return value of sectionName
	 */
	public String getSectionName() {
		return super.fetchStringValue(9);
	}

	/**
	 * set value for totalClasses
	 *
	 * @param value
	 *            to be assigned to totalClasses
	 */
	public void setTotalClasses(String value) {
		this.fieldValues[10] = value;
	}

	/**
	 * @return value of totalClasses
	 */
	public String getTotalClasses() {
		return super.fetchStringValue(10);
	}

	/**
	 * set value for attendanceFrozen
	 *
	 * @param value
	 *            to be assigned to attendanceFrozen
	 */
	public void setAttendanceFrozen(String value) {
		this.fieldValues[11] = value;
	}

	/**
	 * @return value of attendanceFrozen
	 */
	public String getAttendanceFrozen() {
		return super.fetchStringValue(11);
	}

	/**
	 * set value for cieFrozen
	 *
	 * @param value
	 *            to be assigned to cieFrozen
	 */
	public void setCieFrozen(String value) {
		this.fieldValues[12] = value;
	}

	/**
	 * @return value of cieFrozen
	 */
	public String getCieFrozen() {
		return super.fetchStringValue(12);
	}

	/**
	 * set value for isOffered
	 *
	 * @param value
	 *            to be assigned to isOffered
	 */
	public void setIsOffered(String value) {
		this.fieldValues[13] = value;
	}

	/**
	 * @return value of isOffered
	 */
	public String getIsOffered() {
		return super.fetchStringValue(13);
	}
}
