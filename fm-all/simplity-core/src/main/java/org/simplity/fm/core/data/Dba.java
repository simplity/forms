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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.simplity.fm.core.Conventions;
import org.simplity.fm.core.Message;
import org.simplity.fm.core.db.FilterOperator;
import org.simplity.fm.core.db.IReadWriteHandle;
import org.simplity.fm.core.db.IReadonlyHandle;
import org.simplity.fm.core.db.IRowProcessor;
import org.simplity.fm.core.filter.FilterCondition;
import org.simplity.fm.core.filter.FilterParams;
import org.simplity.fm.core.filter.SortBy;
import org.simplity.fm.core.service.IInputData;
import org.simplity.fm.core.service.IServiceContext;
import org.simplity.fm.core.valueschema.ValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages persistence related functionality for a <code>DbRecord</code> NOTE:
 * most of the methods are package-private rather than public. That is because
 * this class itself is unlikely to be exposed to public. This is to keep the
 * flexibility for re-factoring within the package as much as possible
 *
 * @author simplity.org
 *
 */
public class Dba {
	private static final String IN = " IN (";
	private static final String LIKE = " LIKE ? escape '\\'";
	private static final String BETWEEN = " BETWEEN ";
	private static final String WILD_CARD = "%";
	private static final String ESCAPED_WILD_CARD = "\\%";
	private static final String WILD_CHAR = "_";
	private static final String ESCAPED_WILD_CHAR = "\\_";
	private static final char QN = '?';

	protected static final Logger logger = LoggerFactory.getLogger(Dba.class);
	/**
	 * table/view name in the database
	 */
	private final String nameInDb;

	/**
	 * operations like get etc.. are valid? array index corresponds to integer value
	 * of the enum IoType
	 */
	private final boolean[] allowedOperations;
	/**
	 * fields that are mapped to the db. This is same as fields in the record,
	 * except any non-db-fields are replaced with null.
	 */
	private final DbField[] dbFields;

	/**
	 * e.g. where a=? and b=?
	 */
	private final String whereClause;
	/**
	 * db parameters to be used for the where clause
	 */
	private final int[] whereIndexes;
	private final ValueType[] whereTypes;
	/**
	 * e.g. select a,b,c from t
	 */
	private final String selectClause;
	/**
	 * for extracting values from select result
	 */
	private final ValueType[] selectTypes;
	private final int[] selectIndexes;
	/**
	 * e.g insert a,b,c,d into table1 values(?,?,?,?)
	 */
	private final String insertClause;
	/**
	 * db parameters for the insert sql
	 */
	private final int[] insertIndexes;
	private final ValueType[] insertTypes;

	/**
	 * e.g. update table1 set a=?, b=?, c=?
	 */
	private final String updateClause;
	/**
	 * db parameters for the update sql
	 */
	private final int[] updateIndexes;
	private final ValueType[] updateTypes;

	/**
	 * e.g. delete from table1. Note that where is not part of this.
	 */
	private final String deleteClause;

	/*
	 * following fields are also final, but it is bit complex to adhere to the
	 * syntax for setting final fields. Hence we have not declared them final
	 */
	/**
	 * FINAL. primary key column/s. most of the times, it is one field that is
	 * internally generated
	 */
	private int[] keyIndexes;
	private ValueType[] keyTypes;
	/**
	 * FINAL. db column name that is generated as internal key. null if this is not
	 * relevant
	 */
	private String generatedColumnName;

	/**
	 * FINAL. index to the generatedKey
	 */
	private int generatedKeyIdx = -1;

	/**
	 * FINAL. if this APP is designed for multi-tenant deployment, and this table
	 * has data across tenants..
	 */
	private DbField tenantField;

	/**
	 * FINAL. if this table allows update, and needs to use time-stamp-match
	 * technique to avoid concurrent updates.. NOT enabled in the meta data yet.
	 */
	@SuppressWarnings("unused")
	private final DbField timestampField = null;

	/**
	 *
	 * @param allFields
	 * @param nameInDb
	 * @param opers
	 * @param selectClause
	 * @param selectIndexes
	 * @param insertClause
	 * @param insertIndexes
	 * @param updateClause
	 * @param updateIndexes
	 * @param deleteClause
	 * @param whereClause
	 * @param whereIndexes
	 */
	public Dba(final Field[] allFields, final String nameInDb, final boolean[] opers, final String selectClause,
			final int[] selectIndexes, final String insertClause, final int[] insertIndexes, final String updateClause,
			final int[] updateIndexes, final String deleteClause, final String whereClause, final int[] whereIndexes) {

		this.dbFields = new DbField[allFields.length];
		this.prepareFields(allFields);
		if (this.keyIndexes != null) {
			this.keyTypes = typesOfFields(allFields, this.keyIndexes);
		}
		this.allowedOperations = opers;
		this.nameInDb = nameInDb;

		this.selectClause = selectClause;
		this.selectIndexes = selectIndexes;
		this.selectTypes = typesOfFields(allFields, selectIndexes);

		this.insertClause = insertClause;
		this.insertIndexes = insertIndexes;
		this.insertTypes = typesOfFields(allFields, insertIndexes);

		this.updateClause = updateClause;
		this.updateIndexes = updateIndexes;
		this.updateTypes = typesOfFields(allFields, updateIndexes);

		this.whereClause = whereClause;
		this.whereIndexes = whereIndexes;
		this.whereTypes = typesOfFields(allFields, whereIndexes);

		this.deleteClause = deleteClause;

	}

