/*
 * Copyright (c) 2019 simplity.org
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

package org.simplity.fm.gen;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.simplity.fm.core.Conventions;
import org.simplity.fm.core.data.DbField;
import org.simplity.fm.core.data.DbRecord;
import org.simplity.fm.core.data.DbTable;
import org.simplity.fm.core.data.Dba;
import org.simplity.fm.core.data.FieldType;
import org.simplity.fm.core.data.IoType;
import org.simplity.fm.core.data.RecordMetaData;
import org.simplity.fm.core.service.IInputData;
import org.simplity.fm.core.service.IServiceContext;
import org.simplity.fm.core.validn.DependentListValidation;
import org.simplity.fm.core.validn.ExclusiveValidation;
import org.simplity.fm.core.validn.FromToValidation;
import org.simplity.fm.core.validn.IValidation;
import org.simplity.fm.core.validn.InclusiveValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * represents the contents of a spread sheet for a form
 *
 * @author simplity.org
 *
 */
class Record {
	/*
	 * this logger is used by all related classes of form to give the programmer the
	 * right stream of logs to look for any issue in the workbook
	 */
	private static final Logger logger = LoggerFactory.getLogger(Record.class);

	private static final String C = ", ";
	private static final String P = "\n\tprivate static final ";

	/*
	 * fields that are read directly from json
	 */
	String name;
	String nameInDb;
	boolean useTimestampCheck;
	boolean isVisibleToClient;
	// String customValidation;
	String[] operations;
	Field[] fields;
	FromToPair[] fromToPairs;
	ExclusivePair[] exclusivePairs;
	InclusivePair[] inclusivePairs;

	/*
	 * for sub-record
	 */
	String mainRecordName;
	String[] fieldNames;

	/*
	 * derived fields required for generating java/ts
	 */
	/*
	 * reason we have it as an array rather than a MAP is that the sequence, though
	 * not recommended, could be hard-coded by some coders
	 */
	Map<String, Field> fieldsMap;
	Field[] fieldsWithList;
	Field[] keyFields;

	Field tenantField;
	Field timestampField;
	Field generatedKeyField;

	private String className;
	/*
	 * got errors?
	 */
	boolean gotErrors;

	Set<IoType> allowedIos;

	/**
	 * 
	 * @return name of the main/parent record from which the fields for this record
	 *         are to be copied to. null if this is not a sub-record
	 */
	public String getMainRecordName() {
		return this.mainRecordName;
	}

	public void initExtendedRecord(Map<String, ValueSchema> schemas, Record mainRecord) {
		/*
		 * nameInDb, if specified, must be the same as the main-form
		 */
		if (this.nameInDb != null) {
			if (this.nameInDb.equals(mainRecord.nameInDb) == false) {
				logger.error(
						"nameInDb is to be specified if this sub-record is to be used for db-operations. However, it can not be different from the one specified in the main record. Sub record {} uses {} as nameInDb while its main record {} uses {} as nameInDb.",
						this.name, this.nameInDb, this.mainRecordName, mainRecord.nameInDb);
				this.gotErrors = true;
			}
		}

		int nbrNewFields = this.fields == null ? 0 : this.fields.length;
		int totalFields = this.fieldNames.length + nbrNewFields;

		/*
		 * copy fields from the main record
		 */
		Field[] flds = new Field[totalFields];
		for (int i = 0; i < this.fieldNames.length; i++) {
			String fn = this.fieldNames[i];
			Field field = mainRecord.fieldsMap.get(fn);
			if (field == null) {
				logger.error("Extended record {} specifies a field {}  but it is not found in the main record {}",
						this.name, fn, this.mainRecordName);
				this.gotErrors = true;
			} else {
				flds[i] = field.makeACopy(i);
			}
		}

		/*
		 * append any fields specified in this record
		 */
		if (nbrNewFields > 0) {
			int j = this.fieldNames.length;
			for (int i = 0; i < nbrNewFields; i++, j++) {
				flds[j] = this.fields[i];
			}
		}
		this.fields = flds;
		this.init(schemas);
	}

