// SPDX-License-Identifier: MIT
package org.simplity.fm.core.data;

import java.sql.SQLException;

import org.simplity.fm.core.ApplicationError;
import org.simplity.fm.core.app.AppManager;
import org.simplity.fm.core.db.ReadWriteHandle;
import org.simplity.fm.core.db.ReadonlyHandle;
import org.simplity.fm.core.service.InputData;
import org.simplity.fm.core.service.OutputData;
import org.simplity.fm.core.service.ServiceContext;

/**
 * represents a child form for a parent form
 *
 * @author simplity.org
 * @param <T>
 *            Record of the child form
 *
 */
public class ChildForm<T extends Record> {
	/**
	 * how this form is linked to its parent
	 */
	private final ChildMetaData childMeta;

	private Form<T> form;

	/**
	 *
	 * @param childMeta
	 * @param form
	 */
	public ChildForm(final ChildMetaData childMeta, final Form<T> form) {
		this.childMeta = childMeta;
		this.form = form;
	}

	/**
	 * read rows for this child form based on the parent record
	 *
	 * @param parentRec
	 *            parent record
	 * @param outData
	 *            to which data is to be serialized to
	 * @param handle
	 * @throws SQLException
	 */
	public void read(final DbRecord parentRec, final OutputData outData,
			final ReadonlyHandle handle) throws SQLException {
		this.childMeta.read(parentRec, this.form, outData, handle);
	}

	/**
	 * @param parentRec
	 * @param inputObject
	 * @param handle
	 * @param ctx
	 * @return true if all OK. false in case any error is added to the ctx
	 * @throws SQLException
	 */
	public boolean insert(final DbRecord parentRec,
			final InputData inputObject, final ReadWriteHandle handle,
			final ServiceContext ctx) throws SQLException {
		this.checkUpdatability();
		return this.childMeta.save(parentRec, this.form, inputObject, handle,
				ctx);
	}

	/**
	 * @param parentRec
	 * @param inputObject
	 * @param handle
	 * @param ctx
	 * @return true if all OK. false in case any error is added to the ctx
	 * @throws SQLException
	 */
	public boolean update(final DbRecord parentRec,
			final InputData inputObject, final ReadWriteHandle handle,
			final ServiceContext ctx) throws SQLException {
		this.checkUpdatability();
		return this.childMeta.save(parentRec, this.form, inputObject, handle,
				ctx);
	}

	/**
	 * @param parentRec
	 * @param handle
	 * @param ctx
	 * @return true if all OK. false in case any error is added to the ctx
	 * @throws SQLException
	 */
	public boolean delete(final DbRecord parentRec,
			final ReadWriteHandle handle, final ServiceContext ctx)
			throws SQLException {
		this.checkUpdatability();
		return this.childMeta.delete(handle, parentRec, this.form);
	}

	private void checkUpdatability() {
		if (this.form.hasChildren()) {
			throw new ApplicationError(
					"Auto delete operation is not allowed on a form with child forms that in turn have child forms.");
		}
	}

	/**
	 * must be called by parent form before it is used
	 *
	 * @param parentRecord
	 */
	public void init(final Record parentRecord) {
		this.childMeta.init(parentRecord, this.form.getRecord());

	}

	/**
	 * @param parent
	 * @param ctx
	 */
	@SuppressWarnings("unchecked")
	public void override(final Record parent, final ServiceContext ctx) {
		final String formName = this.form.getName();
		this.form = (Form<T>) AppManager.getApp().getCompProvider()
				.getForm(formName, ctx);
	}
}