	/**
	 * get types-array for the given fields array
	 *
	 * @param fields
	 * @param indexes
	 * @return
	 */
	private static ValueType[] typesOfFields(Field[] fields, int[] indexes) {
		if (indexes == null) {
			return null;
		}
		ValueType[] types = new ValueType[indexes.length];
		for (int i = 0; i < types.length; i++) {
			types[i] = fields[indexes[i]].getValueType();
		}
		return types;
	}

	private void prepareFields(final Field[] allFields) {

		final int keys[] = new int[allFields.length];
		int nbrKeys = 0;
		for (int i = 0; i < allFields.length; i++) {
			final DbField fld = (DbField) allFields[i];
			this.dbFields[i] = fld;
			final FieldType ct = fld.getFieldType();
			if (ct == null) {
				/*
				 * not a true db field
				 */
				continue;
			}
			switch (ct) {
			case TenantKey:
				this.tenantField = fld;
				continue;

			case GeneratedPrimaryKey:
				this.generatedColumnName = fld.getColumnName();
				this.generatedKeyIdx = fld.getIndex();
				keys[nbrKeys] = fld.getIndex();
				nbrKeys++;
				continue;

			case PrimaryKey:
				keys[nbrKeys] = fld.getIndex();
				nbrKeys++;
				continue;

			default:
				continue;
			}
		}
		if (nbrKeys > 0) {
			this.keyIndexes = Arrays.copyOf(keys, nbrKeys);
		}

	}

	/**
	 *
	 * @return index of the generated key, or -1 if this record has no generated key
	 */
	public int getGeneratedKeyIndex() {
		return this.generatedKeyIdx;
	}

	/**
	 *
	 * @return name of the table/view associated with this db record
	 */
	public String getNameInDb() {
		return this.nameInDb;
	}

	/**
	 * return the select clause (like select a,b,...) without the where clause for
	 * this record
	 *
	 * @return string that is a valid select-part of a sql that can be used with a
	 *         were clause to filter rows from the underlying dbtable.view
	 */
	public String getSelectClause() {
		return this.selectClause;
	}

	/**
	 * fetch data for this form from a db based on the primary key of this record
	 *
	 * @param handle
	 * @param row    row-data for this record. Array of objects, one for each field
	 *               in this record this acts as source of values for where-clause
	 *               parameter, as well as destination for selected fields in the
	 *               query
	 *
	 * @return true if it is read. false if no data found for this record (key not
	 *         found...)
	 * @throws SQLException
	 */
	boolean read(final IReadonlyHandle handle, final Object[] row) throws SQLException {
		if (this.keyIndexes == null) {
			return notAllowed(IoType.Get);
		}

		// sql parameters from row-data
		Object[] params = copyFromRow(row, this.whereIndexes, null);

		Object[] result = new Object[this.selectTypes.length];
		final boolean ok = handle.read(this.selectClause + ' ' + this.whereClause, params, this.whereTypes,
				this.selectTypes, result);
		// copy selected fields into row-data
		if (ok) {
			copyFromRow(result, this.selectIndexes, row);
		}
		return ok;
	}

	/**
	 * to be called from the parent Record. not to be called by other classes
	 * 
	 * @param handle       readOnly handle
	 * @param filterParams required parameters for the filter operation
	 * @param dataTable    to which the filtered rows are to be added
	 * @param ctx          In case of any errors in filterParams, they are added to
	 *                     the service context
	 * @return true if the filterParams was appropriate and the filter operation was
	 *         executed. False in case of any error in forming the sql based on the
	 *         filter parameters Note that returned value of true does not mean that
	 *         rows were added to the data table
	 * @throws SQLException
	 */
	public boolean filter(final IReadonlyHandle handle, final FilterParams filterParams, DataTable<?> dataTable,
			IServiceContext ctx) throws SQLException {

		FilterDetails fd = this.prepareFilterDetails(filterParams, ctx);
		if (fd == null) {
			return false;
		}
		handle.readIntoDataTable(fd.getSql(), fd.getParamValues(), fd.getParamTypes(), dataTable);

		return true;
	}

	private static final ValueType[] COUNT_OUTPUT_TYPES = { ValueType.Integer };