	public void init(Map<String, ValueSchema> schemas) {
		this.className = Util.toClassName(this.name) + Conventions.App.RECORD_CLASS_SUFIX;
		/*
		 * we want to check for duplicate definition of standard fields
		 */
		Field modifiedAt = null;
		Field modifiedBy = null;
		Field createdBy = null;
		Field createdAt = null;

		final List<Field> list = new ArrayList<>();
		final List<Field> keyList = new ArrayList<>();
		this.fieldsMap = new HashMap<>();
		for (int idx = 0; idx < this.fields.length; idx++) {
			final Field field = this.fields[idx];
			if (field == null) {
				continue;
			}

			field.init(idx, schemas);
			Field existing = this.fieldsMap.put(field.name, field);
			if (existing != null) {
				logger.error("Field {} is a duplicate in record {}", field.name, this.name);
				this.gotErrors = true;
				;
			}

			if (field.listName != null) {
				list.add(field);
			}

			FieldType ft = field.fieldTypeEnum;
			if (ft == null) {
				if (field.nameInDb == null) {
					logger.warn("{} is not linked to a db-column. No I/O happens on this field.", field.name);
					continue;
				}
				logger.error("{} is linked to a db-column {} but does not specify a fieldType.", field.name,
						field.nameInDb);
				ft = FieldType.OptionalData;
				this.gotErrors = true;
				;
			}

			switch (ft) {
			case PrimaryKey:
				if (this.generatedKeyField != null) {
					logger.error("{} is defined as a generated primary key, but {} is also defined as a primary key.",
							keyList.get(0).name, field.name);
					this.gotErrors = true;
					;
				} else {
					keyList.add(field);
				}
				break;

			case GeneratedPrimaryKey:
				if (this.generatedKeyField != null) {
					logger.error("Only one generated key please. Found {} as well as {} as generated primary keys.",
							field.name, keyList.get(0).name);
					this.gotErrors = true;
					;
				} else {
					if (keyList.size() > 0) {
						logger.error(
								"Field {} is marked as a generated primary key. But {} is also marked as a primary key field.",
								field.name, keyList.get(0).name);
						this.gotErrors = true;
						;
						keyList.clear();
					}
					keyList.add(field);
					this.generatedKeyField = field;
				}
				break;

			case TenantKey:
				if (field.valueSchema.equals("tenantKey") == false) {
					logger.error(
							"Tenant key field MUST use valueSchema of tenantKey. Field {} which is marked as tenant key is of data type {}",
							field.name, field.valueSchema);
					this.gotErrors = true;
					;
				}
				if (this.tenantField == null) {
					this.tenantField = field;
				} else {
					logger.error("Both {} and {} are marked as tenantKey. Tenant key has to be unique.", field.name,
							this.tenantField.name);
					this.gotErrors = true;
					;
				}
				break;

			case CreatedAt:
				if (createdAt == null) {
					createdAt = field;
				} else {
					logger.error("Only one field to be used as createdAt but {} and {} are marked", field.name,
							createdAt.name);
					this.gotErrors = true;
					;
				}
				break;

			case CreatedBy:
				if (createdBy == null) {
					createdBy = field;
				} else {
					logger.error("Only one field to be used as createdBy but {} and {} are marked", field.name,
							createdBy.name);
					this.gotErrors = true;
					;
				}
				break;

			case ModifiedAt:
				if (modifiedAt == null) {
					modifiedAt = field;
					if (this.useTimestampCheck) {
						this.timestampField = field;
					}
				} else {
					logger.error("{} and {} are both defined as lastModifiedAt!!", field.name,
							this.timestampField.name);
					this.gotErrors = true;
					;
				}
				break;

			case ModifiedBy:
				if (modifiedBy == null) {
					modifiedBy = field;
				} else {
					logger.error("Only one field to be used as modifiedBy but {} and {} are marked", field.name,
							modifiedBy.name);
					this.gotErrors = true;
					;
				}
				break;

			default:
				break;
			}
		}

		if (list.size() > 0) {
			this.fieldsWithList = list.toArray(new Field[0]);
		}

		if (keyList.size() > 0) {
			this.keyFields = keyList.toArray(new Field[0]);
		}

		if (this.useTimestampCheck && this.timestampField == null) {
			logger.error(
					"Table is designed to use time-stamp for concurrency, but no field with columnType=modifiedAt");
			this.useTimestampCheck = false;
			this.gotErrors = true;
		}

		if (this.operations != null && this.operations.length != 0) {
			if (this.nameInDb != null) {
				this.checkDbOperations();
			} else {
				logger.error(
						"One or more operations are specified, but nameInDb is not specified. db-operations can be performed only if the nameInDb is specified");
				this.gotErrors = true;
			}
		} else if (this.nameInDb != null) {
			logger.error(
					"nameInDb is specified as {} but no operations are specified. You must specify the operations that can be performed using this record.");
			this.gotErrors = true;
		}
	}

