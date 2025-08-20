// SPDX-License-Identifier: MIT
package org.simplity.fm.core.upload;

import java.sql.SQLException;
import java.util.Map;

import org.simplity.fm.core.Message;
import org.simplity.fm.core.data.DbRecord;
import org.simplity.fm.core.db.TransactionHandle;
import org.simplity.fm.core.service.ServiceContext;

/**
 *
 * @author simplity.org
 *
 */
class FormLoader {
	private final DbRecord record;
	/**
	 * if non-null, generated key is copied to the value with this name
	 */
	private final String generatedKeyOutputName;

	/**
	 * array of value providers corresponding to the fields in this form
	 */
	private final InputValueProvider[] valueProviders;

	private final int keyIdx;

	/**
	 *
	 * @param form                   to be used for inserting row
	 * @param generatedKeyOutputName if the table has generated key, and the
	 *                               generated key is to be used by another form,
	 *                               then this is the name of the field with which
	 *                               this generated key is put back into the values
	 *                               map
	 * @param valueProviders         must have exactly the right number and in the
	 *                               same order for the form fields
	 */
	FormLoader(final DbRecord record, final String generatedKeyOutputName, final InputValueProvider[] valueProviders) {
		this.record = record;
		this.generatedKeyOutputName = generatedKeyOutputName;
		this.valueProviders = valueProviders;
		if (this.generatedKeyOutputName == null) {
			this.keyIdx = -1;
		} else {
			this.keyIdx = this.record.fetchGeneratedKeyIndex();
		}
	}

	/**
	 * validate data
	 *
	 * @param values
	 * @param ctx    that must have user and tenantKey if the insert operation
	 *               require these. errors, if any are added to this.
	 * @return true of all ok. false otherwise, in which case ctx will have the
	 *         errors
	 */
	boolean validate(final Map<String, String> values, final ServiceContext ctx) {
		return this.parseInput(values, ctx);

	}

	private boolean parseInput(final Map<String, String> values, final ServiceContext ctx) {
		int idx = -1;
		for (final InputValueProvider vp : this.valueProviders) {
			idx++;
			if (vp != null) {
				this.record.assignValue(idx, vp.getValue(values, ctx));
			}
		}

		// this.record.parseForInsert(data, ctx);
		return true;
	}

	/**
	 *
	 * @param values
	 * @param ctx    that must have user and tenantKey if the insert operation
	 *               require these. errors, if any are added to this.
	 * @return true of all ok. false otherwise, in which case ctx will have the
	 *         errors
	 * @throws SQLException
	 */
	boolean loadData(final Map<String, String> values, final TransactionHandle handle, final ServiceContext ctx)
			throws SQLException {
		if (!this.parseInput(values, ctx)) {
			return false;
		}

		if (!this.record.insert(handle)) {
			ctx.addMessage(Message.newError("Row not inserted, probably because of database constraints"));
			return false;
		}

		if (this.generatedKeyOutputName != null) {
			final Object key = this.record.fetchValue(this.keyIdx);
			if (key != null) {
				values.put(this.generatedKeyOutputName, key.toString());
			}
		}
		return true;
	}

}