	/**
	 * to be called from the parent Record. not to be called by other classes
	 * 
	 * @param handle       readOnly handle
	 * @param filterParams required parameters for the filter operation
	 * @param ctx          In case of any errors in filterParams, they are added to
	 *                     the service context
	 * @return number of rows. -1 in case of any errors.
	 * @throws SQLException
	 */
	public long countRows(final IReadonlyHandle handle, final FilterParams filterParams, IServiceContext ctx)
			throws SQLException {

		FilterDetails fd = this.prepareCountSql(filterParams, ctx);
		if (fd == null) {
			return -1;
		}
		Object[] counts = new Object[1];
		handle.read(fd.getSql(), fd.getParamValues(), fd.getParamTypes(), COUNT_OUTPUT_TYPES, counts);

		return (long) counts[0];
	}

	/**
	 * process each row selected based on the where clause
	 *
	 * @param handle
	 * @param where           should start with 'where' e.g. 'where a=? and b=?...".
	 *                        null to select all rows from this table
	 * @param parameterValues values, in the right order, for the parameters in the
	 *                        where clause. null if were is null.
	 * @param value           types of parameter values
	 * @param rowProcessor    class/lambda that is called for each output row from
	 *                        the query
	 * @throws SQLException
	 */
	void forEach(final IReadonlyHandle handle, final String where, final Object[] parameterValues,
			final ValueType[] parameterTypes, final IRowProcessor rowProcessor) throws SQLException {

		String sql = this.selectClause;
		if (where != null) {
			sql += this.selectClause + ' ' + where;
		}

		handle.readWithRowProcessor(sql, parameterValues, parameterTypes, this.selectTypes, rowProcessor);

		return;
	}

	/**
	 * insert/create this record into the db.
	 *
	 * @param handle
	 * @param rowToInsert data for this record that has values for all the fields in
	 *                    the right order
	 *
	 * @return true if it is created. false in case it failed because of an an
	 *         existing form with the same id/key
	 * @throws SQLException
	 */
	boolean insert(final IReadWriteHandle handle, final Object[] rowToInsert) throws SQLException {
		if (this.insertIndexes == null) {
			return notAllowed(IoType.Create);
		}

		final Object[] params = copyFromRow(rowToInsert, this.insertIndexes, null);
		if (this.generatedColumnName == null) {
			return handle.write(this.insertClause, params, this.insertTypes) > 0;

		}

		final long[] generatedKeys = new long[1];
		int n = handle.insertWithKeyGeneration(this.insertClause, params, this.insertTypes, this.generatedColumnName,
				generatedKeys);
		if (n == 0) {
			return false;
		}

		final long id = generatedKeys[0];
		if (id == 0) {
			logger.error("DB handler did not return generated key");
		} else {
			rowToInsert[this.generatedKeyIdx] = id;
			logger.info("Generated key {} assigned back to form data", id);
		}

		return true;

	}

	/**
	 * update this record data into the db.
	 *
	 * @param handle
	 * @param rowToUpdate
	 *
	 * @return true if it is indeed updated. false in case there was no row to
	 *         update
	 * @throws SQLException
	 */
	boolean update(final IReadWriteHandle handle, final Object[] rowToUpdate) throws SQLException {
		if (this.keyIndexes == null) {
			return notAllowed(IoType.Update);
		}

		final Object[] params = copyFromRow(rowToUpdate, this.updateIndexes, null);

		return handle.write(this.updateClause, params, this.updateTypes) > 0;

	}

	/**
	 * remove this form data from the db
	 *
	 * @param handle
	 * @param rowToDelete row-data for this record that has values for the fields
	 *                    that are required to identify the row to be deleted
	 *
	 * @return true if it is indeed deleted. false otherwise
	 * @throws SQLException
	 */
	boolean delete(final IReadWriteHandle handle, final Object[] rowToDelete) throws SQLException {
		if (this.keyIndexes == null) {
			return notAllowed(IoType.Delete);
		}

		final Object[] params = copyFromRow(rowToDelete, this.keyIndexes, null);
		return handle.write(this.deleteClause, params, this.keyTypes) > 0;

	}

	/**
	 * save a data-row rows into the db. The record must have a generated key as its
	 * primary key fpr this operation to be meaningful. If the key exists in the
	 * data-row, then it is updated, else it is inserted
	 *
	 * @param handle
	 *
	 * @param fieldValues data to be saved
	 * @return true if the save succeeded, false otherwise. This value can be used
	 *         to commit/roll-back the transaction
	 * @throws SQLException
	 */
	boolean save(final IReadWriteHandle handle, final Object[] fieldValues) throws SQLException {
		if (this.keyIndexes == null) {
			return notAllowed(IoType.Update);
		}

		boolean ok = this.update(handle, fieldValues);
		if (!ok) {
			ok = this.insert(handle, fieldValues);
			;
		}

		return ok;
	}

