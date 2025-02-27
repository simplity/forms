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

import org.simplity.fm.core.Message;
import org.simplity.fm.core.service.IServiceContext;
import org.simplity.fm.core.valueschema.ValueSchema;
import org.simplity.fm.core.valueschema.ValueType;

/**
 * @author simplity.org
 *
 */
public class DbField extends Field {

	/**
	 * name of the db column
	 */
	private final String columnName;

	/**
	 * type of column
	 */
	private final FieldType fieldType;

	/**
	 * this is generally invoked by the generated code for a Data Structure
	 *
	 * @param fieldName     unique within its data structure
	 * @param index         0-based index of this field in the parent form
	 * @param valueType     non-null
	 * @param valueSchema   can be null. pre-defined value schema. optional. used
	 *                      for validating data coming from a client
	 * @param isList        is this a comma-separated list of values?
	 * @param defaultValue  value to be used in case the client has not sent a value
	 *                      for this. This e is used ONLY if isRequired is false.
	 *                      That is, this is used if the field is optional, and the
	 *                      client skips it. This value is NOT used if isRequired is
	 *                      set to true
	 * @param messageId     can be null in which case the id from dataType is used
	 * @param valueListName if this field has a list of valid values that are
	 *                      typically rendered in a drop-down. If the value list
	 *                      depends on value of another field, then it is part of
	 *                      inter-field validation, and not part of this field.
	 * @param columnName    db column name. non-null
	 * @param fieldType     db field type. non-null
	 */
	public DbField(final String fieldName, final int index, final ValueType valueType, final ValueSchema valueSchema,
			final boolean isList, final String defaultValue, final String messageId, final String valueListName,
			final String columnName, final FieldType fieldType) {
		super(fieldName, index, valueType, valueSchema, isList, defaultValue, messageId, valueListName,
				fieldType.isRequired());
		this.columnName = columnName;
		this.fieldType = fieldType;
	}

	/**
	 *
	 * @return db column name with which this field is associated with
	 */
	public String getColumnName() {
		return this.columnName;
	}

	/**
	 * @return the field type
	 */
	public FieldType getFieldType() {
		return this.fieldType;
	}

	/**
	 * @return true if this column is part of the primary key
	 */
	public boolean isPrimaryKey() {
		return this.fieldType == FieldType.PrimaryKey || this.fieldType == FieldType.GeneratedPrimaryKey;
	}

	/**
	 * @return true if this field is the tenant key.
	 */
	public boolean isTenantKey() {
		return this.fieldType == FieldType.TenantKey;
	}

	/**
	 * @return true if this field is user id, like createdBy and modifiedBy.
	 */
	public boolean isUserId() {
		return this.fieldType == FieldType.ModifiedBy || this.fieldType == FieldType.CreatedBy;
	}

	/**
	 * special fields need some additional validation, as well as setting
	 * pre-defined data
	 *
	 * @param data
	 * @param forInsert
	 * @param ctx
	 * @param tableName
	 * @param rowNbr
	 * @return true if all OK. false if an error message is addedd to the context
	 */
	public boolean validate(final Object[] data, final boolean forInsert, final IServiceContext ctx,
			final String tableName, final int rowNbr) {
		final int idx = this.getIndex();
		final Object val = data[idx];

		switch (this.fieldType) {
		/*
		 * tenant key is ignored from the client, and populated from the context
		 */
		case TenantKey:
			data[idx] = ctx.getTenantId();
			return true;

		/*
		 * user id is also populated from the context
		 */
		case CreatedBy:
		case ModifiedBy:
			data[idx] = ctx.getUserId();
			return true;

		/*
		 * generated primary key is set as "optional". However this is required if the
		 * input is for an update operation
		 */
		case GeneratedPrimaryKey:
			if (forInsert == false && val == null) {
				ctx.addMessage(Message.newValidationError(this, tableName, rowNbr));
				return false;
			}
			return true;

		default:
			return true;

		}
	}
}
