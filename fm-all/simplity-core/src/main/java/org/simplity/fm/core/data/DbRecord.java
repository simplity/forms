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
import java.util.ArrayList;
import java.util.List;

import org.simplity.fm.core.ApplicationError;
import org.simplity.fm.core.Conventions;
import org.simplity.fm.core.Message;
import org.simplity.fm.core.app.AppManager;
import org.simplity.fm.core.db.FilterOperator;
import org.simplity.fm.core.db.IReadWriteHandle;
import org.simplity.fm.core.db.IReadonlyHandle;
import org.simplity.fm.core.filter.FilterCondition;
import org.simplity.fm.core.filter.FilterParams;
import org.simplity.fm.core.json.JsonUtil;
import org.simplity.fm.core.service.AbstractService;
import org.simplity.fm.core.service.IInputData;
import org.simplity.fm.core.service.IService;
import org.simplity.fm.core.service.IServiceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Extends a record to add functionality to link the underlying data-structure
 * to a database/persistence
 * </p>
 * <p>
 * concrete classed must add named getters and setters to the record so that the
 * user-code is type-safe
 * </p>
 *
 * @author simplity.org
 *
 */
public abstract class DbRecord extends Record {
	protected static final Logger logger = LoggerFactory.getLogger(DbRecord.class);

	protected final Dba dba;

	protected DbRecord(final Dba dba, final RecordMetaData meta, final Object[] fieldValues) {
		super(meta, fieldValues);
		this.dba = dba;
	}

	@Override
	public boolean parse(final IInputData inputObject, final boolean forInsert, final IServiceContext ctx,
			final String tableName, final int rowNbr) {
		if (!super.parse(inputObject, forInsert, ctx, tableName, rowNbr)) {
			return false;
		}
		/*
		 * validate db-specific fields
		 */
		return this.dba.validate(this.fieldValues, forInsert, ctx, tableName, rowNbr);
	}

	/**
	 * load keys from a JSON. input is suspect.
	 *
	 * @param inputObject non-null
	 * @param ctx         non-null. any validation error is added to it
	 * @return true if all ok. false if any parse error is added the ctx
	 */
	public boolean parseKeys(final IInputData inputObject, final IServiceContext ctx) {
		return this.dba.parseKeys(inputObject, this.fieldValues, ctx);
	}

	/**
	 * Use this method if the key values are known but not with their names
	 * 
	 * Sets the values in the array to the key fields, in that order, after
	 * validating them.
	 * 
	 *
	 * @param inputObject non-null
	 * @param ctx         non-null. any validation error is added to it
	 * @return true if all ok. false if any parse error is added the ctx
	 */
	public boolean setkeys(final Object[] keyValues, final IServiceContext ctx) {
		return this.dba.setKeys(keyValues, this.fieldValues, ctx);
	}

	/**
	 * fetch data for this form from a db
	 *
	 * @param handle
	 *
	 * @return true if it is read.false if no data found for this form (key not
	 *         found...)
	 * @throws SQLException
	 */
	public boolean read(final IReadonlyHandle handle) throws SQLException {
		return this.dba.read(handle, this.fieldValues);
	}

	/**
	 * read is expected to succeed. hence an exception is thrown in case if no row
	 * is not read
	 *
	 * @param handle
	 *
	 * @throws SQLException
	 */
	public void readOrFail(final IReadonlyHandle handle) throws SQLException {
		if (!this.dba.read(handle, this.fieldValues)) {
			throw new SQLException("Read failed for " + this.fetchName() + this.dba.emitKeys(this.fieldValues));
		}
	}

	/**
	 * get the count of rows from the underlying table as per the filter conditions
	 *
	 * @param handle       readOnly handle
	 * @param filterParams required parameters for the filter operation
	 * @param ctx          In case of any errors in filterParams, they are added to
	 *                     the service context
	 * @return number of rows. -1 in case of any errors.
	 * @throws SQLException
	 */
	public long countRows(final IReadonlyHandle handle, FilterParams filterParams, IServiceContext ctx)
			throws SQLException {
		return this.dba.countRows(handle, filterParams, ctx);
	}

	/**
	 * insert/create this form data into the db.
	 *
	 * @param handle
	 *
	 * @return true if it is created. false in case it failed because of an an
	 *         existing form with the same id/key
	 * @throws SQLException
	 */
	public boolean insert(final IReadWriteHandle handle) throws SQLException {
		return this.dba.insert(handle, this.fieldValues);
	}

	/**
	 * insert is expected to succeed. hence an exception is thrown in case if no row
	 * is not inserted
	 *
	 * @param handle
	 *
	 * @throws SQLException
	 */
	public void insertOrFail(final IReadWriteHandle handle) throws SQLException {
		if (!this.dba.insert(handle, this.fieldValues)) {
			throw new SQLException(
					"Insert failed silently for " + this.fetchName() + this.dba.emitKeys(this.fieldValues));
		}
	}