	private void checkDbOperations() {

		this.allowedIos = new HashSet<>();
		for (String s : this.operations) {
			if (s == null) {
				continue;
			}
			// we want to be case insensitive..
			s = s.toLowerCase();
			if (s.equals("save")) {
				this.allowedIos.add(IoType.Create);
				this.allowedIos.add(IoType.Update);
			} else {
				s = s.substring(0, 1).toUpperCase() + s.substring(1);
				try {
					IoType typ = IoType.valueOf(s);
					this.allowedIos.add(typ);
				} catch (IllegalArgumentException e) {
					logger.error("{} is not a valid db operation. Please correct operations array.", s);
					this.gotErrors = true;
					return;
				}
			}
		}

		if (this.keyFields == null) {
			if (this.allowedIos.contains(IoType.Get) || this.allowedIos.contains(IoType.Delete)
					|| this.allowedIos.contains(IoType.Update)) {
				logger.error("Key field/s are required for read/get, crate/insert or delete operations");
				this.gotErrors = true;
			}
		}
	}

	boolean generateJava(final String folderName, final String javaPackage) {

		if (this.gotErrors) {
			logger.error("Record {} has errors. Java code not generated", this.name);
			return false;
		}

		final StringBuilder sbf = new StringBuilder();
		/*
		 * our package name is rootPackage + any prefix/qualifier in our name
		 *
		 * e.g. if name a.b.record1 then prefix is a.b and className is Record1
		 */
		String pck = javaPackage + ".rec";
		final String qual = Util.getClassQualifier(this.name);
		if (qual != null) {
			pck += '.' + qual;
		}
		sbf.append("package ").append(pck).append(";\n");

		final boolean isDb = this.nameInDb != null && this.nameInDb.isEmpty() == false;
		/*
		 * imports
		 */
		Util.emitImport(sbf, LocalDate.class);
		Util.emitImport(sbf, Instant.class);
		Util.emitImport(sbf, IInputData.class);
		Util.emitImport(sbf, org.simplity.fm.core.data.Field.class);
		Util.emitImport(sbf, RecordMetaData.class);
		if (isDb) {
			Util.emitImport(sbf, Dba.class);
			Util.emitImport(sbf, DbField.class);
			Util.emitImport(sbf, DbRecord.class);
			Util.emitImport(sbf, FieldType.class);
		} else {
			Util.emitImport(sbf, org.simplity.fm.core.data.Record.class);
		}
		Util.emitImport(sbf, IValidation.class);
		Util.emitImport(sbf, IServiceContext.class);
		Util.emitImport(sbf, List.class);

		/*
		 * validation imports on need basis
		 */
		if (this.fromToPairs != null) {
			Util.emitImport(sbf, FromToValidation.class);
		}
		if (this.exclusivePairs != null) {
			Util.emitImport(sbf, ExclusiveValidation.class);
		}
		if (this.inclusivePairs != null) {
			Util.emitImport(sbf, InclusiveValidation.class);
		}
		Util.emitImport(sbf, DependentListValidation.class);
		/*
		 * data types are directly referred to the static declarations
		 */
		sbf.append("\nimport ").append(javaPackage).append('.')
				.append(Conventions.App.GENERATED_VALUE_SCHEMAS_CLASS_NAME).append(';');
		/*
		 * class definition
		 */

		sbf.append("\n\n/**\n * class that represents structure of ").append(this.name);
		sbf.append("\n */ ");
		sbf.append("\npublic class ").append(this.className).append(" extends ");
		if (isDb) {
			sbf.append("Db");
		}
		sbf.append("Record {");

		this.emitJavaFields(sbf, isDb);
		if (isDb) {
			this.emitValidOps(sbf);
		}
		this.emitJavaValidations(sbf);

		sbf.append("\n\n\tprivate static final RecordMetaData META = new RecordMetaData(\"");
		sbf.append(this.name).append("\", FIELDS, VALIDS);");

		if (isDb) {
			this.emitDbSpecific(sbf);
		} else {
			emitNonDbSpecific(sbf);
		}

		/*
		 * newInstane()
		 */
		sbf.append("\n\n\t@Override\n\tpublic ").append(this.className).append(" newInstance(final Object[] values) {");
		sbf.append("\n\t\treturn new ").append(this.className).append("(values);\n\t}");

		/*
		 * parseTable() override for better type-safety
		 */
		sbf.append("\n\n\t@Override\n\t@SuppressWarnings(\"unchecked\")\n\tpublic List<").append(this.className);
		sbf.append(
				"> parseTable(final IInputData inputData, String memberName, final boolean forInsert, final IServiceContext ctx) {");
		sbf.append("\n\t\treturn (List<").append(this.className)
				.append(">) super.parseTable(inputData, memberName, forInsert, ctx);\n\t}");

		/*
		 * getters and setters
		 */
		Util.emitJavaGettersAndSetters(this.fields, sbf);
		sbf.append("\n}\n");

		Util.writeOut(folderName + this.className + ".java", sbf.toString());
		return true;
	}

