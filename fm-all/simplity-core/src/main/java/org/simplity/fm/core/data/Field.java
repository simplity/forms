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

package org.simplity.fm.core.data;

import org.simplity.fm.core.ApplicationError;
import org.simplity.fm.core.Message;
import org.simplity.fm.core.app.AppManager;
import org.simplity.fm.core.service.IServiceContext;
import org.simplity.fm.core.validn.IValueList;
import org.simplity.fm.core.valueschema.ValueSchema;
import org.simplity.fm.core.valueschema.ValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A field represents an atomic data element in the application. It has the meta
 * data to parse, validate and serialize a value meant for this data element
 *
 * @author simplity.org
 * 
 */
public class Field {

	/**
	 * get the array of names of the fields
	 * 
	 * @param fields
	 * @return names of the fields
	 */
	public static String[] toNames(Field[] fields) {
		String[] names = new String[fields.length];
		for (int i = 0; i < names.length; i++) {
			names[i] = fields[i].name;
		}
		return names;
	}

	protected static final Logger logger = LoggerFactory.getLogger(Field.class);
	/**
	 * name is unique within a record/form
	 */
	private final String name;

	/**
	 * 0-based index of the field in the record.;
	 */
	private final int index;
	/**
	 * value schema describes the restriction on the value (validations)
	 */
	private ValueSchema valueSchema;
	/**
	 * we allow a field to have a list/array of values. If true, then the value is a
	 * string with comma-separated values. Note that this is meant for small list of
	 * numbers/code etc.. Not suitable for array of generic text, as the text itself
	 * may contain comma. This is not a limitation, but a design decision to ensure
	 * that it is used for simple cases only.
	 *
	 */
	private boolean isArray;
	/**
	 * default value is used only if this is optional and the value is missing. not
	 * used if the field is mandatory
	 */
	private Object defaultValue;
	/**
	 * refers to the message id/code that is used for i18n of messages
	 */
	private String messageId;
	/**
	 * required/mandatory. If set to true, text value of empty string and 0 for
	 * integral are assumed to be not valid. Relevant only for editable fields.
	 */
	private boolean isRequired;

	/**
	 * cached value list for validations
	 */
	private IValueList valueList;

	/**
	 * Field with minimum attributes
	 *
	 * @param fieldName
	 * @param index
	 * @param valueSchema
	 * @param isRequired
	 * @param defaultValue optional, can be null
	 */
	public Field(final String fieldName, final int index, final ValueSchema valueSchema, boolean isRequired,
			String defaultValue) {
		this.name = fieldName;
		this.index = index;
		this.isRequired = isRequired;
		this.valueSchema = valueSchema;
		if (defaultValue == null) {
			this.defaultValue = null;
		} else {
			this.defaultValue = valueSchema.parse(defaultValue);
		}
	}