	/**
	 * update this form data back into the db.
	 *
	 * @param handle
	 *
	 * @return true if it is indeed updated. false in case there was no row to
	 *         update
	 * @throws SQLException
	 */
	public boolean update(final IReadWriteHandle handle) throws SQLException {
		return this.dba.update(handle, this.fieldValues);
	}

	/**
	 * update is expected to succeed. hence an exception is thrown in case if no row
	 * is updated
	 *
	 * @param handle
	 *
	 * @throws SQLException
	 */
	public void updateOrFail(final IReadWriteHandle handle) throws SQLException {
		if (!this.dba.update(handle, this.fieldValues)) {
			throw new SQLException(
					"Update failed silently for " + this.fetchName() + this.dba.emitKeys(this.fieldValues));
		}
	}

	/**
	 * insert or update this, based on the primary key. possible only if the primary
	 * key is generated
	 *
	 * @param handle
	 * @return true if it was indeed saved
	 * @throws SQLException
	 */
	public boolean save(final IReadWriteHandle handle) throws SQLException {
		return this.dba.save(handle, this.fieldValues);
	}

	/**
	 * @param handle
	 * @throws SQLException
	 */
	public void saveOrFail(final IReadWriteHandle handle) throws SQLException {
		if (!this.dba.save(handle, this.fieldValues)) {
			throw new SQLException(
					"Save failed silently for " + this.fetchName() + this.dba.emitKeys(this.fieldValues));
		}

	}

	/**
	 * remove this form data from the db
	 *
	 * @param handle
	 *
	 * @return true if it is indeed deleted happened. false otherwise
	 * @throws SQLException
	 */
	public boolean delete(final IReadWriteHandle handle) throws SQLException {
		return this.dba.delete(handle, this.fieldValues);
	}

	/**
	 * delete is expected to succeed. hence an exception is thrown in case if no row
	 * is not deleted
	 *
	 * @param handle
	 *
	 * @throws SQLException
	 */
	public void deleteOrFail(final IReadWriteHandle handle) throws SQLException {
		if (!this.dba.delete(handle, this.fieldValues)) {
			throw new SQLException(
					"Delete failed silently for " + this.fetchName() + this.dba.emitKeys(this.fieldValues));
		}
	}

	/**
	 * create a simple set of filter conditions based on the values found for the
	 * fields in this record in the supplied record. This is to be used with
	 * caution, as this API is quite lenient. Any incompatible value is just
	 * ignored. This should be used only after any user input is validated as per
	 * the API requirement of the calling method
	 * 
	 * @param record   in which to look for values for fields
	 * @param non-null list to which filter conditions, if any, are added rejected
	 *                 without any error message
	 */
	public void filterConditionsFromAnotherRecord(Record record, List<FilterCondition> conditions) {
		Object[] inputData = record.fieldValues;
		for (Field inputField : record.fetchFields()) {
			String fieldName = inputField.getName();
			Field field = this.fetchField(fieldName);
			if (field == null) {
				continue;
			}

			Object inputValue = inputData[inputField.getIndex()];
			if (inputValue != null) {
				FilterCondition condition = new FilterCondition();
				condition.comparator = FilterOperator.Equal.getText();
				condition.field = fieldName;
				condition.value = inputValue.toString();
				conditions.add(condition);
			}

		}

	}

	@Override
	public DbRecord makeACopy() {
		return this.newInstance(this.fieldValues);
	}

	@Override
	public DbRecord newInstance() {
		return this.newInstance(null);
	}

	@Override
	public abstract DbRecord newInstance(Object[] values);

	/**
	 * get a service for the specific operation on this record
	 *
	 * @param operation   non-null
	 * @param serviceName optional. If not specified, it is composed as
	 *                    operation_recordName* used
	 * @return service if this record is designed for this operation. null
	 *         otherwise.
	 */
	public IService getService(final IoType operation, final String serviceName) {
		if (!this.dba.operationAllowed(operation)) {
			logger.info("{} operation is not allowed on record {}", operation, this.fetchName());
			return null;
		}

		String sn = serviceName;
		if (sn == null || sn.isEmpty()) {
			sn = operation.name();
			sn = serviceName.substring(0, 1).toLowerCase() + serviceName.substring(1) + '_' + this.fetchName();
		}
		switch (operation) {
		case Get:
			return new Reader(sn);
		case Create:
			return new Creater(sn);
		case Update:
			return new Updater(sn);
		case Delete:
			return new Deleter(sn);
		case Filter:
			return new Filter(sn);
		default:
			throw new ApplicationError("DbRecord needs to be designed for operation " + operation.name());
		}
	}

	protected class Reader extends AbstractService {

		protected Reader(final String name) {
			super(name);
		}