	/**
	 * save all rows into the db. Each row is inspected to check for the generated
	 * primary key. If the key exists, that row is updated, else it is inserted. .
	 *
	 * @param handle
	 *
	 * @param rows   data to be saved
	 * @return true if all ok. false in case one or more rows failed to save. This
	 *         can be used to commit/roll-back the transaction
	 * @throws SQLException
	 */
	boolean saveAll(final IReadWriteHandle handle, final Object[][] rows) throws SQLException {
		if (this.keyIndexes == null) {
			return notAllowed(IoType.Update);
		}

		boolean allOk = true;

		for (final Object[] row : rows) {
			boolean ok = this.update(handle, row);
			if (!ok) {
				ok = this.insert(handle, row);
				if (!ok) {
					allOk = false;
				}
			}
		}
		return allOk;
	}

	/**
	 * insert all rows. NOTE: caller must consider rolling-back if false is returned
	 *
	 * @param handle
	 *
	 * @param rows   data to be saved
	 * @return true if every one row was inserted. false if any one row failed to
	 *         insert.
	 * @throws SQLException
	 */
	boolean insertAll(final IReadWriteHandle handle, final Object[][] rows) throws SQLException {
		if (this.keyIndexes == null) {
			return notAllowed(IoType.Create);
		}

		int nbrRows = rows.length;
		final Object[][] paramValues = copyFromRows(rows, this.insertIndexes);
		int nbrInserted = 0;

		if (this.generatedColumnName == null) {
			nbrInserted = handle.writeMany(this.insertClause, paramValues, this.insertTypes);
		} else {
			final long[] generatedKeys = new long[nbrRows];
			nbrInserted = handle.insertWithKeyGenerations(this.insertClause, paramValues, this.insertTypes,
					this.generatedColumnName, generatedKeys);
			if (nbrInserted > 0) {
				this.copyKeys(rows, generatedKeys);
			}
		}
		return nbrInserted == nbrRows;
	}

	/**
	 * update all rows. NOTE: caller must consider rolling-back if false is returned
	 *
	 * @param handle
	 *
	 * @param rows   data to be saved
	 * @return true if every one row was successfully updated. false if any one row
	 *         failed to update
	 * @throws SQLException
	 */
	boolean updateAll(final IReadWriteHandle handle, final Object[][] rows) throws SQLException {
		if (this.keyIndexes == null) {
			return notAllowed(IoType.Update);
		}

		int nbrRows = rows.length;
		final Object[][] updateValues = copyFromRows(rows, this.updateIndexes);

		int n = handle.writeMany(this.updateClause, updateValues, this.updateTypes);
		return n == nbrRows;
	}

	/**
	 * validate the data row for db-operation. This is to be invoked after the row
	 * is parsed/validated as a valid record (non-db)
	 *
	 * @param data
	 * @param rowNbr
	 * @param tableName
	 * @param ctx
	 * @param forInsert
	 * @return true if all ok. false if any error message is added to the context
	 */
	public boolean validate(final Object[] data, final boolean forInsert, final IServiceContext ctx,
			final String tableName, final int rowNbr) {
		boolean ok = true;
		for (final DbField field : this.dbFields) {
			if (field != null) {
				if (!field.validate(data, forInsert, ctx, tableName, rowNbr)) {
					ok = false;
				}
			}
		}
		return ok;
	}

	/**
	 *
	 * @param values
	 * @return values of key fields for logging
	 */
	public String emitKeys(final Object[] values) {
		if (this.keyIndexes == null) {
			return "No keys";
		}
		final StringBuilder sbf = new StringBuilder();
		for (final int idx : this.keyIndexes) {
			sbf.append(this.dbFields[idx].getName()).append(" = ").append(values[idx]).append("  ");
		}
		return sbf.toString();
	}

	private static boolean notAllowed(final IoType operation) {
		logger.error("THis record is not designed for '{}' operation", operation);
		return false;
	}

	/**
	 * @param fieldValues
	 * @param ctx
	 * @return
	 */
	boolean parseKeys(final IInputData inputObject, final Object[] fieldValues, final IServiceContext ctx) {

		if (this.tenantField != null) {
			fieldValues[this.tenantField.getIndex()] = ctx.getTenantId();
		}

		if (this.keyIndexes == null) {
			logger.error("No keys defined for this db record.");
			ctx.addMessage(Message.newError(Conventions.MessageId.INTERNAL_ERROR));
			return false;
		}

		boolean ok = true;
		for (final int idx : this.keyIndexes) {
			final DbField f = this.dbFields[idx];
			final String value = inputObject.getString(f.getName());
			if (value == null || value.isEmpty()) {
				ctx.addMessage(Message.newFieldError(f.getName(), Conventions.MessageId.VALUE_REQUIRED, ""));
				ok = false;
			}
			/*
			 * we need to parse this as a normal field, not as DbFIeld.
			 */
			if (!f.parseIntoRow(value, fieldValues, ctx, null, 0)) {
				ok = false;
			}
		}

		return ok;
	}

