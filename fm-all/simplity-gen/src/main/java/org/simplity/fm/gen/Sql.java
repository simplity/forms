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
import java.util.List;
import java.util.Map;

import org.simplity.fm.core.Conventions;
import org.simplity.fm.core.valueschema.ValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author simplity.org
 *
 */
public class Sql {
	private static final Logger logger = LoggerFactory.getLogger(Sql.class);
	private static final String P = "\n\tprivate static final ";
	private static final String SQL_TYPE_READ_MANY = "readMany";
	private static final String SQL_TYPE_READ_ONE = "readOne";
	private static final String SQL_TYPE_WRITE = "write";

	String name;
	String sqlType;
	String selectFrom;
	String sql;
	Field[] inputParameters;
	String inputRecord;
	Field[] outputFields;
	String outputRecord;

	private boolean hasDate = false;
	private boolean hasTime = false;
	private String className;
	private String inRecClassName;
	private String outRecClassName;
	private Record inRec;

	void init(Map<String, ValueSchema> schemas) {
		this.className = Util.toClassName(this.name) + "Sql";
		/*
		 * see if we have any date /time fields
		 */
		if (this.inputParameters != null) {
			this.initArr(this.inputParameters, schemas);
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

	void generateJava(String folderName, final String rootPackage,
			Map<String, Record> records) {

		if (!this.validate(records)) {
			return;
		}

		final StringBuilder sbf = new StringBuilder();

		emitImports(sbf, rootPackage);
		sbf.append("\n\n/** generated class for ").append(this.className)
				.append(" */");
		sbf.append("\npublic class ").append(this.className)
				.append(" extends Sql {");

		this.emitStaticFields(sbf);
		this.emitConstructor(sbf);

		/**
		 * emit setters, but only if input fields are used.
		 */
		if (this.inputParameters != null) {
			Util.emitSettersValues(sbf, this.inputParameters,
					"this.parameterValues");
		}

		if (this.outputFields != null) {
			Util.emitGettersFromValues(sbf, this.outputFields,
					"this.parameterValues");
		}
		if (this.sqlType == SQL_TYPE_READ_ONE) {
			this.emitReadMethods(sbf);
		} else if (this.sqlType == SQL_TYPE_WRITE) {
			this.emitWriteMethods(sbf);
		} else {
			this.emitReadManyMethods(sbf);
		}

		sbf.append("\n}\n");
	}

	private void emitReadMethods(StringBuilder sbf) {
		// TODO Auto-generated method stub

	}

	private void emitWriteMethods(final StringBuilder sbf) {
		if (this.inputRecord != null) {
			this.emitWriteWithRecord(sbf);
			this.emitWriteFailWithRecord(sbf);
			// record based SQL can be used for batch as well
			this.emitWriteManyWithTable(sbf);
			return;
		}

		this.emitWriteWithFields(sbf);
		this.emitWriteFailWithFields(sbf);
	}

	private void emitReadManyMethods(StringBuilder sbf) {
		if (this.inputRecord != null) {
			if (this.outputRecord != null) {
				this.emitReadWithRecords(sbf);
				this.emitReadFailWithRecords(sbf);
				return;
			}
			this.emitReadWithRecordAndFields(sbf);
			this.emitReadFailWithRecordAndFields(sbf);
			return;
		}
		if (this.outputRecord != null) {
			this.emitReadWithRecords(sbf);
			this.emitReadFailWithRecords(sbf);
			return;
		}
		this.emitReadWithRecordAndFields(sbf);
		this.emitReadFailWithRecordAndFields(sbf);
		return;
	}

	private boolean validate(Map<String, Record> records) {
		List<String> msgs = new ArrayList<>();

		if (this.inputRecord != null) {
			this.inRec = records.get(this.inputRecord);
			if (this.inputRecord == null) {
				msgs.add(this.inputRecord
						+ " is specified as inputRecord, but it is not defined as a record");
			} else {
				this.inRecClassName = Util.toClassName(this.inputRecord)
						+ "Record";
			}
		}

		if (this.outputRecord != null) {
			Record rec = records.get(this.outputRecord);
			if (rec == null) {
				msgs.add(this.outputRecord
						+ " is specified as outputRecord, but it is not defined as a record");
			} else {
				this.outRecClassName = Util.toClassName(this.outputRecord)
						+ "Record";
			}
		}

		final boolean hasOutFields = this.outputFields != null
				|| this.outputRecord != null;

		final boolean hasParams = this.inputParameters != null
				|| this.inputRecord != null;

		if (this.inputParameters != null && this.inputRecord != null) {
			msgs.add(
					"Input parameters are to be specified either with inputParamaters or inputRecord, but not both");
		}

		if (this.outputFields != null && this.outputRecord != null) {
			msgs.add(
					"Output parameters are are to be specified either with outputFields or outputRecord, but not both");
		}

		if (this.sqlType.equals(SQL_TYPE_WRITE)) {
			if (hasOutFields) {
				msgs.add(
						"Write sql should not specify output fields or output record.");
			}

			if (!hasParams) {
				msgs.add(
						"Write sql MUST have sql parameters. Unconditional update to database is not allowed");
			}
		} else if (this.sqlType.equals(SQL_TYPE_READ_ONE)
				|| this.sqlType.equals(SQL_TYPE_READ_MANY)) {
			if (this.selectFrom == null) {

			}
			final String txt = this.sql.trim().toLowerCase();
			if (txt.startsWith("where") == false) {
				msgs.add(
						"A Read-SQL should start with 'where' clause. SELECT clause is genrated and prefixed to this sql by the generator.");
			}
		} else {
			msgs.add(this.sqlType
					+ " is invalid. it has to be read/writeOne/writeMany");
		}

		if (msgs.size() != 0) {
			for (String msg : msgs) {
				logger.error(msg);
			}
			return false;
		}
		return true;

	}

	private void generateSelectSql(Field[] fields) {
		final StringBuilder sbf = new StringBuilder();
		sbf.append("SELECT ");
		for (Field field : fields) {
			sbf.append(field.name).append(',');
		}
		sbf.setLength(sbf.length() - 1);

		sbf.append(" FROM ").append(this.selectFrom).append(' ')
				.append(this.sql);
		this.sql = sbf.toString() + sql;
	}

	private void emitImports(final StringBuilder sbf,
			final String rootPackage) {
		sbf.append("package ").append(rootPackage).append(".sql;\n");

		if (this.hasDate) {
			Util.emitImport(sbf, LocalDate.class);
		}
		if (this.hasTime) {
			Util.emitImport(sbf, Instant.class);
		}
		Util.emitImport(sbf, org.simplity.fm.core.data.Field.class);
		Util.emitImport(sbf, org.simplity.fm.core.data.Record.class);
		Util.emitImport(sbf, org.simplity.fm.core.data.DataTable.class);
		Util.emitImport(sbf, org.simplity.fm.core.db.Sql.class);
		sbf.append("\nimport ").append(rootPackage).append('.')
				.append(Conventions.App.GENERATED_VALUE_SCHEMAS_CLASS_NAME)
				.append(';');

		if (this.inRecClassName != null) {
			sbf.append("\nimport ").append(rootPackage).append(".rec.")
					.append(this.inRecClassName).append(';');
		}

		if (this.outRecClassName != null) {
			sbf.append("\nimport ").append(rootPackage).append(".rec.")
					.append(this.outRecClassName).append(';');
		}

	}

	private void emitStaticFields(final StringBuilder sbf) {
		sbf.append(P).append("String SQL = ").append(this.sql);
		sbf.append(P).append("String[] NAMES = ");
		final StringBuilder typeBuf = new StringBuilder();
		if (this.inputParameters != null) {
			Util.emitNamesFromFields(this.inputParameters, sbf);
			Util.emitTypesArray(this.inputParameters, typeBuf);
		} else if (this.inRec != null) {
			Util.emitNamesFromFields(this.inRec.fields, sbf);
			typeBuf.append("null;");
		} else {
			sbf.append("null;");
			typeBuf.append("null;");
		}
		sbf.append(P).append("ValueType[] IN_TYPES = ").append(typeBuf);

		sbf.append(P).append("ValueType[] OUT_TYPES = ");
		if (this.outputFields == null) {
			sbf.append("null;");
		} else {
			Util.emitTypesArray(this.outputFields, typeBuf);
		}
	}

	private void emitConstructor(final StringBuilder sbf) {
		sbf.append("\n\n\t/** \n\t * default constructor\n\t */\n\tpublic ")
				.append(this.className).append("() {");
		sbf.append("\n\t\tsuper(SQL, NAMES, IN_TYPES, OUT_TYPES);");
		sbf.append("\n\t}");
	}

	private void emitReadWithFieldsAndRecord(final StringBuilder sbf) {
		sbf.append(BEGIN_COMMENT);
		sbf.append(COMMENT_PREFIX).append(
				"To be called only after setting values to all the input parameters using setter methods. Output/selected fields are xtracted into the ouput record");
		sbf.append(HANDLE_PARAM);
		sbf.append(OUT_REC_PARAM);
		sbf.append(BOOL_RETURN);
		sbf.append(END_COMMENT);

		sbf.append(BEGIN_METHOD)
				.append("boolean read(final IReadonlyHandle handle, final ")
				.append(this.outRecClassName).append(" outputRecord")
				.append(SQL_EX);
		sbf.append(SUPER_RETURN).append("read(handle, outputRecord);");
		sbf.append(END_METHOD);
	}

	private void emitReadFailWithFieldsAndRecord(final StringBuilder sbf) {
		sbf.append(BEGIN_COMMENT);
		sbf.append(COMMENT_PREFIX).append(
				"To be called only after setting values to all the input parameters using setter methods. Output/selected fields are xtracted into the ouput record");
		sbf.append(HANDLE_PARAM);
		sbf.append(OUT_REC_PARAM);
		sbf.append(END_FAIL_COMMENT);
		sbf.append(BEGIN_METHOD)
				.append("void readOrFail(final IReadonlyHandle handle, final ")
				.append(this.outRecClassName).append(" outputRecord")
				.append(SQL_EX);
		sbf.append(SUPER_VOID).append("readOrFail(handle, outputRecord);");
		sbf.append(END_METHOD);
	}

	private void emitReadManyWithFieldsAndRecord(final StringBuilder sbf) {
		sbf.append(BEGIN_COMMENT);
		sbf.append(COMMENT_PREFIX).append(
				"To be called only after setting values to all the input parameters using setter methods. Output rows are populated into the data table");
		sbf.append(HANDLE_PARAM);
		sbf.append(OUT_TABLE_PARAM);
		sbf.append(END_COMMENT);
		sbf.append(BEGIN_METHOD).append(
				"void readMany(final IReadonlyHandle handle, final DataTable<")
				.append(this.outRecClassName).append("> dataTable")
				.append(SQL_EX);
		sbf.append(SUPER_VOID).append("readMany(handle, dataTable);");
		sbf.append(END_METHOD);
	}

	private void emitReadWithRecords(final StringBuilder sbf) {
		sbf.append(BEGIN_COMMENT);
		sbf.append(COMMENT_PREFIX).append(
				"values for parameters are taken from the input Record. On successful read operation, output record is populated with the output values");
		sbf.append(HANDLE_PARAM);
		sbf.append(IN_REC_PARAM);
		sbf.append(OUT_REC_PARAM);
		sbf.append(BOOL_RETURN);
		sbf.append(BEGIN_METHOD)
				.append("boolean read(final IReadonlyHandle handle, final ")
				.append(this.inRecClassName).append(" inputRecord, final ")
				.append(this.outRecClassName).append(" outputRecord")
				.append(SQL_EX);
		sbf.append(SUPER_RETURN)
				.append("read(handle, inputRecord, outputRecord);");
		sbf.append(END_METHOD);
	}

	private void emitReadFailWithRecords(final StringBuilder sbf) {
		sbf.append(BEGIN_COMMENT);
		sbf.append(COMMENT_PREFIX).append(
				"values for parameters are taken from the input Record. On successful read operation, output record is populated with the output values");
		sbf.append(HANDLE_PARAM);
		sbf.append(IN_REC_PARAM);
		sbf.append(OUT_REC_PARAM);
		sbf.append(END_FAIL_COMMENT);
		sbf.append(BEGIN_METHOD)
				.append("void readOrFail(final IReadonlyHandle handle, final ")
				.append(this.inRecClassName).append(" inputRecord, ")
				.append(this.outRecClassName).append(" outputputRecord")
				.append(SQL_EX);
		sbf.append(SUPER_VOID)
				.append("readOrFail(handle, inputRecord, outputRecord);");
		sbf.append(END_METHOD);
	}

	private void emitReadManyWithRecords(final StringBuilder sbf) {
		sbf.append(BEGIN_COMMENT);
		sbf.append(COMMENT_PREFIX).append(
				"values for parameters are taken from the input Record. On successful read operation, output rows are appended into the datatable");
		sbf.append(HANDLE_PARAM);
		sbf.append(IN_REC_PARAM);
		sbf.append(OUT_TABLE_PARAM);
		sbf.append(BEGIN_METHOD)
				.append("void readMany(final IReadonlyHandle handle, final")
				.append(this.inRecClassName)
				.append(" inputRecord, final DataTable<")
				.append(this.outRecClassName).append("> dataTable")
				.append(SQL_EX);
		sbf.append(SUPER_VOID).append("read(handle, inputRecord, dataTable);");
		sbf.append(END_METHOD);
	}

	private void emitReadWithRecordAndFields(final StringBuilder sbf) {
		sbf.append(BEGIN_COMMENT);
		sbf.append(COMMENT_PREFIX).append(
				"Parameters are taken from the input record. On successful read operation, utput fields can be fetched with the getter methods");
		sbf.append(HANDLE_PARAM);
		sbf.append(IN_REC_PARAM);
		sbf.append(BOOL_RETURN);
		sbf.append(END_COMMENT);
		sbf.append(BEGIN_METHOD)
				.append("boolean readIn(final IReadonlyHandle handle, final ")
				.append(this.inRecClassName).append(" inputRecord")
				.append(SQL_EX);
		sbf.append(SUPER_RETURN).append("readIn(handle, inputRecord);");
		sbf.append(END_METHOD);
	}

	private void emitReadFailWithRecordAndFields(final StringBuilder sbf) {
		sbf.append(BEGIN_COMMENT);
		sbf.append(COMMENT_PREFIX).append(
				"Parameters are taken from the input record. On successful read operation, utput fields can be fetched with the getter methods");
		sbf.append(HANDLE_PARAM);
		sbf.append(IN_REC_PARAM);
		sbf.append(END_FAIL_COMMENT);
		sbf.append(BEGIN_METHOD)
				.append("void readInOrFail(final IReadonlyHandle handle")
				.append(SQL_EX);
		sbf.append(SUPER_VOID).append("readInOrFail(handle);");
		sbf.append(END_METHOD);
	}

	private void emitReadWithFields(final StringBuilder sbf) {
		sbf.append(BEGIN_COMMENT);
		sbf.append(COMMENT_PREFIX).append(
				"To be called after setting values to all the parameters using setter methods. Output fields may be extracted after this call using the getter methods");
		sbf.append(HANDLE_PARAM);
		sbf.append(BOOL_RETURN);
		sbf.append(END_COMMENT);
		sbf.append(BEGIN_METHOD)
				.append("int readIn(final IReadonlyHandle handle")
				.append(SQL_EX);
		sbf.append(SUPER_RETURN).append("readIn(handle);");
		sbf.append(END_METHOD);
	}

	private void emitReadFailWithFields(final StringBuilder sbf) {
		sbf.append(BEGIN_COMMENT);
		sbf.append(COMMENT_PREFIX).append(
				"To be called after setting values to all the parameters using setter methods. Output fields may be extracted after this call using the getter methods");
		sbf.append(HANDLE_PARAM);
		sbf.append(END_FAIL_COMMENT);
		sbf.append(BEGIN_METHOD)
				.append("void readInOrFail(final IReadonlyHandle handle")
				.append(SQL_EX);
		sbf.append(SUPER_VOID).append("readInOrFail(handle);");
		sbf.append(END_METHOD);
	}

	private void emitWriteWithFields(final StringBuilder sbf) {
		sbf.append(BEGIN_COMMENT);
		sbf.append(COMMENT_PREFIX).append(
				"To be called after setting values to all the parameters using setter methods.");
		sbf.append(HANDLE_PARAM);
		sbf.append(INT_RETURN);
		sbf.append(END_COMMENT);
		sbf.append(BEGIN_METHOD)
				.append("int write(final IReadWriteHandle handle")
				.append(SQL_EX);
		sbf.append(SUPER_RETURN).append("write(handle);");
		sbf.append(END_METHOD);
	}

	private void emitWriteFailWithFields(final StringBuilder sbf) {
		sbf.append(BEGIN_COMMENT);
		sbf.append(COMMENT_PREFIX).append(
				"To be called after setting values to all the parameters using setter methods.");
		sbf.append(HANDLE_PARAM);
		sbf.append(INT_RETURN);
		sbf.append(END_FAIL_COMMENT);
		sbf.append(BEGIN_METHOD)
				.append("int write(final IReadWriteHandle handle")
				.append(SQL_EX);
		sbf.append(SUPER_RETURN).append("write(handle);");
		sbf.append(END_METHOD);
	}

	private void emitWriteWithRecord(final StringBuilder sbf) {
		sbf.append(BEGIN_COMMENT);
		sbf.append(COMMENT_PREFIX)
				.append("update the db with values taken from the record");
		sbf.append(HANDLE_PARAM);
		sbf.append(IN_REC_PARAM);
		sbf.append(INT_RETURN);
		sbf.append(END_COMMENT);
		sbf.append(BEGIN_METHOD)
				.append("int write(final IReadWriteHandle handle, ")
				.append(this.inRecClassName).append(" inputRecord")
				.append(SQL_EX);
		sbf.append(SUPER_RETURN).append("write(handle, inputRecord);");
		sbf.append(END_METHOD);
	}

	private void emitWriteFailWithRecord(final StringBuilder sbf) {
		sbf.append(BEGIN_COMMENT);
		sbf.append(COMMENT_PREFIX)
				.append("update the db with values taken from the record");
		sbf.append(HANDLE_PARAM);
		sbf.append(IN_REC_PARAM);
		sbf.append(INT_RETURN);
		sbf.append(END_FAIL_COMMENT);
		sbf.append(BEGIN_METHOD)
				.append("int writeOrFail(final IReadWriteHandle handle, ")
				.append(this.inRecClassName).append(" inputRecord")
				.append(SQL_EX);
		sbf.append(SUPER_RETURN).append("writeOrFail(handle, inputRecord);");
		sbf.append(END_METHOD);
	}

	private void emitWriteManyWithTable(final StringBuilder sbf) {
		sbf.append(BEGIN_COMMENT);
		sbf.append(COMMENT_PREFIX).append(
				"execute a batch-update with data rows from the the data table");
		sbf.append(HANDLE_PARAM);
		sbf.append(IN_TABLE_PARAM);
		sbf.append(INT_RETURN);
		sbf.append(END_COMMENT);
		sbf.append(BEGIN_METHOD).append(
				"int writeMany(final IReadWriteHandle handle, DataTable<")
				.append(this.inRecClassName).append("> table").append(SQL_EX);
		sbf.append(SUPER_RETURN).append("writeMany(handle, table);");
		sbf.append(END_METHOD);
	}

	private static final String SUPER_VOID = "\n\t\t\t super.";
	private static final String SUPER_RETURN = "\n\t\t\t return super.";
	private static final String SQL_EX = ") throws SQLException {";
	private static final String BEGIN_METHOD = "\n\t@Override\n\tpublic ";
	private static final String END_METHOD = "\n\t}";
	private static final String BEGIN_COMMENT = "\n\t/**";
	private static final String COMMENT_PREFIX = "\n\t * ";
	private static final String HANDLE_PARAM = "\n\t *\n\t * @param handle";
	private static final String IN_REC_PARAM = "\n\t * @param inputRecord from which parameter values are set to the sql";
	private static final String OUT_REC_PARAM = "\n\t * @param outputRecord to which output fields are extracted to";
	private static final String IN_TABLE_PARAM = "\n\t * @param dataTable that has the rows for preparing batch-update commands";
	private static final String OUT_TABLE_PARAM = "\n\t * @param dataTable to which output rows are extracted to";
	private static final String END_COMMENT = "\n\t * @throws SQLException\n\t */";
	private static final String BOOL_RETURN = "\n\t * @return true read was successful. false otherwise.";
	private static final String INT_RETURN = "\n\t * @return number of rowsread/affected";
	private static final String NON_ZERO_RETURN = "\n\t * @return non-zero number of rows affected. ";
	private static final String END_FAIL_COMMENT = "\n\t * @throws SQLException if no rows read/affected, or on any DB related error\n\t */";
}
