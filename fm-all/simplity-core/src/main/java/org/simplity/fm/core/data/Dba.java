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
import java.util.List;

import org.simplity.fm.core.Message;
import org.simplity.fm.core.db.IReadWriteHandle;
import org.simplity.fm.core.db.IReadonlyHandle;
import org.simplity.fm.core.db.IRowProcessor;
import org.simplity.fm.core.service.IInputData;
import org.simplity.fm.core.service.IServiceContext;
import org.simplity.fm.core.valueschema.ValueType;
import org.simplity.fm.core.valueschema.ValueTypeUtil;
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

	protected static final Logger logger = LoggerFactory.getLogger(Dba.class);
	/**
	 * table/view name in the database
	 */
	private final String nameInDb;

	/**
	 * operations like get etc.. are valid? array index corresponds to integer
	 * value of the enum IoType
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
	/**
	 * FINAL. db column name that is generated as internal key. null if this is
	 * not relevant
	 */
	private String generatedColumnName;

	/**
	 * FINAL. index to the generatedKey
	 */
	private int generatedKeyIdx = -1;

	/**
	 * FINAL. if this APP is designed for multi-tenant deployment, and this
	 * table has data across tenants..
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
	public Dba(final Field[] allFields, final String nameInDb,
			final boolean[] opers, final String selectClause,
			final int[] selectIndexes, final String insertClause,
			final int[] insertIndexes, final String updateClause,
			final int[] updateIndexes, final String deleteClause,
			final String whereClause, final int[] whereIndexes) {

		this.dbFields = new DbField[allFields.length];
		this.prepareFields(allFields);

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

	private static ValueType[] typesOfFields(Field[] fields, int[] indexes) {
		if (indexes == null) {
			return null;
		}
		ValueType[] types = new ValueType[fields.length];
		for (int i = 0; i < fields.length; i++) {
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
			case TenantKey :
				this.tenantField = fld;
				continue;

			case GeneratedPrimaryKey :
				this.generatedColumnName = fld.getColumnName();
				this.generatedKeyIdx = fld.getIndex();
				keys[nbrKeys] = fld.getIndex();
				nbrKeys++;
				continue;

			case PrimaryKey :
				keys[nbrKeys] = fld.getIndex();
				nbrKeys++;
				continue;

			default :
				continue;
			}
		}
		if (nbrKeys > 0) {
			this.keyIndexes = Arrays.copyOf(keys, nbrKeys);
		}

	}

	/**
	 *
	 * @return index of the generated key, or -1 if this record has no generated
	 *         key
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
	 * return the select clause (like select a,b,...) without the where clause
	 * for this record
	 *
	 * @return string that is a valid select-part of a sql that can be used with
	 *         a were clause to filter rows from the underlying dbtable.view
	 */
	public String getSelectClause() {
		return this.selectClause;
	}

	/**
	 * fetch data for this form from a db based on the primary key of this
	 * record
	 *
	 * @param handle
	 * @param row
	 *            array of objects that can hold values for this record. this
	 *            acts as both input and output for field values
	 *
	 * @return true if it is read. false if no data found for this record (key
	 *         not found...)
	 * @throws SQLException
	 */
	boolean read(final IReadonlyHandle handle, final Object[] row)
			throws SQLException {
		if (this.keyIndexes == null) {
			return notAllowed(IoType.Get);
		}

		Object[] inp = copyFromRow(row, this.whereIndexes);

		Object[] result = handle.read(
				this.selectClause + ' ' + this.whereClause, inp,
				this.whereTypes, this.selectTypes);

		return this.copyToRow(result, row);
	}

	/**
	 * select multiple rows from the db based on the filtering criterion
	 *
	 * @param handle
	 *
	 * @param whereClauseStartingWithWhere
	 *            e.g. "WHERE a=? and b=?". null if all rows are to be read.
	 *            Best practice is to use parameters rather than dynamic sql.
	 *            That is you should use a=? rather than a = 32
	 * @param values
	 *            null or empty if where-clause is null or has no parameters.
	 *            every element MUST be non-null and must be one of the standard
	 *            objects we use: String, Long, Double, Boolean, LocalDate,
	 *            Instant
	 * @return non-null, possibly empty array of rows
	 * @throws SQLException
	 */
	List<Object[]> filter(final IReadonlyHandle handle,
			final String whereClauseStartingWithWhere, final Object[] values)
			throws SQLException {

		String sql = this.selectClause;
		if (whereClauseStartingWithWhere != null) {
			sql += ' ' + whereClauseStartingWithWhere;
		}

		final int nbrFields = this.dbFields.length;
		final List<Object[]> rows = new ArrayList<>();

		handle.readMany(sql, values, ValueTypeUtil.valueTypesOf(values),
				this.selectTypes, outputRow -> {
					final Object[] row = new Object[nbrFields];
					Dba.this.copyToRow(outputRow, row);
					rows.add(row);
					return true;

				});

		return rows;
	}

	/**
	 * process each row selected based on the where clause
	 *
	 * @param handle
	 * @param where
	 *            should start with 'where' e.g. 'where a=? and b=?...". null to
	 *            select all rows from this table
	 * @param inputValues
	 *            values, in the right order, for the parameters in the where
	 *            clause. null if were is null.
	 * @param rowProcessor
	 *            class/lambda that is called for each output row from the query
	 * @throws SQLException
	 */
	void forEach(final IReadonlyHandle handle, final String where,
			final Object[] inputValues, final IRowProcessor rowProcessor)
			throws SQLException {

		String sql = this.selectClause;
		if (where != null) {
			sql += this.selectClause + ' ' + where;
		}

		handle.readMany(sql, inputValues,
				ValueTypeUtil.valueTypesOf(inputValues), this.selectTypes,
				rowProcessor);

		return;
	}

	/**
	 * insert/create this record into the db.
	 *
	 * @param handle
	 * @param rowToInsert
	 *            data for this record that has values for all the fields in the
	 *            right order
	 *
	 * @return true if it is created. false in case it failed because of an an
	 *         existing form with the same id/key
	 * @throws SQLException
	 */
	boolean insert(final IReadWriteHandle handle, final Object[] rowToInsert)
			throws SQLException {
		if (this.keyIndexes == null) {
			return notAllowed(IoType.Create);
		}

		final Object[] params = copyFromRow(rowToInsert, this.insertIndexes);
		final ValueType[] paramTypes = ValueTypeUtil.valueTypesOf(params);
		if (this.generatedColumnName == null) {
			return handle.write(this.insertClause, params, paramTypes) > 0;

		}

		final long[] generatedKeys = new long[1];
		int n = handle.insertWithKeyGeneration(this.insertClause, params,
				paramTypes, generatedColumnName, generatedKeys);
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
	boolean update(final IReadWriteHandle handle, final Object[] rowToUpdate)
			throws SQLException {
		if (this.keyIndexes == null) {
			return notAllowed(IoType.Update);
		}

		final Object[] params = copyFromRow(rowToUpdate, this.updateIndexes);
		final ValueType[] paramTypes = ValueTypeUtil.valueTypesOf(params);

		return handle.write(this.updateClause, params, paramTypes) > 0;

	}

	/**
	 * remove this form data from the db
	 *
	 * @param handle
	 * @param rowToDelete
	 *            row-data for this record that has values for the fields that
	 *            are required to identify the row to be deleted
	 *
	 * @return true if it is indeed deleted. false otherwise
	 * @throws SQLException
	 */
	boolean delete(final IReadWriteHandle handle, final Object[] rowToDelete)
			throws SQLException {
		if (this.keyIndexes == null) {
			return notAllowed(IoType.Delete);
		}

		final Object[] params = copyFromRow(rowToDelete, this.keyIndexes);
		final ValueType[] paramTypes = ValueTypeUtil.valueTypesOf(params);

		return handle.write(this.deleteClause, params, paramTypes) > 0;

	}

	/**
	 * save a data-row rows into the db. The record must have a generated key as
	 * its primary key fpr this operation to be meaningful. If the key exists in
	 * the data-row, then it is updated, else it is inserted
	 *
	 * @param handle
	 *
	 * @param fieldValues
	 *            data to be saved
	 * @return true if the save succeeded, false otherwise. This value can be
	 *         used to commit/roll-back the transaction
	 * @throws SQLException
	 */
	boolean save(final IReadWriteHandle handle, final Object[] fieldValues)
			throws SQLException {
		if (this.keyIndexes == null) {
			return notAllowed(IoType.Update);
		}

		boolean ok = this.update(handle, fieldValues);
		if (!ok) {
			ok = this.insert(handle, fieldValues);;
		}

		return ok;
	}

	/**
	 * save all rows into the db. Each row is inspected to check for the
	 * generated primary key. If the key exists, that row is updated, else it is
	 * inserted. .
	 *
	 * @param handle
	 *
	 * @param rows
	 *            data to be saved
	 * @return true if all ok. false in case one or more rows failed to save.
	 *         This can be used to commit/roll-back the transaction
	 * @throws SQLException
	 */
	boolean saveAll(final IReadWriteHandle handle, final Object[][] rows)
			throws SQLException {
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
	 * insert all rows. NOTE: caller must consider rolling-back if false is
	 * returned
	 *
	 * @param handle
	 *
	 * @param rows
	 *            data to be saved
	 * @return true if every one row was inserted. false if any one row failed
	 *         to insert.
	 * @throws SQLException
	 */
	boolean insertAll(final IReadWriteHandle handle, final Object[][] rows)
			throws SQLException {
		if (this.keyIndexes == null) {
			return notAllowed(IoType.Create);
		}

		int nbrRows = rows.length;
		final Object[][] paramValues = copyFromRows(rows, this.insertIndexes);
		int nbrInserted = 0;

		if (this.generatedColumnName == null) {
			nbrInserted = handle.writeMany(this.insertClause, paramValues);
		} else {
			final long[] generatedKeys = new long[nbrRows];
			nbrInserted = handle.writeManyWithKeyGeneration(this.insertClause,
					paramValues, this.generatedColumnName, generatedKeys);
			if (nbrInserted > 0) {
				this.copyKeys(rows, generatedKeys);
			}
		}
		return nbrInserted == nbrRows;
	}

	/**
	 * update all rows. NOTE: caller must consider rolling-back if false is
	 * returned
	 *
	 * @param handle
	 *
	 * @param rows
	 *            data to be saved
	 * @return true if every one row was successfully updated. false if any one
	 *         row failed to update
	 * @throws SQLException
	 */
	boolean updateAll(final IReadWriteHandle handle, final Object[][] rows)
			throws SQLException {
		if (this.keyIndexes == null) {
			return notAllowed(IoType.Update);
		}

		int nbrRows = rows.length;
		final Object[][] paramValues = copyFromRows(rows, this.insertIndexes);
		int n = handle.writeMany(this.updateClause, paramValues);
		return n == nbrRows;
	}

	/**
	 * validate the data row for db-operation. This is to be invoked after the
	 * row is parsed/validated as a valid record (non-db)
	 *
	 * @param data
	 * @param rowNbr
	 * @param tableName
	 * @param ctx
	 * @param forInsert
	 * @return true if all ok. false if any error message is added to the
	 *         context
	 */
	public boolean validate(final Object[] data, final boolean forInsert,
			final IServiceContext ctx, final String tableName,
			final int rowNbr) {
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
			sbf.append(this.dbFields[idx].getName()).append(" = ")
					.append(values[idx]).append("  ");
		}
		return sbf.toString();
	}

	private static boolean notAllowed(final IoType operation) {
		logger.error("THis record is not designed for '{}' operation",
				operation);
		return false;
	}

	/**
	 * @param fieldValues
	 * @param ctx
	 * @return
	 */
	boolean parseKeys(final IInputData inputObject, final Object[] fieldValues,
			final IServiceContext ctx) {

		if (this.tenantField != null) {
			fieldValues[this.tenantField.getIndex()] = ctx.getTenantId();
		}

		if (this.keyIndexes == null) {
			logger.error("No keys defined for this db record.");
			ctx.addMessage(Message.newError(Message.MSG_INTERNAL_ERROR));
			return false;
		}

		boolean ok = true;
		for (final int idx : this.keyIndexes) {
			final DbField f = this.dbFields[idx];
			final String value = inputObject.getString(f.getName());
			if (value == null || value.isEmpty()) {
				ctx.addMessage(Message.newFieldError(f.getName(),
						Message.FIELD_REQUIRED, ""));
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
	 *
	 * @param json
	 * @param ctx
	 * @return parsedFilter, or null in case of any error
	 */
	public ParsedFilter parseFilter(final IInputData json,
			final IServiceContext ctx) {
		return ParsedFilter.parse(json, this.dbFields, this.tenantField, ctx);
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
	 * copy object values from a row of data to parameters based on indexes
	 *
	 * @param row
	 *            row data for this record
	 * @param indexes
	 *            row elements to be copied
	 * @return inputParams from a select statement
	 */
	private static Object[] copyFromRow(Object[] row, int[] indexes) {
		Object[] params = new Object[indexes.length];

		for (int i = 0; i < indexes.length; i++) {
			params[i] = row[indexes[i]];
		}

		return params;
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

	/**
	 * copy result of a select into the values for this record
	 *
	 * @param result
	 * @param row
	 * @return true if values are copied. false if result is null
	 */
	private boolean copyToRow(Object[] result, Object[] row) {
		if (result == null) {
			return false;
		}

		for (int i = 0; i < result.length; i++) {
			row[this.selectIndexes[i]] = result[i];
		}

		return true;
	}

	private void copyKeys(Object[][] rows, long[] keys) {
		for (int i = 0; i < rows.length; i++) {
			rows[i][this.generatedKeyIdx] = keys[i];
		}
	}

}