	/**
	 * @param fieldName
	 * @return field, or null if there is no such field
	 */
	public DbField getField(final String fieldName) {
		for (final DbField f : this.dbFields) {
			if (f.getName().equals(fieldName)) {
				return f;
			}
		}
		return null;
	}

	/**
	 * @param operation
	 * @return true if this operation is allowed
	 */
	boolean operationAllowed(final IoType operation) {
		if (operation == null) {
			return false;
		}
		return this.allowedOperations[operation.ordinal()];
	}

	/**
	 * copy object values from a source of data to a target based on the mapped
	 * indexes. that is, the source has the data for the target, but their positions
	 * may be different.
	 *
	 * @param source  row data for this record
	 * @param indexes row elements to be copied
	 * @param target  optional. If specified, it must be of the right size. else a
	 *                new one is created
	 * @return copy of the source. target if it were non-null.
	 */
	private static Object[] copyFromRow(Object[] source, int[] indexes, Object[] target) {
		Object[] row = target == null ? new Object[indexes.length] : target;

		for (int i = 0; i < indexes.length; i++) {
			row[i] = source[indexes[i]];
		}

		return row;
	}

	/**
	 * copy object values from rows of data to parameters based on indexes
	 *
	 */
	private static Object[][] copyFromRows(Object[][] rows, int[] indexes) {
		Object[][] params = new Object[indexes.length][];
		final int nbrCols = indexes.length;

		for (int rowIdx = 0; rowIdx < rows.length; rowIdx++) {
			final Object[] row = rows[rowIdx];
			final Object[] param = new Object[nbrCols];
			params[rowIdx] = param;

			for (int colIdx = 0; colIdx < indexes.length; colIdx++) {
				param[colIdx] = row[indexes[colIdx]];
			}
		}
		return params;
	}

	private void copyKeys(Object[][] rows, long[] keys) {
		for (int i = 0; i < rows.length; i++) {
			rows[i][this.generatedKeyIdx] = keys[i];
		}
	}

	/**
	 * prepares the filter details required to count rows in the filter-sql
	 * 
	 * @param filter parameters that are the input for the filter operation This may
	 *               be parsed from the input-request-json, or maybe prepared by a
	 *               server-side service implementation
	 * @param ctx
	 * @return parsedFilter, or null in case of any error. Error messages are added
	 *         to the service context
	 */
	public FilterDetails prepareCountSql(final FilterParams params, final IServiceContext ctx) {

		boolean allOk = true;
		/*
		 * create a look-up map of fields by fieldName
		 */
		final Map<String, DbField> map = new HashMap<>();
		for (final DbField field : this.dbFields) {
			map.put(field.getName(), field);
		}

		/**
		 * Build sql, starting with the SELECT clause
		 */
		StringBuilder sql = new StringBuilder("SELECT count(*) FROM ");
		sql.append(this.nameInDb);
		/*
		 * filters
		 */
		FilterCondition[] filters = params.filters;
		if (filters == null || filters.length == 0) {
			logger.warn("Filter request has no conditions. All rows will be filtered");
			filters = null;
		}

		final List<Object> values = new ArrayList<>();
		final List<ValueType> types = new ArrayList<>();

		final StringBuilder wherePart = new StringBuilder();
		/*
		 * force a condition on tenant id if required
		 */
		if (this.tenantField != null) {
			wherePart.append("(").append(this.tenantField.getColumnName()).append("=?");
			values.add(ctx.getTenantId());
			types.add(ValueType.Integer);
		}

		if (filters == null) {
			if (this.tenantField != null) {
				wherePart.append(")");
			}
		} else {
			final boolean ok = parseConditions(map, filters, ctx, values, types, wherePart);
			if (!ok) {
				allOk = false;
			}

		}

		if (wherePart.length() > 0) {
			sql.append(" WHERE ").append(wherePart.toString());
		}

		if (!allOk) {
			return null;
		}

		final String sqlText = sql.toString();
		Object[] paramValues = null;
		ValueType[] paramTypes = null;

		final int n = values.size();

		/**
		 * log filter clause with parameters
		 */
		logger.info("filter SQL is: {}", sqlText);
		if (n == 0) {
			logger.info("WHERE clause has no parameters.");
		} else {

			final StringBuilder sbf = new StringBuilder();
			for (int i = 0; i < n; i++) {
				sbf.append('\n').append(i).append("= ").append(values.get(i));
			}
			logger.info("Where parameters : {}", sbf.toString());

			paramValues = values.toArray();
			paramTypes = types.toArray(new ValueType[0]);
		}
		return new FilterDetails(sqlText, paramValues, paramTypes, null, null);

	}

