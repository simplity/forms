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
public class TimestampType extends DataType {

	/**
	 * @param name
	 * @param errorId
	 * 
	 */
	public TimestampType(String name, String errorId) {
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