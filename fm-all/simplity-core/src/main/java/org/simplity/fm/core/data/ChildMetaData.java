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

package org.simplity.fm.core.data;

import java.sql.SQLException;

import org.simplity.fm.core.Message;
import org.simplity.fm.core.db.IReadWriteHandle;
import org.simplity.fm.core.db.IReadonlyHandle;
import org.simplity.fm.core.service.IInputArray;
import org.simplity.fm.core.service.IInputData;
import org.simplity.fm.core.service.IOutputData;
import org.simplity.fm.core.service.IServiceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * represents meta data for a linked form
 *
 * @author simplity.org
 *
 */
public class ChildMetaData {
	private static final Logger logger = LoggerFactory
			.getLogger(ChildMetaData.class);
	/**
	 * non-null unique across all fields of the form
	 */
	private final String childName;

	/**
	 * name of the child form being linked
	 */
	private final String childFormName;

	/**
	 * if this is tabular, min rows expected from client
	 */
	private final int minRows;
	/**
	 * if this is tabular, max rows expected from client.
	 */
	private final int maxRows;
	/**
	 * field names from the parent form that are used for linking
	 */
	private final String[] parentLinkNames;
	/**
	 * field names from the child form that form the parent-key for the child
	 * form
	 */
	private final String[] childLinkNames;
	/**
	 * in case min/max rows violated, what is the error message to be used to
	 * report this problem
	 */
	@SuppressWarnings("unused")
	private final String errorMessageId;

	/**
	 * is the link meant for an array of data or 1-to-1?
	 */
	private final boolean isTable;
	/*
	 * fields that are final but created at init()
	 */

	/**
	 * true only if meta data exists, and the two records are db records
	 */
	private boolean isDbLink;
	/**
	 * in case the records are to be linked, then we need the where clause where
	 * the column names come from the linked record, while the values for them
	 * come from the parent record e.g. childCol1=? and childCll2=?
	 */
	private String linkWhereClause;

	/**
	 * has the details to set params values for a prepared statement from a
	 * parent data row
	 */
	private FieldMetaData[] linkWhereParams;
	/**
	 * in case the linked record is to be used for deleting children
	 */
	private String deleteSql;

	/**
	 * how do we link the parent and the child/linked record?
	 */
	private int[] parentIndexes;
	private int[] childIndexes;

	/**
	 * used by generated code, and hence we are ok with large number of
	 * parameters
	 *
	 * @param childName
	 *            this is different from the child form name. childName has to
	 *            be unique across all field names used by the parent. It is the
	 *            name used in this context of the parent-child relationship
	 * @param childFormName
	 * @param minRows
	 * @param maxRows
	 * @param errorMessageId
	 * @param parentLinkNames
	 * @param childLinkNames
	 * @param isTable
	 */
	public ChildMetaData(final String childName, final String childFormName,
			final int minRows, final int maxRows, final String errorMessageId,
			final String[] parentLinkNames, final String[] childLinkNames,
			final boolean isTable) {
		this.childName = childName;
		this.childFormName = childFormName;
		this.minRows = minRows;
		this.maxRows = maxRows;
		this.parentLinkNames = parentLinkNames;
		this.childLinkNames = childLinkNames;
		this.errorMessageId = errorMessageId;
		this.isTable = isTable;
	}

	boolean isTabular() {
		return this.isTable;
	}

	/**
	 * called by parent form/record if link-fields are specified. Note that the
	 * forms must be based on DbRecord for linking them
	 *
	 * @param parentRec
	 *
	 * @param childREc
	 */
	void init(final Record parentRec, final Record childRec) {
		if (this.parentLinkNames == null || this.childLinkNames == null) {
			logger.info(
					"Linked form has no deign-time link parameters. No auto operations possible..");
			return;
		}
		if (parentRec instanceof DbRecord == false
				|| childRec instanceof DbRecord == false) {
			logger.warn(
					"Linked form defined for non-db record. No auto operations possible..");
			return;
		}

		final DbRecord parentRecord = (DbRecord) parentRec;
		final DbRecord childRecord = (DbRecord) childRec;

		final StringBuilder sbf = new StringBuilder(" WHERE ");
		final int nbr = this.parentLinkNames.length;
		this.parentIndexes = new int[nbr];
		this.childIndexes = new int[nbr];
		this.linkWhereParams = new FieldMetaData[nbr];

		for (int i = 0; i < nbr; i++) {
			final DbField parentField = parentRecord
					.fetchField(this.parentLinkNames[i]);
			/*
			 * child field name is not verified during generation... we may get
			 * run-time exception
			 */
			final DbField childField = childRecord
					.fetchField(this.childLinkNames[i]);
			if (childField == null) {
				throw new RuntimeException("Field " + this.childLinkNames[i]
						+ " is defined as childLinkName, but is not defined as a field in the linked form "
						+ this.childFormName);
			}
			this.parentIndexes[i] = parentField.getIndex();
			this.childIndexes[i] = childField.getIndex();
			if (i != 0) {
				sbf.append(" AND ");
			}
			sbf.append(childField.getColumnName()).append("=?");
			this.linkWhereParams[i] = new FieldMetaData(parentField);
		}

		this.linkWhereClause = sbf.toString();
		this.deleteSql = "delete from " + childRecord.dba.getNameInDb()
				+ this.linkWhereClause;
		this.isDbLink = true;
	}