	private void emitNonDbSpecific(final StringBuilder sbf) {
		/*
		 * constructor
		 */
		sbf.append("\n\n\t/**  default constructor */");
		sbf.append("\n\tpublic ").append(this.className).append("() {\n\t\tsuper(META, null);\n\t}");

		sbf.append("\n\n\t/**\n\t *@param values initial values\n\t */");
		sbf.append("\n\tpublic ").append(this.className).append("(Object[] values) {\n\t\tsuper(META, values);\n\t}");
	}

	private void emitDbSpecific(final StringBuilder sbf) {
		sbf.append("\n\t/* DB related */");

		StringBuilder whereClause = new StringBuilder();
		StringBuilder indexes = new StringBuilder();

		if (this.keyFields == null) {
			sbf.append(P).append("String WHERE = null;");
			sbf.append(P).append("int[] WHERE_IDX = null;");
		} else {
			this.makeWhere(whereClause, indexes);
			sbf.append(P).append("String WHERE = \"").append(whereClause.toString()).append("\";");
			sbf.append(P).append("int[] WHERE_IDX = {").append(indexes.toString()).append("};");
		}

		this.emitSelect(sbf);
		this.emitInsert(sbf);
		this.emitUpdate(sbf, whereClause.toString(), indexes.toString());

		sbf.append(P).append("String DELETE = ");
		if (this.allowedIos.contains(IoType.Delete)) {
			sbf.append("\"DELETE FROM ").append(this.nameInDb).append("\";");
		} else {
			sbf.append("null;");
		}

		sbf.append("\n\n\tprivate static final Dba DBA = new Dba(FIELDS, ").append(Util.quotedString(this.nameInDb))
				.append(", OPERS, SELECT, SELECT_IDX, INSERT, INSERT_IDX, UPDATE, UPDATE_IDX, DELETE, WHERE, WHERE_IDX);");

		/*
		 * constructor
		 */
		sbf.append("\n\n\t/**  default constructor */");
		sbf.append("\n\tpublic ").append(this.className).append("() {\n\t\tsuper(DBA, META, null);\n\t}");

		sbf.append("\n\n\t/**\n\t * @param values initial values\n\t */");
		sbf.append("\n\tpublic ").append(this.className)
				.append("(Object[] values) {\n\t\tsuper(DBA, META, values);\n\t}");

	}

