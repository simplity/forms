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
import java.util.Map;

import org.simplity.fm.core.Conventions;
import org.simplity.fm.core.datatypes.ValueType;
import org.simplity.fm.core.rdb.FilterSql;
import org.simplity.fm.core.rdb.FilterWithRecordSql;
import org.simplity.fm.core.rdb.ReadSql;
import org.simplity.fm.core.rdb.ReadWithRecordSql;
import org.simplity.fm.core.rdb.WriteSql;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author simplity.org
 *
 */
public class Sql {
	private static final Logger logger = LoggerFactory.getLogger(Sql.class);
	private static final String P = "\n\tprivate static final ";
	private static final String SQL_TYPE_FILTER = "filter";
	private static final String SQL_TYPE_READ = "read";
	private static final String SQL_TYPE_WRITE = "write";

	String name;
	String sqlType;
	String sql;
	Field[] sqlParams;
	Field[] outputFields;
	String recordName;
	private boolean hasDate = false;
	private boolean hasTime = false;
	private String className;

	void init(Map<String, ValueSchema> schemas) {
		this.className = Util.toClassName(this.name);
		/*
		 * see if we have any date /time fields
		 */
		if (this.sqlParams != null) {
			this.initArr(this.sqlParams, schemas);
		}

		if (this.outputFields != null) {
			this.initArr(this.outputFields, schemas);
		}
	}

	private void initArr(Field[] fields, Map<String, ValueSchema> schemas) {
		for (int idx = 0; idx < fields.length; idx++) {
			Field f = fields[idx];
			f.init(idx, schemas);
			ValueType vt = f.schemaInstance.valueTypeEnum;
			if (vt == ValueType.Date) {
				this.hasDate = true;
			} else if (vt == ValueType.Timestamp) {
				this.hasTime = true;
			}
		}
	}

	void generateJava(String folderName, final String rootPackage) {

		final StringBuilder sbf = new StringBuilder();
		final boolean hasRecord = this.recordName != null
				&& this.recordName.isEmpty() == false;
		final boolean hasOutFields = this.outputFields != null
				&& this.outputFields.length > 0;
		final boolean hasParams = this.sqlParams != null
				&& this.sqlParams.length > 0;
		boolean isWrite = false;

		String msg = null;
		if (this.sqlType.equals(SQL_TYPE_WRITE)) {
			isWrite = true;
			if (hasRecord || hasOutFields) {
				msg = "Write sql should not specify record or output fields.";
			}
			if (!hasParams) {
				msg = "Write sql MUST have sql parameters. Unconditional update to database is not allowed";
			}
		} else if (this.sqlType.equals(SQL_TYPE_READ)
				|| this.sqlType.equals(SQL_TYPE_FILTER)) {
			if ((hasRecord && hasOutFields) || (!hasRecord && !hasOutFields)) {
				msg = "read/filter sql should have either recordName or outputFields";
			} else {
				final String txt = this.sql.trim().toLowerCase();
				if (hasRecord) {
					if (txt.startsWith("where") == false) {
						msg = "When record is specified, the sql should start with 'where' verb, and should be valid prase to append after a select ... part.";
					}
				} else {
					if (txt.startsWith("select") == false) {
						msg = "When output fields are specified, sql must start be a valid select sql starting with the verb 'select'";
					}
				}
			}
		} else {
			msg = this.sqlType + " is invalid. it has to be read/write/filter";
		}

		if (msg != null) {
			logger.error(msg);
			sbf.append(msg);
			return;
		}

		emitImports(sbf, rootPackage);
		if (this.hasDate) {
			Util.emitImport(sbf, LocalDate.class);
		}
		if (this.hasTime) {
			Util.emitImport(sbf, Instant.class);
		}

		// emit the right type of sql
		if (isWrite) {
			this.emitWriteSql(sbf);
		} else if (this.sqlType.equals(SQL_TYPE_READ)) {
			if (hasRecord) {
				this.emitReadWithRecord(sbf, rootPackage);
			} else {
				this.emitRead(sbf);
			}
		} else if (hasRecord) {
			this.emitFilterWithRecord(sbf, rootPackage);

		} else {
			this.emitFilter(sbf);
		}

		Util.writeOut(folderName + this.className + ".java", sbf.toString());
	}

	void emitWriteSql(final StringBuilder sbf) {

		Util.emitImport(sbf, WriteSql.class);

		/*
		 * class
		 */
		sbf.append("\n\n/** generated class for ").append(this.className)
				.append(" */");
		sbf.append("\npublic class ").append(this.className)
				.append(" extends WriteSql {");

		/*
		 * static declarations
		 */
		sbf.append(P).append("String SQL = ")
				.append(Util.qoutedString(this.sql)).append(';');
		sbf.append(P).append("Field[] IN = ");
		emitFields(sbf, this.sqlParams);
		sbf.append(';');

		/*
		 * constructor
		 */
		sbf.append("\n\n\t/** default constructor */\n\tpublic ")
				.append(this.className).append("() {");
		sbf.append("\n\t\tthis.sqlText = SQL;");
		sbf.append("\n\t\tthis.inputData = new Record(IN, null);");
		sbf.append("\n\t}");

		this.emitSetters(sbf);
		sbf.append("\n}\n");
	}