		@Override
		public void serve(final IServiceContext ctx, final IInputData payload) throws Exception {
			final DbRecord rec = DbRecord.this.newInstance();
			if (!rec.parseKeys(payload, ctx)) {
				logger.error("Error while reading keys from the input payload");
				return;
			}

			AppManager.getApp().getDbDriver().doReadonlyOperations(handle -> {
				boolean ok = rec.read(handle);
				if (!ok) {
					logger.error("No data found for the requested keys");
					ctx.addMessage(Message.newError(Conventions.MessageId.INVALID_DATA));
				}
				return ok;
			});

			if (ctx.allOk()) {
				ctx.setAsResponse(rec.fetchFieldNames(), rec.fieldValues);
			}
		}

	}

	protected class Creater extends AbstractService {

		protected Creater(final String name) {
			super(name);
		}

		@Override
		public void serve(final IServiceContext ctx, final IInputData payload) throws Exception {
			final DbRecord rec = DbRecord.this.newInstance();
			if (!rec.parse(payload, true, ctx, null, 0)) {
				logger.error("Error while validating the input payload");
				return;
			}

			AppManager.getApp().getDbDriver().doReadWriteOperations(handle -> {
				if (rec.insert(handle)) {
					return true;
				}

				logger.error("Insert operation failed silently");
				ctx.addMessage(Message.newError(Conventions.MessageId.INVALID_DATA));
				return false;
			});
		}
	}

	protected class Updater extends AbstractService {

		protected Updater(final String name) {
			super(name);
		}

		@Override
		public void serve(final IServiceContext ctx, final IInputData payload) throws Exception {
			final DbRecord rec = DbRecord.this.newInstance();
			if (!rec.parse(payload, false, ctx, null, 0)) {
				logger.error("Error while validating data from the input payload");
				return;
			}

			AppManager.getApp().getDbDriver().doReadWriteOperations(handle -> {
				if (rec.update(handle)) {
					return true;
				}

				logger.error("Update operation failed silently");
				ctx.addMessage(Message.newError(Conventions.MessageId.INVALID_DATA));
				return false;
			});
		}
	}

	protected class Deleter extends AbstractService {

		protected Deleter(final String name) {
			super(name);
		}

		@Override
		public void serve(final IServiceContext ctx, final IInputData payload) throws Exception {
			final DbRecord rec = DbRecord.this.newInstance();
			if (!rec.parseKeys(payload, ctx)) {
				logger.error("Error while validating keys");
				return;
			}

			AppManager.getApp().getDbDriver().doReadWriteOperations(handle -> {
				if (rec.delete(handle)) {
					return true;
				}

				logger.error("Delete operation failed silently");
				ctx.addMessage(Message.newError(Conventions.MessageId.INVALID_DATA));
				return false;
			});

		}
	}

	protected class Filter extends AbstractService {

		protected Filter(final String name) {
			super(name);
		}

		@Override
		public void serve(final IServiceContext ctx, final IInputData payload) throws Exception {
			final DbRecord rec = DbRecord.this.newInstance();
			String tableName = payload.getString(Conventions.Request.TAG_TABLE_NAME);
			if (tableName == null || tableName.isEmpty()) {
				tableName = Conventions.Request.TAG_LIST;
			}

			FilterParams params = JsonUtil.load(payload, FilterParams.class);
			if (params == null) {
				ctx.addMessage(Message.newError(Conventions.MessageId.INVALID_DATA));
				logger.error("Input data for filter parameter did not follow the required data structure", ctx);
				return;
			}

			final FilterDetails filter = rec.dba.prepareFilterDetails(params, ctx);
			if (filter == null) {
				logger.error("Error while parsing filter conditions from the input payload");
				return;
			}

			final List<Object[]> rows = new ArrayList<>();

			boolean readOk = AppManager.getApp().getDbDriver().doReadonlyOperations(handle -> {
				int n = handle.readMany(filter.getSql(), filter.getParamValues(), filter.getParamTypes(),
						filter.getOutputTypes(), rows);
				return n > 0;
			});
			if (!readOk) {
				logger.warn("No rows filtered. Responding with empty list");
			}

			ctx.setAsResponse(tableName, filter.getOutputNames(), rows);
			return;
			/**
			 * TODO: If some fields in record are not DbFields, then the output row does not
			 * match the fields in the record. By default, client may expect values for all
			 * fields in the record, should we use "all-fields" and "dbFields" as the column
			 * names?
			 * 
			 * Note:
			 * 
			 * 1. In any case, we respond back in JSON, and hence the order of fields is
			 * irrelevant
			 * 
			 * 2. Even if we were to add all fields, the non-DbFields will have undefined as
			 * their value.
			 * 
			 * Hence we will simply send only the selected fields.
			 */
		}

	}

	/**
	 * fetch is used instead of get to avoid clash with getters in generated classes
	 *
	 * @param fieldName
	 * @return db field specified by this name, or null if there is no such name
	 */
	public DbField fetchField(final String fieldName) {
		return this.dba.getField(fieldName);
	}

	/**
	 * fetch is used instead of get to avoid clash with getters in generated classes
	 *
	 * @return index of the generated key, or -1 if this record has no generated key
	 */
	public int fetchGeneratedKeyIndex() {
		return this.dba.getGeneratedKeyIndex();
	}

}