	private void emitJavaFields(final StringBuilder sbf, final boolean isDb) {
		sbf.append("\n\tprivate static final Field[] FIELDS = ");
		if (this.fields == null) {
			sbf.append("null;");
			return;
		}
		sbf.append("{");
		boolean isFirst = true;
		for (final Field field : this.fields) {
			if (isFirst) {
				isFirst = false;
			} else {
				sbf.append(C);
			}
			field.emitJavaCode(sbf, isDb);
		}
		sbf.append("\n\t};");
	}

	private void emitValidOps(StringBuilder sbf) {
		sbf.append("\n\tprivate static final boolean[] OPERS = {");
		for (IoType op : IoType.values()) {
			if (this.allowedIos != null && this.allowedIos.contains(op)) {
				sbf.append("true,");
			} else {
				sbf.append("false,");
			}
		}

		sbf.setLength(sbf.length() - 1);
		sbf.append("};");
	}

	private void emitJavaValidations(final StringBuilder sbf) {
		sbf.append("\n\tprivate static final IValidation[] VALIDS = {");
		final int n = sbf.length();
		final String sufix = ",\n\t\t";
		if (this.fromToPairs != null) {
			for (final FromToPair pair : this.fromToPairs) {
				pair.emitJavaCode(sbf);
				sbf.append(sufix);
			}
		}

		if (this.exclusivePairs != null) {
			for (final ExclusivePair pair : this.exclusivePairs) {
				pair.emitJavaCode(sbf);
				sbf.append(sufix);
			}
		}

		if (this.inclusivePairs != null) {
			for (final InclusivePair pair : this.inclusivePairs) {
				pair.emitJavaCode(sbf);
				sbf.append(sufix);
			}
		}

		/*
		 * dependent lists
		 */
		if (this.fieldsWithList != null) {
			for (final Field field : this.fieldsWithList) {
				if (field.listKey == null) {
					continue;
				}
				final Field f = this.fieldsMap.get(field.listKey);
				if (f == null) {
					logger.error("DbField {} specifies {} as listKey, but that field is not defined", field.name,
							field.listKey);
					continue;
				}

				sbf.append("new DependentListValidation(").append(field.index);
				sbf.append(C).append(f.index);
				sbf.append(C).append(Util.quotedString(field.listName));
				sbf.append(C).append(Util.quotedString(field.name));
				sbf.append(C).append(Util.quotedString(field.messageId));
				sbf.append(")");
				sbf.append(sufix);
			}
		}

		if (sbf.length() > n) {
			/*
			 * remove last sufix
			 */
			sbf.setLength(sbf.length() - sufix.length());
		}

		sbf.append("\n\t};");
	}

	private void makeWhere(final StringBuilder clause, final StringBuilder indexes) {
		clause.append(" WHERE ");
		boolean firstOne = true;
		for (final Field field : this.keyFields) {
			if (firstOne) {
				firstOne = false;
			} else {
				clause.append(" AND ");
				indexes.append(C);
			}
			clause.append(field.nameInDb).append("=?");
			indexes.append(field.index);
		}
		/*
		 * as a matter of safety, tenant key is always part of queries
		 */
		if (this.tenantField != null) {
			clause.append(" AND ").append(this.tenantField.nameInDb).append("=?");
			indexes.append(C).append(this.tenantField.index);
		}
	}

