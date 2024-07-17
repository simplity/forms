package org.simplity.fm.core.db;

import java.sql.SQLException;
import java.time.LocalDate;

import org.simplity.fm.core.data.Field;
import org.simplity.fm.core.data.Record;
import org.simplity.fm.core.valueschema.ValueType;

/**
 * Template for Code Generator
 *
 *
 */
public class ExampleStoredPrecedureSql extends StoredProcedure {
	private static final String PROC_NAME = "example_stored_procedure";
	private static final ValueType RET_TYPE = ValueType.Text;
	private static final Class<?>[] SP_OUT_CLASSES = new Class<?>[]{
			/* AppSpecificRecord.classes or nulls */};
	private static final Field[] IN_FIELDS = new Field[]{};
	private static final ValueType[] OUT_TYPES = null;

	/**
	 * do some work on the db blah, blah...
	 */
	public ExampleStoredPrecedureSql() {
		super(PROC_NAME, RET_TYPE, SP_OUT_CLASSES, IN_FIELDS, OUT_TYPES);
	}

	/*
	 * setters/getters if input/output Fields are used. Not to be generated when
	 * input/output records are used
	 */

	/**
	 * @param value
	 */
	public void setXXX(boolean value) {
		this.inputValues[0] = value;
	}

	/**
	 * @return value
	 */
	public LocalDate getYYY() {
		return (LocalDate) this.outputValues[1];
	}

	/*
	 * expose the desired I/O method . Ensure that the Method signature uses the
	 * right extended-Record class, instead of teh generic Record class
	 */

	@Override
	public boolean read(IReadonlyHandle handle, Record record)
			throws SQLException {
		return super.read(handle, record);
	}

}
