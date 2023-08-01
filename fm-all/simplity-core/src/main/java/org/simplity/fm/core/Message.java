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
package org.simplity.fm.core;

import org.simplity.fm.core.data.Field;
import org.simplity.fm.core.service.IOutputData;
import org.simplity.fm.core.valueschema.InvalidValueException;

/**
 * represents a validation error while accepting data from a client for a field
 *
 * @author simplity.org.
 *
 */
public class Message {
	/**
	 * message to be used if the user is not authorized for this specific form
	 * instance
	 */
	public static final String MSG_NOT_AUTHORIZED = "notAuthorized";
	/**
	 * time stamp value is invalid
	 */
	public static final String INVALID_TIMESTAMP = "invalidTimestamp";
	/**
	 * time stamp value is mismatch
	 */
	public static final String CONCURRENT_UPDATE = "concurrentUpdate";
	/**
	 * error to be used in case of any internal error
	 */
	public static final String MSG_INTERNAL_ERROR = "internalError";

	/**
	 * error to be used in case of any internal error
	 */
	public static final String MSG_INVALID_DATA = "invalidData";
	/**
	 * error to be used to indicate that value is required for an input field
	 */
	public static final String FIELD_REQUIRED = "valueRequired";

	/**
	 * create an error message for a message id
	 *
	 * @param messageId
	 * @return an error message for this message id
	 */
	public static Message newError(final String messageId) {
		return new Message(MessageType.Error, messageId, null, null, null, -1);
	}

	/**
	 * @param e
	 * @return a validation message based on the exception
	 */
	public static Message newValidationError(final InvalidValueException e) {
		return new Message(MessageType.Error, e.getMessageId(),
				e.getFieldName(), e.getParams(), null, -1);
	}

	/**
	 * @param field
	 * @param tableName
	 * @param idx
	 * @return a validation message when an input value fails validation
	 */
	public static Message newValidationError(final Field field,
			final String tableName, final int idx) {
		return new Message(MessageType.Error, field.getMessageId(),
				field.getName(), null, tableName, idx);
	}

	/**
	 * create a validation error message for a field
	 *
	 * @param fieldName
	 * @param messageId
	 * @param params
	 * @return validation error message
	 */
	public static Message newFieldError(final String fieldName,
			final String messageId, final String... params) {
		return new Message(MessageType.Error, messageId, fieldName, params,
				null, -1);
	}

	/**
	 * create a validation error message for a field inside an object/table
	 *
	 * @param fieldName
	 *            name of the field inside the object
	 * @param objectName
	 *            attribute/field name of the parent that has this child object
	 *            as data
	 *
	 * @param messageId
	 * @param rowNumber
	 *            1-based row number in which the error is detected
	 * @param params
	 *            run-time parameters
	 * @return validation error message
	 */
	public static Message newObjectFieldError(final String fieldName,
			final String objectName, final String messageId,
			final int rowNumber, final String... params) {
		return new Message(MessageType.Error, messageId, fieldName, params,
				objectName, rowNumber);
	}

	/**
	 * generic message could be warning/info etc..
	 *
	 * @param messageType
	 * @param messageId
	 * @param params
	 * @return message
	 */
	public static Message newMessage(final MessageType messageType,
			final String messageId, final String... params) {
		return new Message(messageType, messageId, null, params, null, -1);
	}

	/**
	 * message type/severity.
	 */
	public MessageType messageType;
	/**
	 * error message id for this error. non-null;
	 */
	public final String messageId;
	/**
	 * name of the field that is in error. null if the error is not specific to
	 * a field. Could be a simple field name, or the it could be inside a child
	 * table/object
	 */
	public final String fieldName;

	/**
	 * If the field is inside a child table/object, this is the name of that
	 * table/object
	 */
	public final String objectName;

	/**
	 * 0-based row number in case this is a tabular data
	 */
	public final int rowNumber;

	/**
	 * run-time parameters to be used to format the text for this message.
	 * Please holders in the message are marked with {}
	 */
	public final String[] params;

	private Message(final MessageType messageType, final String messageId,
			final String fieldName, final String[] params,
			final String objectName, final int rowNumber) {
		this.messageType = messageType;
		this.messageId = messageId;
		this.fieldName = fieldName;
		this.params = params;
		this.objectName = objectName;
		this.rowNumber = rowNumber;
	}

	@Override
	public String toString() {
		return "type:" + this.messageType + "  id:" + this.messageId + " field:"
				+ this.fieldName;
	}

	/**
	 * @param outData
	 */
	public void toOutputData(final IOutputData outData) {
		outData.beginObject();
		outData.addName("type");

		if (this.messageType == null) {
			outData.addValue("error");
		} else {
			outData.addValue(this.messageType.toString().toLowerCase());
		}
		outData.addName("id").addValue(this.messageId);
		outData.addName("text").addValue(this.messageId);
		if (this.fieldName != null) {
			outData.addName("fieldName").addValue(this.messageId);
		}
		if (this.fieldName != null) {
			outData.addName("objectName").addValue(this.messageId);
		}

		if (this.params != null && this.params.length > 0) {
			outData.addName("params").beginArray();

			for (String p : this.params) {
				outData.addValue(p);
			}
			outData.endArray();
		}

		if (this.rowNumber != -1) {
			outData.addName("idx").addValue(this.rowNumber);
		}

		outData.endObject();
	}
}
