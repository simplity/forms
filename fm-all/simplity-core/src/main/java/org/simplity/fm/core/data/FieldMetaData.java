// SPDX-License-Identifier: MIT
package org.simplity.fm.core.data;

import org.simplity.fm.core.valueschema.ValueType;

/**
 * @author simplity.org
 *
 */
public class FieldMetaData {
	/**
	 * 0-based index in the form-fields that this parameter corresponds to (for
	 * getting/setting value in form data array)
	 */
	private final int idx;
	/**
	 * value type of this parameter based on which set/get method is ssued on
	 * the statement
	 */
	private final ValueType valueType;

	/**
	 * create this parameter as an immutable data structure
	 *
	 * @param idx
	 * @param valueType
	 */
	public FieldMetaData(final int idx, final ValueType valueType) {
		this.idx = idx;
		this.valueType = valueType;
	}

	/**
	 * create this parameter as an immutable data structure
	 *
	 * @param field
	 */
	public FieldMetaData(final Field field) {
		this.idx = field.getIndex();
		this.valueType = field.getValueType();
	}

	/**
	 * append this parameter to a message that describes the run time values of
	 * a sql
	 *
	 * @param buf
	 * @param posn
	 * @param values
	 * @return buffer for convenience
	 */
	public StringBuilder toMessage(final StringBuilder buf, final int posn,
			final Object[] values) {
		buf.append('\n').append(posn).append(". type=").append(this.valueType);
		buf.append(" value=").append(values[this.idx]);
		return buf;
	}

	/**
	 *
	 * @return index of this field in the data row
	 */
	public int getIndex() {
		return this.idx;
	}

	/**
	 * @return the valueType
	 */
	public ValueType getValueType() {
		return this.valueType;
	}

}
