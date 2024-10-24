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
import java.util.List;

import org.simplity.fm.core.ApplicationError;
import org.simplity.fm.core.Conventions;
import org.simplity.fm.core.Message;
import org.simplity.fm.core.app.AppManager;
import org.simplity.fm.core.db.IReadonlyHandle;
import org.simplity.fm.core.service.AbstractService;
import org.simplity.fm.core.service.IInputData;
import org.simplity.fm.core.service.IOutputData;
import org.simplity.fm.core.service.IService;
import org.simplity.fm.core.service.IServiceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Form is a client-side component. If a form is based on a record for its data,
 * then this class is generated to deliver services for that client-side
 * component.
 *
 * @author simplity.org
 * @param <T> primary record that describes the data behind this form
 *
 */
public abstract class Form<T extends Record> {
	protected static final Logger logger = LoggerFactory.getLogger(Form.class);
	/*
	 * name of this form. unique within an app
	 */
	private final String name;

	/*
	 * record that this form is based on
	 */
	protected T record;

	/**
	 * is this form open to guests
	 */
	protected boolean serveGuests;

	/*
	 * what operations are allowed on this form
	 */
	private final boolean[] operations;

	/*
	 * child forms
	 */
	protected final ChildForm<?>[] childForms;
	private final boolean isDb;

	protected Form(final String name, final T record, final boolean[] operations, final ChildForm<?>[] childForms) {
		this.name = name;
		this.record = record;
		this.operations = operations;
		this.isDb = record instanceof DbRecord;
		this.childForms = childForms;
		if (childForms != null && childForms.length > 0) {
			for (final ChildForm<?> lf : childForms) {
				lf.init(record);
			}
		}
	}

	/**
	 * @return true if this form is based on a db record. false otherwise
	 */
	public boolean isDb() {
		return this.isDb;
	}

	/**
	 *
	 * @return true if this form has child forms. false otherwise.
	 */
	public boolean hasChildren() {
		return this.childForms != null;
	}

	/**
	 * read rows for the child-forms
	 *
	 * @param rawData for the record for this form
	 * @param outData to which the read rows are to be serialized into
	 * @param handle
	 * @throws SQLException
	 */
	public void readChildForms(final Object[] rawData, final IOutputData outData, final IReadonlyHandle handle)
			throws SQLException {
		if (this.childForms != null) {
			for (final ChildForm<?> child : Form.this.childForms) {
				child.read((DbRecord) this.record, outData, handle);
			}
		}
	}

	/**
	 * load keys from the input. input is suspect.
	 *
	 * @param inputObject non-null
	 * @param ctx         non-null. any validation error is added to it
	 * @return true record with parsed values. null if any input fails validation.
	 */
	public boolean parseKeys(final IInputData inputObject, final IServiceContext ctx) {
		if (!this.isDb) {
			logger.error("This form is based on {} that is not a DbRecord. Keys can not be parsed");
			return false;
		}
		return ((DbRecord) this.record).parseKeys(inputObject, ctx);
	}

	/**
	 *
	 * @param operation
	 * @return a service for this operation on the form. null if the operation is
	 *         not allowed.
	 */
	public IService getService(final IoType operation) {
		if (!this.operations[operation.ordinal()]) {
			logger.info("{} operation is not allowed on record {}", operation, this.name);
			return null;
		}

		String serviceName = operation.name();
		serviceName = serviceName.substring(0, 1).toLowerCase() + serviceName.substring(1) + '_' + this.name;

		/*
		 * forms with children require form-based service
		 */
		if (this.childForms != null) {
			switch (operation) {
			case Get:
				return new Reader(serviceName);
			case Create:
				return new Creater(serviceName);
			case Update:
				return new Updater(serviceName);
			case Delete:
				return new Deleter(serviceName);
			case Filter:
				return new Filter(serviceName);
			default:
				throw new ApplicationError("Form needs to be designed for operation " + operation.name());
			}
		}

		/*
		 * form is simply a wrapper on the record..
		 */
		if (this.isDb) {
			return ((DbRecord) this.record).getService(operation, serviceName);
		}

		return new AutoService(serviceName, operation);
	}

	/*
	 * there is very little we can do as an auto-service. Just for
	 * testing/development purpose?? we will add other features later..
	 */
	protected class AutoService extends AbstractService {
		private IoType operation;

		protected AutoService(final String name, IoType operation) {
			super(name);
			this.operation = operation;
		}

		@Override
		public void serve(final IServiceContext ctx, final IInputData inputPayload) throws Exception {
			Form.this.record.parse(inputPayload, this.operation == IoType.Create, ctx, null, 0);
			if (ctx.allOk()) {
				logger.info("Service " + this.serviceName + " succeeded in parsing input. Same is set as response");
				ctx.setAsResponse(Form.this.record);
				return;
			}
			logger.error("Validation failed for service {} and operation {}", this.serviceName, this.operation.name());
		}

	}

	protected class Reader extends AbstractService {

		protected Reader(final String name) {
			super(name);
		}

		@Override
		public void serve(final IServiceContext ctx, final IInputData payload) throws Exception {
			if (!Form.this.parseKeys(payload, ctx)) {
				logger.error("Error while reading keys from the input payload");
				return;
			}

			final DbRecord rec = (DbRecord) Form.this.record;
			AppManager.getApp().getDbDriver().doReadonlyOperations(handle -> {
				if (!rec.read(handle)) {
					logger.error("No data found for the requested keys");
					ctx.addMessage(Message.newError(Conventions.MessageId.INVALID_DATA));
					return;
				}
				/*
				 * instead of storing data and then serializing it, we have designed this
				 * service to serialize data then-and-there
				 */
				final IOutputData outData = ctx.getOutputData();
				outData.beginObject();
				outData.addRecord(rec);

				for (final ChildForm<?> child : Form.this.childForms) {
					child.read(rec, outData, handle);
				}
				outData.endObject();
			});
		}
	}

