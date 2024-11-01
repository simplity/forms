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
public interface IOutputData {

	/**
	 * start an object
	 *
	 * @return current instance so that methods can be chained
	 */
	IOutputData beginObject();

	/**
	 * close an object
	 *
	 * @return current instance so that methods can be chained
	 */
	IOutputData endObject();

	/**
	 * start an array
	 *
	 * @return current instance so that methods can be chained
	 */
	IOutputData beginArray();

	/**
	 * close an array
	 *
	 * @return current instance so that methods can be chained
	 */
	IOutputData endArray();

	/**
	 * start a name of a name-value pair. it must be followed with a value() or
	 * beginArray() or beginObject()
	 *
	 * @param name
	 * @return current instance so that methods can be chained
	 */
	IOutputData addName(String name);

	/**
	 * to be used after name()
	 *
	 * @param value
	 * @return current instance so that methods can be chained
	 */
	IOutputData addValue(String value);

	/**
	 * to be used after name()
	 *
	 * @param value
	 * @return current instance so that methods can be chained
	 */
	IOutputData addValue(long value);

	/**
	 * to be used after name()
	 *
	 * @param value
	 * @return current instance so that methods can be chained
	 */
	IOutputData addValue(boolean value);

	/**
	 * to be used after name()
	 *
	 * @param value
	 * @return current instance so that methods can be chained
	 */
	IOutputData addValue(double value);

	/**
	 * to be used after name()
	 *
	 * @param value
	 * @return current instance so that methods can be chained
	 */
	IOutputData addValue(LocalDate value);

	/**
	 * to be used after name()
	 *
	 * @param value
	 * @return current instance so that methods can be chained
	 */
	IOutputData addValue(Instant value);

	/**
	 * @param value can be null. must be one of the standard objects we use as
	 *              primitive. Otherwise a toString() is used
	 * @return current instance so that methods can be chained
	 */
	IOutputData addPrimitive(Object value);

	/**
	 * @param name  non-null name of the primitive to be added.
	 * @param value can be null. must be one of the standard objects we use as
	 *              primitive. Otherwise a toString() is used
	 * @return current instance so that methods can be chained
	 */
	IOutputData addNameValuePair(String name, Object value);

	/**
	 * to be called inside of an object. Short cut to issue a series of name() and
	 * value() calls
	 *
	 * @param names
	 * @param values
	 * @return current instance so that methods can be chained
	 */
	IOutputData addValues(String[] names, Iterable<Object> values);

	/**
	 * to be called inside of an object. Short cut to issue a series of name() and
	 * value() calls
	 *
	 * @param names
	 * @param values
	 * @return current instance so that methods can be chained
	 */
	IOutputData addValues(String[] names, Object[] values);

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
	IOutputData addArray(String memberName, String[] names, Iterable<Object[]> rows);

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
	IOutputData addArray(String memberName, String[] names, Object[][] rows);

	/**
	 * to be called inside an array(). Each row is added as an object-member of the
	 * array. Each object has all the field names as its members.
	 * 
	 * to be used if addArray() is not suitable.
	 *
	 * @param names  length of this array must be the same as the number of values
	 *               in each row
	 * @param values rows of data, each row being an array of objects for the
	 *               specified fields. could be null or empty
	 * @return current instance so that methods can be chained
	 */
	IOutputData addArrayElements(String[] names, Iterable<Object[]> rows);

	/**
	 * to be called inside an array(). Each row is added as an object-member of the
	 * array. Each object has all the field names as its members.
	 * 
	 * to be used if addArray() is not suitable.
	 *
	 * @param names  length of this array must be the same as the number of values
	 *               in each row
	 * @param values rows of data, each row being an array of objects for the
	 *               specified fields. could be null or empty
	 * @return current instance so that methods can be chained
	 */
	IOutputData addArrayElements(String[] names, Object[][] rows);
}
