// SPDX-License-Identifier: MIT
package org.simplity.fm.core.valueschema;

/**
 * @author simplity.org
 *
 */
public class InvalidValueException extends Exception {
	private static final long serialVersionUID = 1L;
	private String messageId;
	private String fieldName;
	private String[] params;

	/**
	 * a field has failed validations
	 * 
	 * @param fieldName
	 * @param msgId
	 * @param params
	 */
	public InvalidValueException(String msgId, String fieldName, String... params) {
		this.messageId = msgId;
		this.fieldName = fieldName;
		this.params = params;
	}

	@Override
	public String getMessage() {
		return "validation for faield " + this.fieldName + " failed with messageId=" + this.messageId
				+ " and additional params=" + this.params;
	}

	/**
	 * @return the messageId
	 */
	public String getMessageId() {
		return this.messageId;
	}

	/**
	 * @return the fieldName
	 */
	public String getFieldName() {
		return this.fieldName;
	}

	/**
	 * @return run-time parameters. null if no params
	 */
	public String[] getParams() {
		return this.params;
	}
}
