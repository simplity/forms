package org.simplity.fm.gen.db;

import org.simplity.fm.core.data.Field;
import org.simplity.fm.core.valueschema.ValueType;

/**
 * data structure of metadata of a column
 */
public class ColumnMetaRecord extends org.simplity.fm.core.data.Record {
	private static final Field[] FIELDS = { new Field("name", 0, ValueType.Text, null, true, null),
			new Field("nameInDb", 1, ValueType.Text, null, false, null),
			new Field("description", 2, ValueType.Text, null, false, null),
			new Field("fieldType", 3, ValueType.Text, null, false, null),
			new Field("valueType", 4, ValueType.Text, null, false, null),
			new Field("renderAs", 5, ValueType.Text, null, false, null),
			new Field("label", 6, ValueType.Text, null, false, null),
			new Field("valueTypeInfo", 7, ValueType.Text, null, false, null) };

	/** default constructor */
	public ColumnMetaRecord() {
		super(FIELDS, null);
	}

	/**
	 * @param values initial values
	 */
	public ColumnMetaRecord(Object[] values) {
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

	public void setFieldType(String fieldType) {
		this.assignStringValue(3, fieldType);
	}

	public String getFieldtype() {
		return this.fetchStringValue(3);
	}

	/**
	 * string value of value type like "text", "boolean" etc..
	 * 
	 * @param valueSchema
	 */
	public void setValueType(String valueType) {
		this.assignStringValue(4, valueType);
	}

	/**
	 * string value of value type like "text", "boolean" etc..
	 * 
	 * @param valueSchema
	 */
	public String getValueType() {
		return this.fetchStringValue(4);
	}

	public void setRenderAs(String renderAs) {
		this.assignStringValue(5, renderAs);
	}

	public String getRenderAs() {
		return this.fetchStringValue(5);
	}

	public void setLabel(String label) {
		this.assignStringValue(6, label);
	}

	public String getLabel() {
		return this.fetchStringValue(6);
	}

	public void setValueTypeInfo(String valueTypeInfo) {
		this.assignStringValue(7, valueTypeInfo);
	}

	public String getValueTypeInfo() {
		return this.fetchStringValue(7);
	}

}
