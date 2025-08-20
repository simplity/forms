// SPDX-License-Identifier: MIT
package org.simplity.fm.core;

import org.simplity.fm.core.data.Field;
import org.simplity.fm.core.service.OutputData;

/**
 * represents a validation error while accepting data from a client for a field
 *
 * @author simplity.org.
 *
 */
public class Message {
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
	 * @param field
	 * @param tableName
	 * @param idx
	 * @return a validation message when an input value fails validation
	 */
	public static Message newValidationError(final Field field, final String tableName, final int idx) {
		return new Message(MessageType.Error, field.getMessageId(), field.getName(), null, tableName, idx);
	}

	/**
	 * create a validation error message for a field
	 *
	 * @param fieldName
	 * @param messageId
	 * @param params
	 * @return validation error message
	 */
	public static Message newFieldError(final String fieldName, final String messageId, final String... params) {
		return new Message(MessageType.Error, messageId, fieldName, params, null, -1);
	}

	/**
	 * create a validation error message for a field inside an object/table
	 *
	 * @param fieldName  name of the field inside the object
	 * @param objectName attribute/field name of the parent that has this child
	 *                   object as data
	 *
	 * @param messageId
	 * @param rowNumber  1-based row number in which the error is detected
	 * @param params     run-time parameters
	 * @return validation error message
	 */
	public static Message newObjectFieldError(final String fieldName, final String objectName, final String messageId,
			final int rowNumber, final String... params) {
		return new Message(MessageType.Error, messageId, fieldName, params, objectName, rowNumber);
	}

	/**
	 * generic message could be warning/info etc..
	 *
	 * @param messageType
	 * @param messageId
	 * @param params
	 * @return message
	 */
	public static Message newMessage(final MessageType messageType, final String messageId, final String... params) {
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
	 * name of the field that is in error. null if the error is not specific to a
	 * field. Could be a simple field name, or the it could be inside a child
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
	 * run-time parameters to be used to format the text for this message. Please
	 * holders in the message are marked with {}
	 */
	public final String[] params;

	private Message(final MessageType messageType, final String messageId, final String fieldName,
			final String[] params, final String objectName, final int rowNumber) {
		this.messageType = messageType;
		this.messageId = messageId;
		this.fieldName = fieldName;
		this.params = params;
		this.objectName = objectName;
		this.rowNumber = rowNumber;
	}

	@Override
	public String toString() {
		return "type:" + this.messageType + "  id:" + this.messageId + " field:" + this.fieldName;
	}

	/**
	 * @param outData
	 */
	public void toOutputData(final OutputData outData) {
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
			outData.addName("fieldName").addValue(this.fieldName);
		}
		if (this.objectName != null) {
			outData.addName("objectName").addValue(this.objectName);
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