	/**
	 * prepares the filter details required to filter rows from a database table
	 * 
	 * @param filter parameters that are the input for the filter operation This may
	 *               be parsed from the input-request-json, or maybe prepared by a
	 *               server-side service implementation
	 * @param ctx
	 * @return parsedFilter, or null in case of any error. Error messages are added
	 *         to the service context
	 */
	public FilterDetails prepareFilterDetails(final FilterParams params, final IServiceContext ctx) {

		/*
		 * create a look-up map of fields by fieldName
		 */
		final Map<String, DbField> map = new HashMap<>();
		for (final DbField field : this.dbFields) {
			map.put(field.getName(), field);
		}

		/*
		 * let us start parsing the input, starting with max rows
		 */
		int maxRows = params.maxRows;
		if (maxRows != 0 && maxRows > 0 && maxRows <= Conventions.Db.MAX_ROWS_TO_FILTER) {
			logger.info("Client requested a max of {} rows.", maxRows);
		} else {
			maxRows = Conventions.Db.MAX_ROWS_TO_FILTER;
			logger.info("As per configuration, a max of {} rows will be selected.", maxRows);
		}

		DbField[] outputFields = this.dbFields;
		boolean allOk = true;

		final String[] fieldNames = params.fields;
		if (fieldNames != null && fieldNames.length != 0) {
			outputFields = new DbField[fieldNames.length];
			int i = 0;
			for (String name : fieldNames) {
				final DbField f = map.get(name);
				if (f == null) {
					reportError("Field " + name
							+ " does not exist in the form/record or it is not a column in the associated table/view",
							ctx);
					allOk = false;
				} else {
					outputFields[i] = f;
					i++;
				}
			}
		}

		int nbrFields = outputFields.length;

		/**
		 * build these two arrays as the SELECT clause is assembled
		 */
		ValueType[] outputTypes = new ValueType[nbrFields];
		String[] outputNames = new String[nbrFields];

		/**
		 * Build sql, starting with the SELECT clause
		 */
		StringBuilder sql = new StringBuilder("SELECT ");
		int nbrColumns = 0;
		for (int i = 0; i < nbrFields; i++) {
			final DbField f = outputFields[i];
			if (f == null) {
				continue;
			}
			String columnName = f.getColumnName();
			if (columnName == null) {
				// it is not a column in the table
				continue;
			}
			sql.append(columnName).append(", ");
			outputTypes[nbrColumns] = f.getValueType();
			outputNames[nbrColumns] = f.getName();
			nbrColumns++;
		}

		if (nbrColumns == 0) {
			allOk = false;
			reportError("No field/column to be included in the output row ", ctx);
		} else {
			if (nbrColumns != nbrFields) {
				// we have some fields that are not columns
				outputTypes = Arrays.copyOf(outputTypes, nbrColumns);
				outputNames = Arrays.copyOf(outputNames, nbrColumns);
			}
			sql.setLength(sql.length() - 2);
			sql.append(" FROM ").append(this.nameInDb);
		}

		logger.info("SELECT is built as: {}", sql.toString());
		/*
		 * filters
		 */
		FilterCondition[] filters = params.filters;
		if (filters == null || filters.length == 0) {
			logger.warn("Filter request has no conditions. All rows will be filtered");
			filters = null;
		}

		final List<Object> values = new ArrayList<>();
		final List<ValueType> types = new ArrayList<>();

		final StringBuilder wherePart = new StringBuilder();
		/*
		 * force a condition on tenant id if required
		 */
		if (this.tenantField != null) {
			wherePart.append("(").append(this.tenantField.getColumnName()).append("=?");
			values.add(ctx.getTenantId());
			types.add(ValueType.Integer);
		}

		if (filters == null) {
			if (this.tenantField != null) {
				wherePart.append(")");
			}
		} else {
			final boolean ok = parseConditions(map, filters, ctx, values, types, wherePart);
			if (!ok) {
				allOk = false;
			}

		}

		if (wherePart.length() > 0) {
			sql.append(" WHERE ").append(wherePart.toString());
		}

		logger.info("SQL after WHERE = {}", sql.toString());

		/*
		 * sort order
		 */
		final SortBy[] sorts = params.sorts;

		if (sorts != null) {
			boolean isFirst = true;

			for (SortBy sortBy : sorts) {
				String fieldName = sortBy.field;
				final DbField field = map.get(fieldName);
				if (field == null) {
					reportError("Field " + fieldName + " does not exist in the form/record", ctx);
					allOk = false;
					continue;
				}
				final String columnName = field.getColumnName();
				if (isFirst) {
					sql.append(" ORDER BY ");
					isFirst = false;
				} else {
					sql.append(", ");
				}

				if (field.getValueType() == ValueType.Text) {
					sql.append("UPPER(").append(columnName).append(")");
				} else {
					sql.append(columnName);
				}

				boolean isDescending = sortBy.descending;
				if (isDescending) {
					sql.append(" DESC ");
				}
			}
		}

		if (!allOk) {
			return null;
		}

		sql.append(" FETCH FIRST " + maxRows + " ROWS ONLY");

		final String sqlText = sql.toString();
		Object[] paramValues = null;
		ValueType[] paramTypes = null;

		final int n = values.size();

		/**
		 * log filter clause with parameters
		 */
		logger.info("filter SQL is: {}", sqlText);
		if (n == 0) {
			logger.info("WHERE clause has no parameters.");
		} else {

			final StringBuilder sbf = new StringBuilder();
			for (int i = 0; i < n; i++) {
				sbf.append('\n').append(i).append("= ").append(values.get(i));
			}
			logger.info("Where parameters : {}", sbf.toString());

			paramValues = values.toArray();
			paramTypes = types.toArray(new ValueType[0]);
		}
		return new FilterDetails(sqlText, paramValues, paramTypes, outputNames, outputTypes);

	}