	private void emitSelect(final StringBuilder sbf) {
		sbf.append(P).append("String SELECT = ");
		if (this.allowedIos.contains(IoType.Get) == false) {
			sbf.append("null;");
			sbf.append(P).append("int[] SELECT_IDX = null;");
			return;
		}
		final StringBuilder idxSbf = new StringBuilder();
		sbf.append(" \"SELECT ");

		boolean firstOne = true;
		for (final Field field : this.fields) {
			if (field.nameInDb == null) {
				continue;
			}
			final FieldType ct = field.fieldTypeEnum;
			if (ct == null) {
				continue;
			}
			if (firstOne) {
				firstOne = false;
			} else {
				sbf.append(C);
				idxSbf.append(C);
			}
			sbf.append(field.nameInDb);
			idxSbf.append(field.index);
		}

		sbf.append(" FROM ").append(this.nameInDb);
		sbf.append("\";");
		sbf.append(P).append("int[] SELECT_IDX = {").append(idxSbf).append("};");

	}

	private void emitInsert(final StringBuilder sbf) {
		sbf.append(P).append(" String INSERT = ");
		if (this.allowedIos.contains(IoType.Create) == false) {
			sbf.append("null;");
			sbf.append(P).append("int[] INSERT_IDX = null;");
			return;
		}

		sbf.append("\"INSERT INTO ").append(this.nameInDb).append('(');
		final StringBuilder idxSbf = new StringBuilder();
		idxSbf.append(P).append("int[] INSERT_IDX = {");
		final StringBuilder vbf = new StringBuilder();
		boolean firstOne = true;
		boolean firstField = true;
		for (final Field field : this.fields) {
			if (field.nameInDb == null) {
				continue;
			}
			final FieldType ct = field.fieldTypeEnum;
			if (ct == null || ct.isInserted() == false) {
				continue;
			}
			if (firstOne) {
				firstOne = false;
			} else {
				sbf.append(C);
				vbf.append(C);
			}
			sbf.append(field.nameInDb);
			if (ct == FieldType.ModifiedAt || ct == FieldType.CreatedAt) {
				vbf.append(" CURRENT_TIMESTAMP ");
			} else {
				vbf.append('?');
				if (firstField) {
					firstField = false;
				} else {
					idxSbf.append(C);
				}
				idxSbf.append(field.index);
			}
		}

		sbf.append(") values (").append(vbf).append(")\";");
		sbf.append(idxSbf).append("};");
	}

	private void emitUpdate(final StringBuilder sbf, final String whereClause, final String whereIndexes) {
		sbf.append(P).append(" String UPDATE = ");
		if (this.allowedIos.contains(IoType.Create) == false) {
			sbf.append("null;");
			sbf.append(P).append("int[] UPDATE_IDX = null;");
			return;
		}

		final StringBuilder updateBuf = new StringBuilder();
		updateBuf.append(" \"UPDATE ").append(this.nameInDb).append(" SET ");

		final StringBuilder idxBuf = new StringBuilder();
		idxBuf.append(P).append(" int[] UPDATE_IDX = {");

		boolean firstOne = true;
		boolean firstField = true;
		for (final Field field : this.fields) {
			if (field.nameInDb == null) {
				continue;
			}
			final FieldType ct = field.fieldTypeEnum;
			if (ct == null || ct.isUpdated() == false) {
				continue;
			}

			if (firstOne) {
				firstOne = false;
			} else {
				updateBuf.append(C);
			}

			updateBuf.append(field.nameInDb).append("=");
			if (ct == FieldType.ModifiedAt) {
				updateBuf.append(" CURRENT_TIMESTAMP ");
			} else {
				updateBuf.append(" ? ");
				if (firstField) {
					firstField = false;
				} else {
					idxBuf.append(C);
				}
				idxBuf.append(field.index);
			}
		}
		if (firstOne) {
			return;
		}
		// update sql will have the where indexes at the end
		if (!firstField) {
			idxBuf.append(C);
		}
		idxBuf.append(whereIndexes);
		updateBuf.append(whereClause);

		if (this.useTimestampCheck) {
			updateBuf.append(" AND ").append(this.timestampField.nameInDb).append("=?");
			idxBuf.append(C).append(this.timestampField.index);
		}
		updateBuf.append("\";");
		sbf.append(updateBuf.toString()).append(idxBuf.toString()).append("};");
	}

