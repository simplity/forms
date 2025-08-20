// SPDX-License-Identifier: MIT
package org.simplity.fm.core.validn;

import org.simplity.fm.core.data.Record;
import org.simplity.fm.core.service.ServiceContext;

/**
 * represents a validation at the form level, including inter-field
 * validations.This should not be used for field level validations. (Field level
 * validations are handled at <code>DataElement</code> level
 *
 * @author simplity.org
 *
 */
public interface FormDataValidation {
	/**
	 * execute this validation for a data row
	 *
	 * @param dataRow
	 * @param ctx
	 * @return true if all OK. false if an error message is added to the list
	 */
	boolean isValid(Record dataRow, ServiceContext ctx);

	/**
	 *
	 * @return primary/any field that is associated with this validation
	 */
	String getFieldName();
}
