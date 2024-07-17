package org.simplity.fm.gen;

import java.util.Map;

import org.simplity.fm.core.valueschema.ValueType;

/**
 * input field for a SQL
 *
 * @author simplity.org
 *
 */
public class InputField implements IField {
	String name;
	String valueSchema;
	boolean isRequired;
	String defaultValue;

	ValueType valueType;
	int index;

	@Override
	public void init(Map<String, ValueSchema> schemas, int i) {
		this.index = i;
		ValueSchema vs = schemas.get(this.valueSchema);
		this.valueType = vs == null ? ValueType.Text : vs.valueTypeEnum;

	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public ValueType getValueType() {
		return this.valueType;
	}

	@Override
	public int getIndex() {
		return this.index;
	}
}
