/*
 * Copyright (c) 2019 simplity.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.simplity.fm.core.datatypes;

/**
 * class that restricts possible valid values that a field can have. Used for
 * parsing and validating a field value coming from a non-reliable source
 *
 * @author simplity.org
 *
 */
public abstract class DataType {
	protected String name;
	protected String messageId;
	protected int minLength;
	protected int maxLength;
	protected ValueType valueType;

	/**
	 * @return unique error message id that has the actual error message to be
	 *         used if a value fails validation
	 */
	public String getMessageId() {
		return this.messageId;
	}

	/**
	 * @return the valueType
	 */
	public ValueType getValueType() {
		return this.valueType;
	}

	/**
	 * @param value
	 *            non-null. generic object to be to be parsed and validated.
	 * @return null if the validation fails. object of the right type for the
	 *         field.
	 */

	public abstract Object parse(Object value);
	/**
	 * @param value
	 *            non-null. value to be parsed and validated into the right type
	 *            after validation
	 * @return null if the validation fails. object of the right type for the
	 *         field.
	 */
	public abstract Object parse(String value);
	/**
	 * @param value
	 *            non-null. String with comma separated values
	 * @return null if the validation fails.
	 */
	public ParsedList parseList(String value) {
		String[] texts = value.split(",");
		Object[] vals = new Object[texts.length];

		// we expect this to be a small array, and hence no optimization
		String textValue = null;
		for (int i = 0; i < texts.length; i++) {
			Object val = this.parse(texts[i]);
			if (val == null) {
				return null;
			}
			vals[i] = val;
			if (textValue == null) {
				textValue = val.toString();
			} else {
				textValue += ',' + val.toString();
			}
		}
		return new ParsedList(textValue, vals);
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * immutable data structure to manage value-lists. Has the text-version as
	 * well as an array of the value of the right type
	 *
	 * @author simplity.org
	 *
	 */
	public class ParsedList {
		/**
		 * text that is a has the comma separated list of values
		 */
		public final String textValue;

		/**
		 * array that has the right-types values
		 */
		public final Object[] valueList;

		/**
		 * immutable, and hence the values for all the members are to be
		 * supplied on instantiation
		 *
		 * @param textValue
		 *            non-null
		 * @param valueList
		 *            non-null
		 */
		public ParsedList(String textValue, Object[] valueList) {
			this.textValue = textValue;
			this.valueList = valueList;
		}
	}
}
