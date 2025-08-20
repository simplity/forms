// SPDX-License-Identifier: MIT
package org.simplity.fm.core.service;

import java.util.Set;

/**
 * Input Data for a service. This interface allows us to wrap dynamic data
 * structures like JSON or XML etc.. Services are not hard-wired to any specific
 * serialization/de-serialization techniques.
 *
 */
public interface InputData {

	/**
	 * To be used to explore data with unknown/flexible schema. Also useful in case
	 * nulls are allowed by design. Simplity recommends using additional data
	 * elements rather than resorting to nullable fields.
	 *
	 * @param name member name
	 *
	 * @return non-null value
	 */
	NullableValue getValue(String name);

	/**
	 *
	 * @param name
	 * @return null if no member with that name, or if the member is not an
	 *         <code>IInputData</code>
	 */
	InputData getData(String name);

	/**
	 *
	 * @param name
	 * @return null if no such member, or the value is not an array.
	 */
	InputArray getArray(String name);

	/**
	 * value is zero if the member is missing or is not a number. getText() may be
	 * used if there is a need to differentiate zero from missing member
	 *
	 * @param name
	 * @return 0 if member is non-text, non-numeric. text is parsed into integral
	 *         value
	 */
	long getInteger(String name);

	/**
	 *
	 * @param name
	 * @return string value of this primitive property. null if member is missing,
	 *         or is not a primitive. it is null if the member is IInputObject or
	 *         IInputArray.
	 */
	String getString(String name);

	/**
	 *
	 * @param name
	 * @return true if the member is boolean and is true. Also true if it is text
	 *         'true', or '1' or integer 1; false otherwise
	 */
	boolean getBoolean(String name);

	/**
	 *
	 * @param name
	 * @return if member is text, it is parsed into decimal. 0 if it is non-text and
	 *         non-number
	 */
	double getDecimal(String name);

	/**
	 *
	 * @return true if the object has no members
	 */
	boolean isEmpty();

	/**
	 * allows exploring unknown data. Obviously, bit expensive with construction of
	 * Map etc..
	 *
	 * @return member names
	 */
	Set<String> getMemberNames();

	/**
	 * to be used carefully. This alters the inputData.
	 *
	 * @param memberName name of the member
	 * @param value      string value of the member
	 */

	void addValue(String memberName, String value);

	/**
	 *
	 * @return number of members
	 */
	int size();
}