	void emitFilterWithRecord(final StringBuilder sbf,
			final String rootPackage) {

		Util.emitImport(sbf, FilterWithRecordSql.class);
		final String recordCls = Util.toClassName(this.recordName) + "Record";
		sbf.append("\nimport ").append(rootPackage).append(".rec.")
				.append(recordCls).append(";");

		/*
		 * class
		 */
		sbf.append("\n\n/** generated class for ").append(this.className)
				.append(" */");
		sbf.append("\npublic class ").append(this.className)
				.append(" extends FilterWithRecordSql<");
		sbf.append(recordCls).append("> {");

		/*
		 * static declarations
		 */
		sbf.append(P).append("String SQL = ")
				.append(Util.qoutedString(this.sql)).append(';');
		if (this.sqlParams != null) {
			sbf.append(P).append("Field[] IN = ");
			emitFields(sbf, this.sqlParams);
			sbf.append(';');
		}

		/*
		 * constructor
		 */
		sbf.append("\n\n\t/** default constructor */\n\tpublic ")
				.append(this.className).append("() {");
		sbf.append("\n\t\tthis.sqlText = SQL;");
		if (this.sqlParams != null) {
			sbf.append("\n\t\tthis.inputData = new Record(IN, null);");
		}
		sbf.append("\n\t\tthis.record = new ").append(recordCls).append("();");
		sbf.append("\n\t}");

		if (this.sqlParams != null) {
			this.emitSetters(sbf);
		}
		sbf.append("\n}\n");

	}

	void emitReadWithRecord(final StringBuilder sbf, final String rootPackage) {

		Util.emitImport(sbf, ReadWithRecordSql.class);
		final String recordCls = Util.toClassName(this.recordName) + "Record";
		sbf.append("\nimport ").append(rootPackage).append(".rec.")
				.append(recordCls).append(';');

		/*
		 * class
		 */
		sbf.append("\n\n/** generated class for ").append(this.className)
				.append(" */");
		sbf.append("\npublic class ").append(this.className)
				.append(" extends ReadWithRecordSql<").append(recordCls)
				.append("> {");

		/*
		 * static declarations
		 */
		sbf.append(P).append("String SQL = ")
				.append(Util.qoutedString(this.sql)).append(';');
		if (this.sqlParams != null) {
			sbf.append(P).append("Field[] IN = ");
			emitFields(sbf, this.sqlParams);
			sbf.append(';');
		}

		/*
		 * constructor
		 */
		sbf.append("\n\n\t/** default constructor */\n\tpublic ")
				.append(this.className).append("() {");
		sbf.append("\n\t\tthis.sqlText = SQL;");
		if (this.sqlParams != null) {
			sbf.append("\n\t\tthis.inputData = new Record(IN, null);");
		}
		sbf.append("\n\t\tthis.record = new ").append(recordCls).append("();");
		sbf.append("\n\t}");

		if (this.sqlParams != null) {
			this.emitSetters(sbf);
		}
		sbf.append("\n}\n");
	}

	void emitRead(final StringBuilder sbf) {
		Util.emitImport(sbf, ReadSql.class);
		/*
		 * class
		 */
		sbf.append("\n\n/** generated class for ").append(this.className)
				.append(" */");
		sbf.append("\npublic class ").append(this.className)
				.append(" extends ReadSql<").append(this.className)
				.append(".OutputRecord> {");

		/*
		 * static declarations
		 */
		sbf.append(P).append("String SQL = ")
				.append(Util.qoutedString(this.sql)).append(';');
		if (this.sqlParams != null) {
			sbf.append(P).append("Field[] IN = ");
			emitFields(sbf, this.sqlParams);
			sbf.append(';');
		}

		sbf.append("\n\tprotected static final Field[] OUT = ");
		emitFields(sbf, this.outputFields);
		sbf.append(';');

		/*
		 * constructor
		 */
		sbf.append("\n\n\t/** default constructor */\n\tpublic ")
				.append(this.className).append("() {");
		sbf.append("\n\t\tthis.sqlText = SQL;");
		if (this.sqlParams != null) {
			sbf.append("\n\t\tthis.inputData = new Record(IN, null);");
		}
		sbf.append("\n\t}");

		if (this.sqlParams != null) {
			this.emitSetters(sbf);
		}
		this.emitOutMethods(sbf);
		sbf.append("\n}\n");
	}

