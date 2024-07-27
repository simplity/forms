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

import org.simplity.fm.core.ApplicationError;
import org.simplity.fm.core.app.AppManager;
import org.simplity.fm.core.db.IReadWriteHandle;
import org.simplity.fm.core.db.IReadonlyHandle;
import org.simplity.fm.core.service.IInputData;
import org.simplity.fm.core.service.IOutputData;
import org.simplity.fm.core.service.IServiceContext;

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
	public void read(final DbRecord parentRec, final IOutputData outData,
			final IReadonlyHandle handle) throws SQLException {
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
			final IInputData inputObject, final IReadWriteHandle handle,
			final IServiceContext ctx) throws SQLException {
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
			final IInputData inputObject, final IReadWriteHandle handle,
			final IServiceContext ctx) throws SQLException {
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
			final IReadWriteHandle handle, final IServiceContext ctx)
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
	public void override(final Record parent, final IServiceContext ctx) {
		final String formName = this.form.getName();
		this.form = (Form<T>) AppManager.getApp().getCompProvider()
				.getForm(formName, ctx);
	}
}