	/**
	 * this is generally invoked by the generated code for a Data Structure
	 *
	 * @param fieldName     unique within its data structure
	 * @param index         0-based index of this field in the parent form
	 * @param valueSchema   pre-defined value schema. used for validating data
	 *                      coming from a client
	 * @param isArray       is this field represent a list of primitive values? If
	 *                      true, it is a string with the underlying
	 * @param defaultValue  value to be used in case the client has not sent a value
	 *                      for this. This is used ONLY if isRequired is false. That
	 *                      is, this is used if the field is optional, and the
	 *                      client skips it. This value is NOT used if isRequired is
	 *                      set to true
	 * @param messageId     can be null in which case the id from dataType is used
	 * @param valueListName if this field has a list of valid values that are
	 *                      typically rendered in a drop-down. If the value list
	 *                      depends on value of another field, then it is part of
	 *                      inter-field validation, and not part of this field.
	 * @param isRequired    is this field mandatory. used for validating data coming
	 *                      from a client
	 */
	public Field(final String fieldName, final int index, final ValueSchema valueSchema, final boolean isArray,
			final String defaultValue, final String messageId, final String valueListName, final boolean isRequired) {
		this.name = fieldName;
		this.index = index;
		this.isRequired = isRequired;
		this.messageId = messageId;
		this.valueSchema = valueSchema;
		this.isArray = isArray;
		if (defaultValue == null) {
			this.defaultValue = null;
		} else {
			this.defaultValue = valueSchema.parse(defaultValue);
		}
		if (valueListName == null) {
			this.valueList = null;
		} else {
			this.valueList = AppManager.getApp().getCompProvider().getValueList(valueListName);
		}
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @return the index
	 */
	public int getIndex() {
		return this.index;
	}

	/**
	 * @return the value-schema associated with this field
	 */
	public ValueSchema getValueSchema() {
		return this.valueSchema;
	}

	/**
	 * @return the defaultValue
	 */
	public Object getDefaultValue() {
		return this.defaultValue;
	}

	/**
	 * @return the messageId
	 */
	public String getMessageId() {
		if (this.messageId == null) {
			return this.valueSchema.getMessageId();
		}
		return this.messageId;
	}

	/**
	 * @return the isRequired
	 */
	public boolean isRequired() {
		return this.isRequired;
	}

	/**
	 *
	 * @return the value type of this field
	 */
	public ValueType getValueType() {
		if (this.isArray) {
			return ValueType.Text;
		}
		return this.valueSchema.getValueType();
	}

	/**
	 *
	 * @param fieldValue string value that is to be parsed. can be null or empty
	 * @param row        into which parsed values is to be set to. MUST be array
	 *                   with the right number of elements
	 * @param ctx        into which any error message is added
	 * @param tableName  if this row is inside a table. used for reporting error
	 * @param rowNbr     used for reporting error is this is part of table
	 * @return true if all ok. false if an error message is added to the context
	 */
	public boolean parseIntoRow(final String fieldValue, final Object[] row, final IServiceContext ctx,
			final String tableName, final int rowNbr) {

		String value = fieldValue == null ? null : fieldValue.trim();
		if (value == null || value.isEmpty()) {
			row[this.index] = null;
			if (!this.isRequired) {
				return true;
			}

			logger.error("Field {} is required but no data is received", this.name);
			ctx.addMessage(Message.newValidationError(this, tableName, rowNbr));
			return false;
		}

		final Object val = this.parse(value, ctx, tableName, rowNbr);
		row[this.index] = val;
		return val != null;
	}

	/**
	 * parse into the desired type, validate and return the value. Meant to be
	 * called after validating null input for mandatory condition
	 *
	 * @param inputValue non-null. input text.
	 * @param ctx        can be null. error added if not null;
	 * @param tableName
	 * @param idx
	 * @return object of the right type. or null if the value is invalid
	 */
	private Object parse(final String inputValue, final IServiceContext ctx, final String tableName, final int idx) {
		final Object obj = this.valueSchema.parse(inputValue);
		if (obj == null) {
			logger.error("{} is not valid for field {} as per value schema {}", inputValue, this.name,
					this.valueSchema.getName());
			ctx.addMessage(Message.newValidationError(this, tableName, idx));
			return null;
		}

		if (this.isArray) {
			final ValueSchema.ParsedList parsedList = (ValueSchema.ParsedList) obj;

			if (this.valueList == null) {
				return parsedList.textValue;
			}

			for (Object val : parsedList.valueList) {
				if (!this.valueList.isValid(val, null, ctx)) {
					return null;
				}
			}
			return parsedList.textValue;
		}

		if (this.valueList == null) {
			return obj;
		}
		/*
		 * numeric 0 is generally considered as "not entered". This is handled by
		 * allowing 0 as part of dataType definition. One issue is when this is a
		 * valueList. Let us handle that specifically
		 */
		if (this.getValueType().equals(ValueType.Integer) && this.isRequired == false && ((Long) obj) == 0) {
			return obj;
		}

		if (this.valueList.isValid(obj, null, ctx)) {
			return obj;
		}

		logger.error("{} is not found in the list of valid values for  for field {}", inputValue, this.name);
		ctx.addMessage(Message.newValidationError(this, tableName, idx));
		return null;
	}

	/**
	 * override attributes of this field
	 *
	 * @param over
	 */
	public void override(final FieldOverride over) {
		this.isRequired = over.isRequired;

		if (over.dataType != null && over.dataType.isEmpty() == false) {
			final ValueSchema dt = AppManager.getApp().getCompProvider().getValueSchema(over.dataType);
			if (dt.getValueType() != this.getValueType()) {
				throw new ApplicationError(
						"Field {} is of value schema {}. It can not be overrideen with value schema '{}' because its value type is different");
			}
			this.valueSchema = AppManager.getApp().getCompProvider().getValueSchema(over.dataType);
		}

		if (over.defaultValue != null && over.defaultValue.isEmpty() == false) {
			this.defaultValue = this.valueSchema.parse(over.defaultValue);
		}

		if (over.messageId != null && over.messageId.isEmpty() == false) {
			this.messageId = over.messageId;
		}

		if (over.listName != null && over.listName.isEmpty() == false) {
			this.valueList = AppManager.getApp().getCompProvider().getValueList(over.listName);
		}
	}
}
