package org.simplity.fm.core.db;

import org.simplity.fm.core.data.Field;
import org.simplity.fm.core.data.Record;
import org.simplity.fm.core.valueschema.ValueSchema;
import org.simplity.fm.core.valueschema.ValueType;

/**
 * Record that is used by StoredProcedureResult to return nbrRows affected as a
 * dataTable
 *
 */
public class NbrRowsAffectedRecord extends Record {

	private static final Field[] FIELDS = new Field[]{new Field(
			"nbrAffectedRows", 0,
			ValueSchema.defaultTextSchema(ValueType.Integer), false, null)};
	/**
	 * default constructor
	 */
	public NbrRowsAffectedRecord() {
		super(FIELDS, null);
	}

	/**
	 *
	 * @return number of affected rows
	 */
	public int getNbrAffectedRows() {
		return (int) this.fieldValues[0];
	}

}
