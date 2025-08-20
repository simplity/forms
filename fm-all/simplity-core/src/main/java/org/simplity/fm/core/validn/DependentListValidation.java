// SPDX-License-Identifier: MIT
package org.simplity.fm.core.validn;

import org.simplity.fm.core.Message;
import org.simplity.fm.core.app.AppManager;
import org.simplity.fm.core.data.Record;
import org.simplity.fm.core.service.ServiceContext;

/**
 * when the valid values for a field depends on the vale of its key-field. like
 * list of valid districts depends on stateCode
 *
 * @author simplity.org
 */
public class DependentListValidation implements FormDataValidation {
	private final String listName;
	private final int fieldIndex;
	private final int parentFieldIndex;
	private final String fieldName;
	private final String messaageId;

	/**
	 * create the list with valid keys and values
	 *
	 * @param fieldIndex
	 * @param parentFieldIndex
	 * @param listName
	 * @param fieldName
	 * @param messageId
	 *
	 */
	public DependentListValidation(final int fieldIndex,
			final int parentFieldIndex, final String listName,
			final String fieldName, final String messageId) {
		this.fieldIndex = fieldIndex;
		this.parentFieldIndex = parentFieldIndex;
		this.listName = listName;
		this.fieldName = fieldName;
		this.messaageId = messageId;
	}

	@Override
	public boolean isValid(final Record record, final ServiceContext ctx) {
		final Object fieldValue = record.fetchValue(this.fieldIndex);
		if (fieldValue == null) {
			return true;
		}
		final Object keyValue = record.fetchValue(this.parentFieldIndex);
		if (keyValue == null) {
			return true;
		}

		final ValueList vl = AppManager.getApp().getCompProvider()
				.getValueList(this.listName);
		if (vl == null) {
			return true;
		}
		if (vl.isValid(fieldValue, keyValue, ctx)) {
			return true;
		}
		ctx.addMessage(Message.newFieldError(this.fieldName, this.messaageId));
		return false;
	}

	@Override
	public String getFieldName() {
		return this.fieldName;
	}
}