	protected class Creater extends AbstractService {

		protected Creater(final String name) {
			super(name);
		}

		@Override
		public void serve(final IServiceContext ctx, final IInputData payload) throws Exception {
			final DbRecord rec = (DbRecord) Form.this.record;
			if (!rec.parse(payload, true, ctx, null, 0)) {
				logger.error("Error while validating the input payload");
				return;
			}
			AppManager.getApp().getDbDriver().doReadWriteOperations(handle -> {
				if (!rec.insert(handle)) {
					logger.error("Insert operation failed silently");
					ctx.addMessage(Message.newError(Conventions.MessageId.INVALID_DATA));
					return false;
				}
				for (final ChildForm<?> lf : Form.this.childForms) {
					if (!lf.insert(rec, payload, handle, ctx)) {
						logger.error("Insert operation failed for a child form");
						ctx.addMessage(Message.newError(Conventions.MessageId.INVALID_DATA));
						return false;
					}
				}
				return true;
			});
		}
	}

	protected class Updater extends AbstractService {

		protected Updater(final String name) {
			super(name);
		}

		@Override
		public void serve(final IServiceContext ctx, final IInputData payload) throws Exception {
			final DbRecord rec = (DbRecord) Form.this.record;
			if (!rec.parse(payload, false, ctx, null, 0)) {
				logger.error("Error while validating the input payload");
				return;
			}

			AppManager.getApp().getDbDriver().doReadWriteOperations(handle -> {
				if (!rec.update(handle)) {
					logger.error("update operation failed silently");
					ctx.addMessage(Message.newError(Conventions.MessageId.INVALID_DATA));
					return false;
				}
				for (final ChildForm<?> lf : Form.this.childForms) {
					if (!lf.update(rec, payload, handle, ctx)) {
						logger.error("Update operation failed for a child form");
						ctx.addMessage(Message.newError(Conventions.MessageId.INVALID_DATA));
						return false;

					}
				}
				return true;
			});
		}
	}

	protected class Deleter extends AbstractService {

		protected Deleter(final String name) {
			super(name);
		}

		@Override
		public void serve(final IServiceContext ctx, final IInputData payload) throws Exception {
			final DbRecord rec = (DbRecord) Form.this.record;
			if (!rec.parseKeys(payload, ctx)) {
				logger.error("Error while validating keys");
				return;
			}

			AppManager.getApp().getDbDriver().doReadWriteOperations(handle -> {
				if (!rec.delete(handle)) {
					logger.error("Delete operation failed silently");
					ctx.addMessage(Message.newError(Conventions.MessageId.INVALID_DATA));
					return false;
				}

				for (final ChildForm<?> lf : Form.this.childForms) {
					if (!lf.delete(rec, handle, ctx)) {
						logger.error("Insert operation failed for a child form");
						ctx.addMessage(Message.newError(Conventions.MessageId.INVALID_DATA));
						return false;
					}
				}
				return true;
			});
		}
	}

	protected class Filter extends AbstractService {

		protected Filter(final String name) {
			super(name);
		}

		@Override
		public void serve(final IServiceContext ctx, final IInputData payload) throws Exception {
			logger.info("Form service invoked for filter for {}", this.getId());
			final DbRecord rec = (DbRecord) Form.this.record;
			final ParsedFilter filter = rec.dba.parseFilter(payload, ctx);

			if (filter == null) {
				logger.error("Error while parsing filter conditions from the input payload");
				return;
			}

			AppManager.getApp().getDbDriver().doReadonlyOperations(handle -> {
				final List<Object[]> list = rec.dba.filter(handle, filter.getWhereClause(),
						filter.getWhereParamValues(), filter.getWhereParamTypes(), null);
				/*
				 * instead of storing data and then serializing it, we have designed this
				 * service to serialize data then-and-there
				 */
				final IOutputData outData = ctx.getOutputData();
				outData.beginObject();
				outData.addName(Conventions.Request.TAG_LIST);
				outData.beginArray();

				if (list.size() == 0) {
					logger.warn("No rows filtered. Responding with empty list");
				} else {
					for (final Object[] row : list) {
						final DbRecord r = rec.newInstance(row);
						outData.beginObject();
						outData.addRecord(r);
						for (final ChildForm<?> child : Form.this.childForms) {
							child.read(r, outData, handle);
						}
						outData.endObject();
					}
				}

				outData.endArray();
				outData.endObject();
			});
		}
	}

	/**
	 * @return underlying record for this form
	 */
	public T getRecord() {
		return this.record;
	}

	/**
	 * @param ctx
	 */
	@SuppressWarnings("unchecked")
	public void override(final IServiceContext ctx) {
		final String recordName = this.record.fetchName();
		this.record = (T) AppManager.getApp().getCompProvider().getRecord(recordName, ctx);
		if (this.childForms != null) {
			for (final ChildForm<?> lf : this.childForms) {
				lf.override(this.record, ctx);
			}
		}
	}

	/**
	 * @return unique name of this form
	 */
	public String getName() {
		return this.name;
	}
}
