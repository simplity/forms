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

package org.simplity.fm.core.validn;

import java.util.Map;

import org.simplity.fm.core.service.IServiceContext;

/**
 * design-time or run-time list that can be used for validating a field value
 * and supplying possible list of values for that field
 *
 * @author simplity.org
 *
 */
public interface IValueList {
	/**
	 *
	 * @return unique name of this list. This is normally string,but it can be long
	 *         as well.
	 */
	Object getName();

	/**
	 * is this list key-based?
	 *
	 * @return true if the list depends on a key. false if the list is fixed
	 */
	boolean isKeyBased();

	/**
	 * get a list of valid values
	 *
	 * @param keyValue null if this list is not key-based.
	 * @param ctx      non-null for run-time list. can be null for generated lists
	 *                 (simple and keyed)
	 * @return array of [internalValue, displayValue]. internal value could be
	 *         string or number. null if no such list
	 */
	Object[][] getList(Object keyValue, IServiceContext ctx);

	/**
	 * is the field value valid as per this list?
	 *
	 * @param fieldVale non-null value of the right type. Typically either String or
	 *                  Long
	 * @param keyValue  null if this list is not key-based. value of the right type
	 *                  if it is key-based
	 * @param ctx       service context. Could be null in case the validation is
	 *                  required outside of a service context. Implementations must
	 *                  handle the case when this is null
	 * @return true of the field value is valid. false if it is invalid, or these is
	 *         any error in the validation process
	 */
	boolean isValid(Object fieldVale, Object keyValue, IServiceContext ctx);

	/**
	 * Reverse look-up. get internal id for a display name
	 *
	 *
	 * @param ctx null if a static list is to be used.must be non-null for runtime
	 *            lists
	 * @return map with key = keyName|displayText and value = internal value. e.g if
	 *         keyId=91. keyName=India, internalValue=KA displyaText=Karnataka, then
	 *         we will have an entry with key="India|Karnataka" and value="KA".
	 */
	Map<String, String> getAllEntries(IServiceContext ctx);

	/**
	 * relevant for keyed list. Get all the lists for all possible keys
	 *
	 * @param ctx
	 * @return all lists mapped by the key value. Note that the map-key is string
	 *         even if the list-key is numeric. null if this is not a keyed list
	 */
	Map<String, Object[][]> getAllLists(IServiceContext ctx);

	/**
	 * most lists are non-sensitive data, and can be accessed by the public.
	 * However, certain business data may be accessible only to authenticated users
	 *
	 * @return true if the list is accessible only to authenticated users
	 */

	boolean authenticationRequired();
}
