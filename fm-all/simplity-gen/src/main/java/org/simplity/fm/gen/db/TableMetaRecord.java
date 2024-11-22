package org.simplity.fm.gen.db;

import org.simplity.fm.core.data.Field;
import org.simplity.fm.core.valueschema.ValueType;

/**
 * class that represents structure of meta data about a db table/view
 */
public class TableMetaRecord extends org.simplity.fm.core.data.Record {
	private static final Field[] FIELDS = { new Field("name", 0, ValueType.Text, null, true, null),
			new Field("nameInDb", 1, ValueType.Text, null, true, null),
			new Field("description", 2, ValueType.Text, null, false, null),
			new Field("isView", 3, ValueType.Boolean, null, false, null) };

	/** default constructor */
	public TableMetaRecord() {
		super(FIELDS, null);
	}

	/**
	 * @param values initial values
	 */
	public TableMetaRecord(Object[] values) {
		super(FIELDS, values);
	}

	public void setName(String name) {
		this.assignStringValue(0, name);
	}

	public String getName() {
		return this.fetchStringValue(0);
	}

	public void setNameInDb(String nameInDb) {
		this.assignStringValue(1, nameInDb);
	}

	public String getNameInDb() {
		return this.fetchStringValue(1);
	}

	public void setDescription(String description) {
		this.assignStringValue(2, description);
	}

	public String getDescription() {
		return this.fetchStringValue(2);
	}

	public void setIsView(boolean isView) {
		this.assignBoolValue(3, isView);
	}

	public boolean getIsView() {
		return this.fetchBoolValue(3);
	}

}