	boolean emitJavaTableClass(final StringBuilder sbf, final String generatedPackage) {
		if (this.gotErrors) {
			logger.error("Record {} has errors. Java code not generated for the table. ", this.name);
			return false;
		}
		/*
		 * table is defined only if this record is a DbRecord
		 */
		if (this.nameInDb == null) {
			return false;
		}
		/*
		 * our package name is rootPAckage + any prefix/qualifier in our name
		 *
		 * e.g. if name a.b.record1 then prefix is a.b and className is Record1
		 */
		final String c = Util.toClassName(this.name);
		final String recCls = c + "Record";
		final String cls = c + "Table";
		String pck = generatedPackage + ".rec";
		final String qual = Util.getClassQualifier(this.name);
		if (qual != null) {
			pck += '.' + qual;
		}
		sbf.append("package ").append(pck).append(";\n");

		/*
		 * imports
		 */
		Util.emitImport(sbf, DbTable.class);

		/*
		 * class definition
		 */

		sbf.append("\n\n/**\n * class that represents an array of records of ").append(this.name);
		sbf.append("\n */");
		sbf.append("\npublic class ").append(cls).append(" extends DbTable<").append(recCls).append("> {");

		/*
		 * constructor
		 */
		sbf.append("\n\n\t/** default constructor */");
		sbf.append("\n\tpublic ").append(cls).append("() {\n\t\tsuper(new ").append(recCls).append("());\n\t}");

		sbf.append("\n}\n");
		return true;
	}

	/**
	 *
	 * @param createSbf
	 * @param dataSbf
	 * @return true if sql is emitted, false otherwise
	 */
	public boolean emitSql(final StringBuilder createSbf, final StringBuilder dataSbf) {
		if (this.mainRecordName != null || this.nameInDb == null) {
			return false;
		}
		if (this.gotErrors) {
			logger.error("Record {} is in error. SQL script NOT generated ", this.name);
			return false;
		}
		createSbf.append("\n\nCREATE TABLE ").append(this.nameInDb).append("(\n\t");
		dataSbf.append("\n\nINSERT INTO ").append(this.nameInDb).append(" (");
		StringBuilder valSbf = new StringBuilder("\nVALUES (");
		boolean isFirst = true;
		boolean dataEmitted = false;
		for (Field field : this.fields) {
			if (!field.isColumn()) {
				continue;
			}
			if (isFirst) {
				isFirst = false;
			} else {
				createSbf.append(",\n\t");
			}
			if (dataEmitted) {
				dataSbf.append(", ");
				valSbf.append(", ");
			}
			dataEmitted = field.emitSql(createSbf, dataSbf, valSbf);
		}

		if (this.keyFields != null && this.generatedKeyField == null) {
			createSbf.append(",\n\tPRIMARY KEY(");
			isFirst = true;
			for (Field field : this.keyFields) {
				if (isFirst) {
					isFirst = false;
				} else {
					createSbf.append(',');
				}
				createSbf.append(field.name);
			}
			createSbf.append(')');
		}
		createSbf.append("\n);");

		dataSbf.append(") ").append(valSbf.toString()).append(");");
		return true;
	}
}
