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
 * class that represents structure of studentDetail
 */
public class StudentDetailRecord extends DbRecord {
	private static final Field[] FIELDS = {
			new DbField("studentId", 0, DefinedDataTypes.flexibleId, false,
					"-1", null, null, "student_id", FieldType.PrimaryKey),
			new DbField("instituteId", 1, DefinedDataTypes.tenantKey, false,
					null, null, null, "institute_id", FieldType.TenantKey),
			new DbField("departmentId", 2, DefinedDataTypes.id, false, null,
					null, null, "department_id", FieldType.RequiredData),
			new DbField("departmentName", 3, DefinedDataTypes.text, false, null,
					null, null, "department_name", FieldType.RequiredData),
			new DbField("usn", 4, DefinedDataTypes.text, false, null, null,
					null, "usn", FieldType.OptionalData),
			new DbField("name", 5, DefinedDataTypes.name, false, null, null,
					null, "name", FieldType.RequiredData),
			new DbField("phoneNumber", 6, DefinedDataTypes.phone, false, null,
					null, null, "phone_number", FieldType.RequiredData)};
	private static final boolean[] OPERS = {true, true, true, true, true};
	private static final IValidation[] VALIDS = {};

	private static final RecordMetaData META = new RecordMetaData(
			"studentDetail", FIELDS, VALIDS);
	/* DB related */
	private static final String SELECT = "SELECT student_id, institute_id, department_id, department_name, usn, name, phone_number FROM student_details";
	private static final int[] SELECT_IDX = {0, 1, 2, 3, 4, 5, 6};
	private static final String INSERT = "INSERT INTO student_details(student_id, institute_id, department_id, department_name, usn, name, phone_number) values (?, ?, ?, ?, ?, ?, ?)";
	private static final int[] INSERT_IDX = {0, 1, 2, 3, 4, 5, 6};
	private static final String WHERE = " WHERE student_id=? AND institute_id=?";
	private static final int[] WHERE_IDX = {0, 1};
	private static final String UPDATE = "UPDATE student_details SET department_id= ? , department_name= ? , usn= ? , name= ? , phone_number= ?  WHERE student_id=? AND institute_id=?";
	private static final int[] UPDATE_IDX = {2, 3, 4, 5, 6, 0, 1};
	private static final String DELETE = "DELETE FROM student_details";

	private static final Dba DBA = new Dba(FIELDS, "student_details", OPERS,
			SELECT, SELECT_IDX, INSERT, INSERT_IDX, UPDATE, UPDATE_IDX, DELETE,
			WHERE, WHERE_IDX);

	/** default constructor */
	public StudentDetailRecord() {
		super(DBA, META, null);
	}

	/**
	 * @param values
	 *            initial values
	 */
	public StudentDetailRecord(Object[] values) {
		super(DBA, META, values);
	}

	@Override
	public StudentDetailRecord newInstance(final Object[] values) {
		return new StudentDetailRecord(values);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<StudentDetailRecord> parseTable(final IInputObject inputObject,
			String memberName, final boolean forInsert,
			final IServiceContext ctx) {
		return (List<StudentDetailRecord>) super.parseTable(inputObject,
				memberName, forInsert, ctx);
	}

	/**
	 * set value for studentId
	 *
	 * @param value
	 *            to be assigned to studentId
	 */
	public void setStudentId(String value) {
		this.fieldValues[0] = value;
	}

	/**
	 * @return value of studentId
	 */
	public String getStudentId() {
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
	 * set value for departmentId
	 *
	 * @param value
	 *            to be assigned to departmentId
	 */
	public void setDepartmentId(String value) {
		this.fieldValues[2] = value;
	}

	/**
	 * @return value of departmentId
	 */
	public String getDepartmentId() {
		return super.fetchStringValue(2);
	}

	/**
	 * set value for departmentName
	 *
	 * @param value
	 *            to be assigned to departmentName
	 */
	public void setDepartmentName(String value) {
		this.fieldValues[3] = value;
	}

	/**
	 * @return value of departmentName
	 */
	public String getDepartmentName() {
		return super.fetchStringValue(3);
	}

	/**
	 * set value for usn
	 *
	 * @param value
	 *            to be assigned to usn
	 */
	public void setUsn(String value) {
		this.fieldValues[4] = value;
	}

	/**
	 * @return value of usn
	 */
	public String getUsn() {
		return super.fetchStringValue(4);
	}

	/**
	 * set value for name
	 *
	 * @param value
	 *            to be assigned to name
	 */
	public void setName(String value) {
		this.fieldValues[5] = value;
	}

	/**
	 * @return value of name
	 */
	public String getName() {
		return super.fetchStringValue(5);
	}

	/**
	 * set value for phoneNumber
	 *
	 * @param value
	 *            to be assigned to phoneNumber
	 */
	public void setPhoneNumber(String value) {
		this.fieldValues[6] = value;
	}

	/**
	 * @return value of phoneNumber
	 */
	public String getPhoneNumber() {
		return super.fetchStringValue(6);
	}
}
