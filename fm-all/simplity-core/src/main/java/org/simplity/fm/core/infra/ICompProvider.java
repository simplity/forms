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

package org.simplity.fm.core.infra;

import org.simplity.fm.core.Message;
import org.simplity.fm.core.data.Form;
import org.simplity.fm.core.data.Record;
import org.simplity.fm.core.fn.IFunction;
import org.simplity.fm.core.service.IService;
import org.simplity.fm.core.service.IServiceContext;
import org.simplity.fm.core.validn.IValueList;
import org.simplity.fm.core.valueschema.ValueSchema;

/**
 * specification for the class that provides instances of all standard app
 * components
 *
 * @author simplity.org
 *
 */
public interface ICompProvider {
	/**
	 *
	 * @param formId
	 * @return form instance, or null if such a form is not located
	 */
	Form<?> getForm(String formId);

	/**
	 *
	 * @param formId
	 * @param ctx
	 *            service context that may have form-overrides
	 * @return form instance, or null if such a form is not located
	 */
	Form<?> getForm(String formId, IServiceContext ctx);

	/**
	 * get record when the record may be over-ridden for the current tenant id
	 *
	 * @param recordName
	 * @return record instance, or null if such a record is not located
	 */
	Record getRecord(String recordName);

	/**
	 * get record when the record may be over-ridden for the current tenant id
	 *
	 * @param recordName
	 * @param ctx
	 *            service context. null if the record if base record is required
	 *            and no need to check for overrides
	 * @return record instance, or null if such a record is not located
	 */
	Record getRecord(String recordName, IServiceContext ctx);

	/**
	 *
	 * @param dataTypeId
	 * @return a data type instance, or null if it is not located.
	 */
	ValueSchema getValueSchema(String dataTypeId);

	/**
	 *
	 * @param listId
	 * @return an instance for this id, or null if is not located
	 */
	IValueList getValueList(String listId);

	/**
	 *
	 * @param serviceName
	 * @param ctx
	 *            service context where this service is to be executed
	 * @return an instance for this id, or null if it cannot be located
	 */
	IService getService(String serviceName, IServiceContext ctx);

	/**
	 *
	 * @param functionName
	 * @return an instance for this id, or null if is not located
	 */
	IFunction getFunction(String functionName);

	/**
	 *
	 * @param messageId
	 * @return message or null if no such message is located.
	 */
	Message getMessage(String messageId);

}