	private void noDb() {
		logger.error(
				"Link is not designed for db operation on form {}. Database operation not done",
				this.childFormName);
	}
	/**
	 * our current design is to write to the serializer directly
	 *
	 * @param parentRec
	 * @param form
	 * @param outData
	 * @param handle
	 * @return true if read was ok. false in in case of any validation error
	 * @throws SQLException
	 */
	public boolean read(final DbRecord parentRec, final Form<?> form,
			final IOutputData outData, final IReadonlyHandle handle)
			throws SQLException {
		if (!this.isDbLink) {
			this.noDb();
			return false;
		}

		final Object[] values = this.getWhereValues(parentRec);

		final DbRecord thisRecord = (DbRecord) form.record;
		outData.addName(this.childName);
		final Field[] fields = thisRecord.fetchFields();
		if (this.isTable) {
			outData.beginArray();
			for (final Object[] row : thisRecord.filter(this.linkWhereClause,
					values, handle)) {
				outData.beginObject();
				outData.addFields(fields, row);
				form.readChildForms(row, outData, handle);
				outData.endObject();
			}
			outData.endArray();
			return true;
		}

		outData.beginObject();
		if (thisRecord.filterFirst(this.linkWhereClause, values, handle)) {
			outData.addRecord(thisRecord);
		}
		outData.endObject();
		return true;
	}

	private Object[] getWhereValues(final Record parentRec) {
		final int nbr = this.parentIndexes.length;
		final Object[] values = new Object[nbr];
		for (int i = 0; i < nbr; i++) {
			values[i] = parentRec.fetchValue(this.parentIndexes[i]);
		}
		return values;
	}

	private void copyParentKeys(final Record parentRec,
			final Record thisRecord) {
		for (int i = 0; i < this.childIndexes.length; i++) {
			thisRecord.assignValue(this.childIndexes[i],
					parentRec.fetchValue(this.parentIndexes[i]));
		}
	}

	/**
	 * @param parentRec
	 * @param inputObject
	 * @param form
	 * @param handle
	 * @param ctx
	 * @return true if insert operation is successful. false otherwise, in which
	 *         case, the transaction is to be rolled back;
	 * @throws SQLException
	 */
	public boolean save(final DbRecord parentRec, final Form<?> form,
			final IInputData inputObject, final IReadWriteHandle handle,
			final IServiceContext ctx) throws SQLException {
		if (!this.isDbLink) {
			this.noDb();
			return false;
		}

		final DbRecord thisRecord = (DbRecord) form.record;
		if (this.isTable) {
			final IInputArray arr = inputObject.getArray(this.childName);
			if (arr == null) {
				if (this.minRows == 0) {
					logger.info(
							"Input not received, but it is optional. No data saved for linked form.");
					return true;
				}
				ctx.addMessage(Message.newFieldError(this.childName,
						Message.FIELD_REQUIRED, ""));
				return false;
			}

			final int nbr = arr.length();
			if (nbr < this.minRows
					|| (this.maxRows > 0 && nbr > this.maxRows)) {
				ctx.addMessage(Message.newFieldError(this.childName,
						"a min of " + this.minRows + " and a max of "
								+ this.maxRows + " rows expected",
						""));
				return false;
			}

			IInputData[] childRecs = arr.toDataArray();
			for (int idx = 0; idx < childRecs.length; idx++) {
				if (!thisRecord.parse(childRecs[idx], true, ctx,
						this.childFormName, idx)) {
					return false;
				}
				this.copyParentKeys(parentRec, thisRecord);
				thisRecord.saveOrFail(handle);
			}

			return true;
		}

		final IInputData obj = inputObject.getData(this.childName);
		if (obj == null) {
			if (this.minRows > 0) {
				ctx.addMessage(Message.newFieldError(this.childName,
						Message.FIELD_REQUIRED, ""));
				return false;
			}
			logger.info(
					"Input not received, but it is optional. No data saved for linked form.");
			return true;
		}

		if (!thisRecord.parse(obj, true, ctx, this.childName, 0)) {
			logger.error("INput data had errors for linked form {}",
					this.childName);
			return false;
		}

		this.copyParentKeys(parentRec, thisRecord);
		thisRecord.saveOrFail(handle);
		return true;
	}

	/**
	 * @param parentRec
	 * @param form
	 * @param handle
	 * @return true if all OK.
	 * @throws SQLException
	 */
	public boolean delete(final IReadWriteHandle handle,
			final DbRecord parentRec, final Form<?> form) throws SQLException {
		if (!this.isDbLink) {
			this.noDb();
			return false;
		}

		handle.write(this.deleteSql, this.getWhereValues(parentRec));
		/*
		 * 0 delete also is okay
		 */
		return true;
	}
}
