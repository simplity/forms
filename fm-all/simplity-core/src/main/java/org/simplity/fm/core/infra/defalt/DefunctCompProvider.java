// SPDX-License-Identifier: MIT
package org.simplity.fm.core.infra.defalt;

import org.simplity.fm.core.ApplicationError;
import org.simplity.fm.core.Message;
import org.simplity.fm.core.data.Form;
import org.simplity.fm.core.data.Record;
import org.simplity.fm.core.fn.FunctionDefinition;
import org.simplity.fm.core.infra.CompProvider;
import org.simplity.fm.core.service.Service;
import org.simplity.fm.core.service.ServiceContext;
import org.simplity.fm.core.validn.ValueList;
import org.simplity.fm.core.valueschema.ValueSchema;

/**
 * 
 * @author simplity.org
 *
 */
public class DefunctCompProvider implements CompProvider {
	private static void error() {
		throw new ApplicationError("The app is not configured to provide components.");
	}

	@Override
	public Form<?> getForm(final String formId) {
		error();
		return null;
	}

	@Override
	public ValueSchema getValueSchema(final String schemaId) {
		error();
		return null;
	}

	@Override
	public ValueList getValueList(final String listId) {
		error();
		return null;
	}

	@Override
	public Service getService(final String serviceName, final ServiceContext ctx) {
		error();
		return null;
	}

	@Override
	public FunctionDefinition getFunction(final String functionName) {
		error();
		return null;
	}

	@Override
	public Message getMessage(final String messageId) {
		error();
		return null;
	}

	@Override
	public Record getRecord(final String recordName, final ServiceContext ctx) {
		error();
		return null;
	}

	@Override
	public Record getRecord(final String recordName) {
		error();
		return null;
	}

	@Override
	public Form<?> getForm(final String formId, final ServiceContext ctx) {
		error();
		return null;
	}
}
