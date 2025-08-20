// SPDX-License-Identifier: MIT
package org.simplity.fm.core.service;

import java.time.Instant;
import java.time.LocalDate;

/**
 * API for a component that serializes arbitrary object structure for
 * transportation across layers as plain text.
 *
 * IMPORTANT: this is designed for "responsible" use. Methods are not tolerant,
 * and throw ApplicationError() in case of any semantic error. This design is to
 * simplify end-use code, as the code can not take any meaningful action if the
 * action is caught
 *
 * @author simplity.org
 *
 */
public interface OutputData {

	/**
	 * start an object
	 *
	 * @return current instance so that methods can be chained
	 */
	OutputData beginObject();

	/**
	 * close an object
	 *
	 * @return current instance so that methods can be chained
	 */
	OutputData endObject();

	/**
	 * start an array
	 *
	 * @return current instance so that methods can be chained
	 */
	OutputData beginArray();

	/**
	 * close an array
	 *
	 * @return current instance so that methods can be chained
	 */
	OutputData endArray();

	/**
	 * start a name of a name-value pair. it must be followed with a value() or
	 * beginArray() or beginObject()
	 *
	 * @param name
	 * @return current instance so that methods can be chained
	 */
	OutputData addName(String name);

	/**
	 * to be used after name()
	 *
	 * @param value
	 * @return current instance so that methods can be chained
	 */
	OutputData addValue(String value);

	/**
	 * to be used after name()
	 *
	 * @param value
	 * @return current instance so that methods can be chained
	 */
	OutputData addValue(long value);

	/**
	 * to be used after name()
	 *
	 * @param value
	 * @return current instance so that methods can be chained
	 */
	OutputData addValue(boolean value);

	/**
	 * to be used after name()
	 *
	 * @param value
	 * @return current instance so that methods can be chained
	 */
	OutputData addValue(double value);

	/**
	 * to be used after name()
	 *
	 * @param value
	 * @return current instance so that methods can be chained
	 */
	OutputData addValue(LocalDate value);

	/**
	 * to be used after name()
	 *
	 * @param value
	 * @return current instance so that methods can be chained
	 */
	OutputData addValue(Instant value);

	/**
	 * @param value can be null. must be one of the standard objects we use as
	 *              primitive. Otherwise a toString() is used
	 * @return current instance so that methods can be chained
	 */
	OutputData addPrimitive(Object value);

	/**
	 * @param name  non-null name of the primitive to be added.
	 * @param value can be null. must be one of the standard objects we use as
	 *              primitive. Otherwise a toString() is used
	 * @return current instance so that methods can be chained
	 */
	OutputData addNameValuePair(String name, Object value);

	/**
	 * to be called inside of an object. Short cut to issue a series of name() and
	 * value() calls
	 *
	 * @param names
	 * @param values
	 * @return current instance so that methods can be chained
	 */
	OutputData addValues(String[] names, Iterable<Object> values);

	/**
	 * to be called inside of an object. Short cut to issue a series of name() and
	 * value() calls
	 *
	 * @param names
	 * @param values
	 * @return current instance so that methods can be chained
	 */
	OutputData addValues(String[] names, Object[] values);

	/**
	 * to be called inside an object. A member is added as an object/sub-form with
	 * name-value pairs as its members
	 *
	 * @param memberName
	 *
	 * @param names      length must match the length of objects in each row
	 * @param values     primitive values for the names
	 * @return current instance so that methods can be chained
	 */
	OutputData addRecord(String memberName, String[] names, Object[] values);

	/**
	 * to be called inside an object. A member is added as an array of rows, each
	 * row being an object
	 *
	 * @param memberName
	 *
	 * @param names      length must match the length of objects in each row
	 * @param rows       rows of data, each row being an array of objects.
	 * @return current instance so that methods can be chained
	 */
	OutputData addArray(String memberName, String[] names, Iterable<Object[]> rows);

	/**
	 * to be called inside an object. A member is added as an array of rows, each
	 * row being an object
	 *
	 * @param memberName
	 *
	 * @param names      length must match the length of objects in each row
	 * @param rows       rows of data, each row being an array of objects.
	 * @return current instance so that methods can be chained
	 */
	OutputData addArray(String memberName, String[] names, Object[][] rows);

	/**
	 * to be called inside an array(). Each row is added as an object-member of the
	 * array. Each object has all the field names as its members.
	 * 
	 * to be used if addArray() is not suitable.
	 *
	 * @param names length of this array must be the same as the number of values in
	 *              each row
	 * @param rows  rows of data, each row being an array of objects for the
	 *              specified fields. could be null or empty
	 * @return current instance so that methods can be chained
	 */
	OutputData addArrayElements(String[] names, Iterable<Object[]> rows);

	/**
	 * to be called inside an array(). Each row is added as an object-member of the
	 * array. Each object has all the field names as its members.
	 * 
	 * to be used if addArray() is not suitable.
	 *
	 * @param names length of this array must be the same as the number of values in
	 *              each row
	 * @param rows  rows of data, each row being an array of objects for the
	 *              specified fields. could be null or empty
	 * @return current instance so that methods can be chained
	 */
	OutputData addArrayElements(String[] names, Object[][] rows);

	/**
	 * to be used in case the caller has a serialized JSON as a string.
	 * 
	 * @param json string to be written as it is to form the body of the json being
	 *             written
	 * @return current instance so that methods can be chained
	 */
	OutputData addStringAsJson(String json);

}
