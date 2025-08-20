// SPDX-License-Identifier: MIT
package org.simplity.fm.core.valueschema;

import java.time.Instant;

/**
 * 
 * time stamp is very unlikely to be parsed from a client as input. Validating a
 * time-stamp is probably not required. We just ensure that it is in the right
 * format
 * 
 * @author simplity.org
 *
 */
public class TimestampSchema extends ValueSchema {

	/**
	 * @param name
	 * @param errorId
	 * 
	 */
	public TimestampSchema(String name, String errorId) {
		this.valueType = ValueType.Timestamp;
	}

	@Override
	public Instant parse(Object object) {
		if(object instanceof Instant) {
			return (Instant)object;
		}
		if(object instanceof String) {
			return Instant.parse((String)object);
		}
		return null;
	}

	@Override
	public Instant parse(String value) {
		try {
			return Instant.parse(value);
		}catch(Exception e) {
		return null;
		}
	}
}