	private static void reportError(final String error, final IServiceContext ctx) {
		logger.error(error);
		ctx.addMessage(Message.newError(Conventions.MessageId.INVALID_DATA));
	}

	private static boolean parseConditions(final Map<String, DbField> fields, final FilterCondition[] filters,
			final IServiceContext ctx, final List<Object> values, final List<ValueType> types,
			final StringBuilder sql) {

		/*
		 * fairly long inside the loop for each field. But it is just serial code. Hence
		 * left it that way
		 * 
		 * For safety, we put a pair of braces around each condition so that the AND
		 * operation is safe
		 */

		int i = -1;
		boolean allOk = true;
		for (FilterCondition f : filters) {
			i++;

			if (sql.length() == 0) {
				sql.append("(");
			} else {
				sql.append(") AND (");
			}
			final String fieldName = f.field;
			final DbField field = fields.get(fieldName);
			if (field == null) {
				/**
				 * all fields are anyways optional. What if we just ignore this as "not ours"?
				 */
//				reportError("Filter field " + fieldName + " does not exist in the form/record", ctx);
//				allOk = false;
				continue;
			}

			final String operatorText = f.comparator;
			if (operatorText == null || operatorText.isEmpty()) {
				reportError("filter operator is missing at index " + i, ctx);
				allOk = false;
			}

			final FilterOperator opertor = FilterOperator.parse(operatorText);
			if (opertor == null) {
				reportError(operatorText + " is not a valid filter condition", ctx);
				allOk = false;
			}

			String value1 = f.value;
			if (value1 == null) {
				reportError("value is missing for a filter condition at index " + i, ctx);
				allOk = false;
			}

			String value2 = null;
			if (opertor == FilterOperator.Between) {
				value2 = f.toValue;
				if (value2 == null) {
					reportError("toValue is missing for a filter condition at index " + i, ctx);
					allOk = false;
				}
			}

			/*
			 * operator == null is unnecessary, but added to avoid null-check-errors
			 */
			if (!allOk || opertor == null || value1 == null) {
				// skip even checking semantic errors because we start building the sql along
				// with further checks..
				continue;
			}

			String column = null;
			ValueType vt = null;

			column = field.getColumnName();
			vt = field.getValueType();
			String column1 = parseField(value1, fields, vt, ctx);
			if (column1 != null && column1.isEmpty()) {
				allOk = false;
				continue;
			}

			String column2 = null;
			if (value2 != null) {
				column2 = parseField(value2, fields, vt, ctx);
				if (column2 != null && column2.isEmpty()) {
					allOk = false;
					continue;
				}
			}

			/*
			 * we do all our string comparisons as case-insensitive.
			 * 
			 * This is because, in the business context 'case' of a letter has no meaning,
			 * but used for formatting.
			 * 
			 * e.g. John is john, and is also JOHN.
			 * 
			 * TODO: Of course, this argument is not valid for a field that is a computer
			 * generated unique text code. We will offer some feature to handle this
			 * exception
			 */
			if (vt == ValueType.Text) {
				column = toUpper(column);
				column1 = toUpper(column1);
				column2 = toUpper(column2);
				value1 = value1.toUpperCase();
				if (value2 != null) {
					value2 = value2.toUpperCase();
				}
			}

			sql.append(column);

			/*
			 * complex ones first.. we have to append ? to sql, and add type and value to
			 * the lists for each case
			 */
			if ((opertor == FilterOperator.Contains || opertor == FilterOperator.StartsWith)) {
				if (vt != ValueType.Text) {
					reportError("Condition " + opertor + " is not valid for field " + fieldName
							+ " which is of value type " + vt, ctx);
					allOk = false;
					continue;
				}
				if (column1 != null) {
					reportError(
							"Operator " + opertor.name() + " can not be used with a field as the second operand." + vt,
							ctx);
					allOk = false;
					continue;
				}

				sql.append(LIKE);
				value1 = escapeLike(value1) + WILD_CARD;
				if (opertor == FilterOperator.Contains) {
					value1 = WILD_CARD + value1;
				}
				values.add(value1);
				types.add(vt);
				continue;
			}

			if (opertor == FilterOperator.In) {
				if (column1 != null) {
					reportError(
							"Operator " + opertor.name() + " can not be used with a field as the second operand." + vt,
							ctx);
					allOk = false;
					continue;
				}
				sql.append(IN);
				boolean firstOne = true;
				boolean ok = true;
				for (final String part : value1.split(",")) {
					Object obj = vt.parse(part.trim());
					if (obj == null) {
						reportError(value1 + " is not a valid value for value type " + vt + " for field " + fieldName,
								ctx);
						ok = false;
						break;
					}
					if (firstOne) {
						sql.append(QN);
						firstOne = false;
					} else {
						sql.append(",?");
					}
					values.add(obj);
					types.add(vt);
				}
				if (ok) {
					sql.append(')');
				} else {
					allOk = false;
				}
				continue;
			}

			Object obj1 = null;
			if (column1 == null) {
				obj1 = vt.parse(value1);
				if (obj1 == null) {
					reportError(value1 + " is not a valid value for value type " + vt + " for field " + fieldName, ctx);
					allOk = false;
					continue;
				}
			}

			if (opertor == FilterOperator.Between) {
				sql.append(BETWEEN);
				if (column1 == null) {
					sql.append(QN);
					values.add(obj1);
					types.add(vt);
				} else {
					sql.append(column1);
				}
				sql.append(" AND ");
				if (column2 == null) {
					Object obj2 = vt.parse(value2);
					if (obj2 == null) {
						reportError(
								value2 + " is not a valid value for value type " + vt + " for the field " + fieldName,
								ctx);
						allOk = false;
						continue;
					}
					sql.append(QN);
					values.add(obj2);
					types.add(vt);
				} else {
					sql.append(column2);
				}

				continue;
			}

			sql.append(' ').append(operatorText).append(" ");
			if (column1 == null) {
				sql.append(QN);
				values.add(obj1);
				types.add(vt);
			} else {
				sql.append(column1);
			}
			sql.append(' ');
		}
		sql.append(")");
		return allOk;
	}

