// SPDX-License-Identifier: MIT
package org.simplity.fm.core.data;

/**
 * data structure with field attributes that are to be overridden
 *
 * @author simplity.org
 *
 */
public class FieldOverride {
	/**
	 * name is unique within a record/form
	 */
	public String name;

	/**
	 * data type describes the type of value and restrictions (validations) on
	 * the value
	 */
	public String valueSchema;
	/**
	 * default value is used only if this is optional and the value is missing.
	 * not used if the field is mandatory
	 */
	public String defaultValue;
	/**
	 * refers to the message id/code that is used for i18n of messages
	 */
	public String messageId;
	/**
	 * required/mandatory. If set to true, text value of empty string and 0 for
	 * integral are assumed to be not valid. Relevant only for editable fields.
	 */
	public boolean isRequired;

	/**
	 * list that provides drop-down values for this field
	 */
	public String listName;
}
