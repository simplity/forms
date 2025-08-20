// SPDX-License-Identifier: MIT
package org.simplity.fm.core.valueschema;

/**
 * validation parameters for a an integral value
 * 
 * @author simplity.org
 *
 */
public class BooleanSchema extends ValueSchema {

	/**
	 * @param name 
	 * @param messageId
	 */
	public BooleanSchema(String name, String messageId) {
		this.valueType = ValueType.Boolean;
		this.name = name;
		this.messageId = messageId;
	}

	@Override
	public Boolean parse(String value) {
		return (Boolean)ValueType.Boolean.parse(value);
	}
	
	@Override
	public Boolean parse(Object value) {
		if(value instanceof Boolean) {
			return (Boolean)value;
		}
		return (Boolean)ValueType.Boolean.parse(value.toString());
	}
}