	void emitFilter(final StringBuilder sbf) {

		Util.emitImport(sbf, FilterSql.class);
		/*
		 * class
		 */
		sbf.append("\n\n/** generated class for ").append(this.className)
				.append(" */");
		sbf.append("\npublic class ").append(this.className)
				.append(" extends FilterSql<").append(this.className)
				.append(".OutputRecord> {");

		/*
		 * static declarations
		 */
		sbf.append(P).append("String SQL = ")
				.append(Util.qoutedString(this.sql)).append(';');
		if (this.sqlParams != null) {
			sbf.append(P).append("Field[] IN = ");
			emitFields(sbf, this.sqlParams);
			sbf.append(';');
		}

		sbf.append("\n\tprotected static final Field[] OUT = ");
		emitFields(sbf, this.outputFields);
		sbf.append(';');

		/*
		 * constructor
		 */
		sbf.append("\n\n\t/** default constructor */\n\tpublic ")
				.append(this.className).append("() {");
		sbf.append("\n\t\tthis.sqlText = SQL;");
		if (this.sqlParams != null) {
			sbf.append("\n\t\tthis.inputData = new Record(IN, null);");
		}
		sbf.append("\n\t}");

		if (this.sqlParams != null) {
			this.emitSetters(sbf);
		}
		this.emitOutMethods(sbf);
		sbf.append("\n}\n");
	}

	private static void emitImports(final StringBuilder sbf,
			final String rootPackage) {
		sbf.append("package ").append(rootPackage).append(".sql;\n");

		Util.emitImport(sbf, org.simplity.fm.core.data.Field.class);
		Util.emitImport(sbf, org.simplity.fm.core.data.Record.class);
		sbf.append("\nimport ").append(rootPackage).append('.')
				.append(Conventions.App.GENERATED_DATA_TYPES_CLASS_NAME)
				.append(';');

	}

	private void emitOutMethods(final StringBuilder sbf) {
		/*
		 * abstract method to be implemented
		 */
		sbf.append("\n\n\t@Override\n\tprotected ");
		sbf.append("OutputRecord newOutputData() {");
		sbf.append("\n\t\treturn new OutputRecord(OUT);\n\t}");
		/*
		 * inner class for custom Vo
		 */

		sbf.append("\n\n\t/** Record with output fields from this Sql */");
		sbf.append("\n\tpublic static class OutputRecord extends Record {");

		sbf.append("\n\n\t\t/**\n\t\t * @param fields\n\t\t */");
		sbf.append("\n\t\tpublic OutputRecord(final Field[] fields) {");
		sbf.append("\n\t\t\tsuper(fields, null);\n\t\t}");

		this.emitGetters(sbf);

		/*
		 * over-ride new instance
		 */
		sbf.append(
				"\n\n\t@Override\n\tpublic OutputRecord newInstance(Object[] arr) {");
		sbf.append("\n\t\treturn new OutputRecord(OUT);\n\t}");
		sbf.append("\n\t}");

	}

	private void emitSetters(final StringBuilder sbf) {
		for (final Field f : this.sqlParams) {
			final ValueSchema vs = f.schemaInstance;
			String typ = "unknownBecauseOfUnknownDataType";
			if (vs == null) {
				logger.error("Field {} has an invalid data type of {}",
						f.fieldName, f.valueSchema);
			} else {
				typ = Util.JAVA_VALUE_TYPES[vs.valueTypeEnum.ordinal()];
			}
			final String nam = f.fieldName;
			final String cls = Util.toClassName(nam);

			sbf.append("\n\n\t/**\n\t * set value for ").append(nam);
			sbf.append("\n\t * @param value to be assigned to ").append(nam);
			sbf.append("\n\t */");
			sbf.append("\n\tpublic void set").append(cls).append('(')
					.append(typ).append(" value){");
			sbf.append("\n\t\tthis.inputData.assignValue(").append(f.index)
					.append(", value);");
			sbf.append("\n\t}");
		}
	}

	/**
	 * @param constr
	 * @param schemas
	 */
	private void emitGetters(final StringBuilder sbf) {
		for (final Field f : this.outputFields) {
			final ValueSchema vs = f.schemaInstance;
			String typ = "unknownBecauseOfUnknownDataType";
			String get = typ;
			if (vs == null) {
				logger.error("Field {} has an invalid data type of {}",
						f.fieldName, f.valueSchema);
			} else {
				typ = Util.JAVA_VALUE_TYPES[vs.valueTypeEnum.ordinal()];
				get = Util.JAVA_GET_TYPES[vs.valueTypeEnum.ordinal()];
			}
			final String nam = f.fieldName;
			final String cls = Util.toClassName(nam);

			sbf.append("\n\n\t\t/**\n\t * @return value of ").append(nam)
					.append("\n\t */");
			sbf.append("\n\t\tpublic ").append(typ).append(" get").append(cls)
					.append("(){");
			sbf.append("\n\t\t\treturn super.fetch").append(get)
					.append("Value(").append(f.index).append(");");
			sbf.append("\n\t\t}");
		}
	}

	private static void emitFields(final StringBuilder sbf,
			final Field[] fields) {
		sbf.append('{');
		int idx = -1;
		for (final Field field : fields) {
			idx++;
			if (idx > 0) {
				sbf.append(',');
			}
			field.index = idx;
			field.emitJavaCode(sbf, false);
		}
		sbf.append('}');
	}
}
