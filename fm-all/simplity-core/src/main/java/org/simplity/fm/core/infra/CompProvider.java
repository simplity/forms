// SPDX-License-Identifier: MIT
package org.simplity.fm.core.infra;

import org.simplity.fm.core.Message;
import org.simplity.fm.core.data.Form;
import org.simplity.fm.core.data.Record;
import org.simplity.fm.core.fn.FunctionDefinition;
import org.simplity.fm.core.service.Service;
import org.simplity.fm.core.service.ServiceContext;
import org.simplity.fm.core.validn.ValueList;
import org.simplity.fm.core.valueschema.ValueSchema;

/**
 * specification for the class that provides instances of all standard app
 * components
 *
 * @author simplity.org
 *
 */
public interface CompProvider {
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
	Form<?> getForm(String formId, ServiceContext ctx);

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
	Record getRecord(String recordName, ServiceContext ctx);

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
	ValueList getValueList(String listId);

	/**
	 *
	 * @param serviceName
	 * @param ctx
	 *            service context where this service is to be executed
	 * @return an instance for this id, or null if it cannot be located
	 */
	Service getService(String serviceName, ServiceContext ctx);

	/**
	 *
	 * @param functionName
	 * @return an instance for this id, or null if is not located
	 */
	FunctionDefinition getFunction(String functionName);

	/**
	 *
	 * @param messageId
	 * @return message or null if no such message is located.
	 */
	Message getMessage(String messageId);

}
