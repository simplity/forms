/*
 * Copyright (c) 2020 simplity.org
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

package org.simplity.fm.core.service;

import java.util.Map;
import java.util.Set;

/**
 * Input Data for a service. This interface allows us to wrap dynamic data
 * structures like JSON or XML etc.. Services are not hard-wired to any specific
 * serialization/de-serialization techniques.
 *
 */
public interface IInputData {

	/**
	 *
	 * @param name
	 * @return null if no member with that name, or if the member is not an
	 *         <code>IInputData</code>
	 */
	IInputData getData(String name);

	/**
	 *
	 * @param name
	 * @return null if no such member, or the value is not an array.
	 */
	IInputArray getArray(String name);

	/**
	 * value is zero if the member is missing or is not a number. getText() may
	 * be used if there is a need to differentiate zero from missing member
	 *
	 * @param name
	 * @return 0 if member is non-text, non-numeric. text is parsed into
	 *         integral value
	 */
	long getLong(String name);

	/**
	 *
	 * @param name
	 * @return null if member is missing, or is not a primitive. it is null if
	 *         the member is IInputObject or IInputArray.
	 */
	String getString(String name);

	/**
	 *
	 * @param name
	 * @return true if the member is boolean and is true. Also true if it is
	 *         text 'true', or '1' or integer 1; false otherwise
	 */
	boolean getBoolean(String name);

	/**
	 *
	 * @param name
	 * @return if member is text, it is parsed into decimal. 0 if it is non-text
	 *         and non-number
	 */
	double getDecimal(String name);

	/**
	 *
	 * @return true if the object has no members
	 */
	boolean isEmpty();

	/**
	 * inspect what type of data a member has
	 *
	 * @param memberName
	 * @return non-null
	 */
	InputDataType getDataType(String memberName);

	/**
	 * allows exploring unknown data. Obviously, bit expensive with construction
	 * of Map etc..
	 *
	 * @return member names
	 */
	Map<String, InputDataType> getMemberTypes();

	/**
	 * allows exploring unknown data. Obviously, bit expensive with construction
	 * of Map etc..
	 *
	 * @return member names
	 */
	Set<String> getMemberNames();

	/**
	 * to be used carefully. This alters the inputData.
	 *
	 * @param memberName
	 *            name of the member
	 * @param value
	 *            string value of the member
	 */

	void addValue(String memberName, String value);

	/**
	 *
	 * @return number of members
	 */
	int size();
}
