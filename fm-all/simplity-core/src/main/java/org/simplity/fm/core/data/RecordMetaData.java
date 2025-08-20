// SPDX-License-Identifier: MIT
package org.simplity.fm.core.data;

import java.util.Map;

import org.simplity.fm.core.validn.FormDataValidation;

/**
 * Meta data for a record.
 *
 * @author simplity.org
 *
 */
public class RecordMetaData {
	private final String name;
	/**
	 * fields that make up this record. These fields to bo
	 */
	private final Field[] fields;

	/**
	 * describes all the inter-field validations, and any business validations
	 */
	private final FormDataValidation[] validations;

	/**
	 *
	 * @param name
	 * @param fields
	 * @param validations
	 */
	public RecordMetaData(final String name, final Field[] fields, final FormDataValidation[] validations) {
		this.name = name;
		this.fields = fields;
		this.validations = validations;
	}

	/**
	 * over ride meta data
	 *
	 * @param over
	 */
	public void override(final RecordOverride over) {
		final Map<String, FieldOverride> newFields = over.fields;
		for (final Field field : this.fields) {
			final FieldOverride newField = newFields.get(field.getName());
			if (newField != null) {
				field.override(newField);
			}
		}
	}

	/**
	 * construct a record that is used locally, and is not part of components to
	 * be discovered by others. In such a case, there is no need to provide a
	 * unique name, and no need for validations
	 *
	 * @param fields
	 */
	public RecordMetaData(final Field[] fields) {
		this.name = this.getClass().getName().toString();
		this.fields = fields;
		this.validations = null;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @return the fields
	 */
	public Field[] getFields() {
		return this.fields;
	}

	/**
	 * @param idx
	 * @return the field at idx, or null if idx is not a valid index
	 */
	public Field getField(final int idx) {
		if (idx < 0 || idx >= this.fields.length) {
			return null;
		}
		return this.fields[idx];
	}

	/**
	 * @return the validations
	 */
	public FormDataValidation[] getValidations() {
		return this.validations;
	}

	/**
	 *
	 * @return array with default values for fields. if a default is not set, it
	 *         would contain null
	 */
	public Object[] getDefaultValues() {
		final Object[] values = new Object[this.fields.length];
		for (int i = 0; i < values.length; i++) {
			values[i] = this.fields[i].getDefaultValue();
		}
		return values;
	}
}