	/**
	 * 
	 * @param name can be null, in which case null is returned
	 * @return SQL text syntax to get the upper-case value of this column
	 */
	private static String toUpper(String name) {
		if (name == null) {
			return null;
		}
		return "UPPER(" + name + ")";
	}

	/**
	 * returns null string if this is not of the form ${name}.
	 * 
	 * column name if the field valid.
	 * 
	 * Empty string if the field invalid. Appropriate error message is pushed to the
	 * ctx
	 * 
	 */
	private static String parseField(String value, Map<String, DbField> fields, ValueType vt, IServiceContext ctx) {
		int lastPosn = value.length() - 1;
		if (value.startsWith("${") == false || value.charAt(lastPosn) != '}') {
			return null;
		}
		String msg = null;
		String name = value.substring(2, lastPosn);
		DbField field = fields.get(name);
		if (field == null) {
			msg = "This field does not exist";
		} else {

			final ValueType valueType = field.getValueType();
			if (valueType == vt) {
				return field.getColumnName();
			}
			msg = "This field is of value type " + valueType.name() + "but a value of type " + vt.name()
					+ "is expected";

		}

		msg = "Filter condition uses '" + value + "' indicating that the field '" + name
				+ "' to be used for comparison. " + msg;
		reportError(msg, ctx);
		return "";

	}

	/**
	 * NOTE: Does not work for MS-ACCESS. but we are fine with that!!!
	 *
	 * @param string
	 * @return string that is escaped for a LIKE sql operation.
	 */
	private static String escapeLike(final String string) {
		return string.replaceAll(WILD_CARD, ESCAPED_WILD_CARD).replaceAll(WILD_CHAR, ESCAPED_WILD_CHAR);
	}

	/**
	 * set keys from an array of objects
	 * 
	 * @param keyValues
	 * @param fieldValues
	 * @param ctx
	 * @return true if all ok. False otherwise. error message would have been added
	 *         to the ctx
	 */
	boolean setKeys(Object[] keyValues, Object[] fieldValues, IServiceContext ctx) {
		if (this.keyIndexes == null) {
			logger.error("Table '{}' has no keys, but a setKeys() being attempted. Operation not done", this.nameInDb);
			ctx.addMessage(Message.newError(Conventions.MessageId.INVALID_DATA));
			return false;
		}

		if (this.keyIndexes.length != keyValues.length) {
			logger.error("Table '{}' has {} keys, but a setKeys() being attempted with {} keys. Operation not done",
					this.nameInDb, this.keyIndexes.length, keyValues.length);
			ctx.addMessage(Message.newError(Conventions.MessageId.INVALID_DATA));
			return false;
		}

		for (int i = 0; i < keyValues.length; i++) {
			Field field = this.dbFields[this.keyIndexes[i]];
			final String value = keyValues[i].toString();
			if (!field.parseIntoRow(value, fieldValues, ctx, "", 0)) {
				return false;
			}
		}

		return true;
	}

}
