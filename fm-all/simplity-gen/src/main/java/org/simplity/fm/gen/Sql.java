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

import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.simplity.fm.core.ApplicationError;
import org.simplity.fm.core.Conventions;
import org.simplity.fm.core.db.IReadWriteHandle;
import org.simplity.fm.core.db.IReadonlyHandle;
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

	/*
	 * sqlPurpose values
	 */
	private static final String SQL_TYPE_READ_ONE = "readOne";
	private static final String SQL_TYPE_READ_MANY = "readMany";
	private static final String SQL_TYPE_WRITE = "write";
	private static final String SQL_TYPE_CALL = "call";

	/*
	 * standard verbs with which a sql should start
	 */
	private static final String SQL_INSERT = "insert";
	private static final String SQL_UPDATE = "update";
	private static final String SQL_WHERE = "where";

	/*
	 * texts to generate java methods. Since these classes are consumed by app
	 * Programmers, we have to generate good comments as well
	 */

	/**
	 * comments : Begin, params, return and end
	 */
	private static final String BEGIN_COMMENT = "\n\n\t/**";
	private static final String COMMENT_PREFIX = "\n\t * ";

	private static final String COMMENT_SP = "Call this stored procedure. Only the returned vakue, if any is returned. The results of the stored procedure, if any are to be extracted subsequently with calls to  ";
	/*
	 * descriptions for handling the input
	 */
	private static final String DESCRIPTION_SETTERS = "To be called only after setting values to all the input parameters using setter methods.";
	private static final String DESCRIPTION_INPUT_RECORD = "To be called after ensuring that the inputRecord has the right values for the input parameters of this sql.";
	private static final String DESCRIPTION_INPUT_TABLE = "To be called after ensuring that the dataTable has the right rows for the batch oprration.";

	/*
	 * description for using the output
	 */
	private static final String DESCRIPTION_GETTERS = "On successful read operation, getter methods may be used to get the output fields values";
	private static final String DESCRIPTION_OUTPUT_RECORD = " On successful completion of read operation, output/selected fields populated into the outputRecord.";
	private static final String DESCRIPTION_OUTPUT_TABLE = " DataTable has all the output rows, once the read operation is successful";

	/*
	 * java-doc for parameters
	 */
	private static final String HANDLE_PARAM = "\n\t *\n\t * @param handle Db handle of the right type";
	private static final String IN_REC_PARAM = "\n\t * @param inputRecord from which parameter values are set to the sql";
	private static final String OUT_REC_PARAM = "\n\t * @param outputRecord to which output fields are to be extracted";
	private static final String IN_TABLE_PARAM = "\n\t * @param dataTable that has the rows for preparing batch-update commands";
	private static final String OUT_TABLE_PARAM = "\n\t * @param dataTable to which output rows are to be extracted";

	private static final String BOOL_RETURN = "\n\t * @return true if read was successful. false otherwise.";
	private static final String INT_RETURN = "\n\t * @return number of rows read/affected";
	private static final String NON_ZERO_RETURN = "\n\t * @return non-zero number of rows affected.";
	private static final String SP_RETURN = "\n\t * @return retruned value from the stored procedure. null if teh stored procedure has no return specification";

	/*
	 * copy=pasted from StoredProcedure class with added annotations for override,
	 * and protected changed to public
	 */
	private static final String SP_FETCHES = "\r\n" + "	/**\r\n"
			+ "	 * to be invoked only after a successful invocation of callSp(). This will\r\n"
			+ "	 * return the update counts from the stored procedure\r\n" + "	 *\r\n"
			+ "	 * @return array of dataTables that contain the output rows from each of the\r\n"
			+ "	 *         results. An array element is null if the corresponding result did\r\n"
			+ "	 *         not have a resultSte (but an updateCount instead) The array is\r\n"
			+ "	 *         null if the stored procedure did not succeed. The array\r\n"
			+ "	 *         DataTables correspond to the Records that re specified as output\r\n"
			+ "	 *         parameters in the SQL specification.\r\n"
			+ "	public DataTable<?>[] fetchResultsTable() {\r\n" + "		return this.resultTables;\r\n" + "	}\r\n"
			+ "\r\n" + "	/**\r\n"
			+ "	 * to be invoked only after a successful invocation of callSp(). This will\r\n"
			+ "	 * return the update counts from the stored procedure\r\n" + "	 *\r\n"
			+ "	 * @return array of integers that represent the updateCounts of each result\r\n"
			+ "	 *         of this stored procedure. -1 implies that this result did not\r\n"
			+ "	 *         produce an updateCOunt (but a resultSet)\r\n" + "	 *\r\n"
			+ "	 *         null if the stored procedure was not invoked or the stored\r\n"
			+ "	 *         procedure has no results\r\n" + "	 */\r\n" + "	public int[] fetchUpdateCounts() {\r\n"
			+ "		return this.updateCounts;\r\n" + "	}\r\n" + "";
	private static final String END_FAIL_COMMENT = "\n\t * @throws SQLException if no rows read/affected, or on any DB related error\n\t */";
	private static final String END_COMMENT = "\n\t * @throws SQLException\n\t */";

	/**
	 * methods
	 */
	private static final String BEGIN_METHOD = "\n\tpublic ";
	private static final String BEGIN_OVERRIDE_METHOD = "\n\t@Override\n\tpublic ";
	private static final String SUPER_VOID = "\n\t\t\t super.";
	private static final String SUPER_RETURN = "\n\t\t\t return super.";
	private static final String SQL_EX = ") throws SQLException {";
	private static final String END_METHOD = "\n\t}";

	/*
	 * attributes loaded from meta-data
	 */
	String name;
	String description;
	String sqlType;
	String readFrom;
	String sql;

	InputField[] inputFields;
	String inputRecord;
	OutputField[] outputFields;
	String outputRecord;
	/*
	 * additional attributes for a Stored Procedure
	 */
	String procedureName;
	String returnType;
	String[] resultRecords;
	/**
	 * calculated attributes
	 */
	private String descriptionToUse;
	private boolean hasDate = false;
	private boolean hasTime = false;
	private String thisClassName; // of the generated class for this SQL

	private String inRecClassName; // simple name, not fully qualified
	private Field[] inRecFields; // records from the specified inputRecord

	private String outRecClassName; // simple, not fully qualified
	private ValueType[] outputTypes;
	private String[] dbNames; // input parameter names as used in the data base

	private String preparedSql; // that is ready to be assigned to super.sqlText
	private ValueType returnValueType; // parsed from returnType
	private String spOutputClassNames[];

	/*
	 * Conveniently named booleans for various conditions
	 */

	/**
	 * expects one row of output data
	 */
	private boolean isToReadOne;

	/**
	 * expects many rows of output data
	 */
	private boolean isToReadMany;

	/**
	 * either of the above two
	 */
	private boolean isToRead;

	/**
	 * update/insert/delete
	 */
	private boolean isToWrite;

	/**
	 * update/insert/delete
	 */
	private boolean isToCall;

	/**
	 * sql has input parameters
	 */
	private boolean hasInput;

	/**
	 * sql expects output fields
	 */
	private boolean hasOutput;

	/**
	 * uses a stored procedure, not a sql
	 */
	private boolean isSp;

	private List<String> msgs = new ArrayList<>();

	/**
	 * to be called by the generator before generating java/ts code
	 *
	 * @param schemas
	 */
	void init(Map<String, ValueSchema> schemas, Map<String, Record> records) {

		/*
		 * what is the purpose of this sql?
		 */
		this.isToReadOne = this.sqlType.equals(SQL_TYPE_READ_ONE);
		this.isToReadMany = this.sqlType.equals(SQL_TYPE_READ_MANY);
		this.isToRead = this.isToReadOne || this.isToReadMany;
		this.isToWrite = this.sqlType.equals(SQL_TYPE_WRITE);
		this.isToCall = this.sqlType.equals(SQL_TYPE_CALL);

		this.hasInput = this.inputFields != null || this.inputRecord != null;
		this.hasOutput = this.outputFields != null || this.outputRecord != null;
		this.isSp = this.procedureName != null;

		this.thisClassName = Util.toClassName(this.name) + "Sql";

		/**
		 * validate attributes
		 */
		if (this.sql == null) {
			if (this.isSp == false) {
				this.msgs.add("No sql specified");
				this.sql = "";
			}
		}

		if (this.inputFields != null) {
			this.initArr(this.inputFields, schemas);
		}

		if (this.inputRecord != null) {
			Record inRec = records.get(this.inputRecord);
			if (inRec == null) {
				this.msgs.add(this.inputRecord + " is specified as inputRecord, but it is not defined as a record");
			} else {
				this.inRecClassName = Util.toClassName(this.inputRecord) + "Record";
				this.inRecFields = inRec.fields;
			}
		}

		if (this.outputFields != null) {
			this.initArr(this.outputFields, schemas);
			this.setDbNames(this.outputFields);
		}

		if (this.outputRecord != null) {
			Record outRec = records.get(this.outputRecord);
			if (outRec == null) {
				this.msgs.add(this.outputRecord

						+ " is specified as outputRecord, but it is not defined as a record");
			} else {
				this.outRecClassName = Util.toClassName(this.outputRecord) + "Record";
				this.setDbNames(outRec.fields);
			}
		}

		if (this.returnType != null) {
			try {
				this.returnValueType = ValueType.valueOf(Util.toClassName(this.returnType));

			} catch (Exception e) {
				this.msgs.add(
						"this.returnType is not a valid type. Use one of text, integer, decimal, boolean, date or timestamp");
			}
		}

		if (this.resultRecords != null) {
			this.spOutputClassNames = new String[this.resultRecords.length];
			for (int i = 0; i < this.resultRecords.length; i++) {
				String recName = this.resultRecords[i];
				if (recName == null) {
					continue;
				}

				Record rec = records.get(recName);
				if (rec == null) {
					this.msgs.add(recName

							+ " is specified as outputRecord, but it is not defined as a record");
					continue;
				}
				this.spOutputClassNames[i] = Util.toClassName(recName) + "Record";
			}
		}

		if (this.inputFields != null && this.inputRecord != null) {
			this.msgs.add(
					"Input parameters are to be specified either with inputParamaters or inputRecord, but not both");
		}

		if (this.outputFields != null && this.outputRecord != null) {
			this.msgs.add("Output from sql may be specified either with outputFields or outputRecord, but not both.");
		}

		/**
		 * description in the java-doc
		 */
		this.descriptionToUse = this.description == null ? ("Code generated from " + this.name + ".sql.json")
				: this.description;

		if (this.isToWrite) {
			this.preparedSql = this.sql;
			if (this.hasInput == false) {
				this.msgs.add("Write sql MUST have sql parameters. Unconditional update to database is not allowed");
			}
			if (this.hasOutput) {
				this.msgs.add("write sql should not specify outputFields or outputRecord");
			}

			final String sqlOperarion = this.sql.substring(0, 6).toLowerCase();
			if (sqlOperarion.equals(SQL_INSERT) == false && sqlOperarion.equals(SQL_UPDATE) == false) {
				this.msgs.add("write sql must start with either " + SQL_INSERT + " or " + SQL_UPDATE);
			}

		} else if (this.isToRead) {
			this.preparedSql = this.prepareReadSql();

			if (this.hasOutput == false) {
				this.msgs.add("read sql must specify either output fields or output record.");
			}

			if (this.readFrom == null) {
				this.msgs.add(
						"read sql must specify readFrom. This is required to synthesise the SELECT-FROM clause of the SQL");
			}

			final String sqlOperarion = this.sql.substring(0, 5).toLowerCase();
			if (sqlOperarion.equals(SQL_WHERE) == false) {
				this.msgs.add(
						"read sql must start with WHERE clause. SELECT statment will be prefixed to this by the generator.");
			}

			if (this.isToReadMany && this.outputRecord == null) {
				this.msgs.add(
						"Output from a read-many sql can be received only into a dataTable. Hence outputRecord must be specified.");
			}

		} else if (this.isSp) {

			if (this.procedureName == null) {
				this.msgs.add("procedureName, as in the DB, must be specified for a stored procedure");
			}

			if (this.sql != null) {
				this.msgs.add("Sql should not be specified for a stored procedure. Generated code will manage to ");
			}

			if (this.hasOutput && this.resultRecords != null) {
				this.msgs.add(
						"Stored procedure can return more than one outputs. If this procedure is producing one output, or if you want only the first output, use outputRecord or outputFields. If you intend ro receive more than one outputs, use storedProcedureOutput");
			}

		} else {
			this.msgs.add(this.sqlType + " is invalid. it has to be readOne/readMany/write/storedProcedure");
		}

		if (!this.isSp) {
			if (this.returnType != null) {
				this.msgs.add("returnType can be specified only for a stored procedure");
			}
			if (this.resultRecords != null) {
				this.msgs.add("procedureOutputRecords can be specified only for a stored procedure");
			}
		}

	}

	private void setDbNames(Field[] fields) {
		this.dbNames = new String[fields.length];
		for (int i = 0; i < fields.length; i++) {
			this.dbNames[i] = fields[i].nameInDb;
		}
	}

	private void setDbNames(OutputField[] fields) {
		this.dbNames = new String[fields.length];
		for (int i = 0; i < fields.length; i++) {
			this.dbNames[i] = fields[i].nameInDb;
		}
	}

	private void initArr(IField[] fields, Map<String, ValueSchema> schemas) {
		for (int idx = 0; idx < fields.length; idx++) {
			IField f = fields[idx];
			f.init(schemas, idx);
			/*
			 * see if we have any date /time fields
			 */
			ValueType vt = f.getValueType();
			if (vt == ValueType.Date) {
				this.hasDate = true;
			} else if (vt == ValueType.Timestamp) {
				this.hasTime = true;
			}
		}
	}

	/**
	 * to be called ONLY AFTER calling init()
	 *
	 * @param folderName
	 * @param rootPackage
	 * @param records
	 */
	boolean generateJava(String folderName, final String rootPackage) {
		if (this.msgs.size() > 0) {
			for (String msg : this.msgs) {
				logger.error(msg);
			}
			return false;
		}

		final StringBuilder sbf = new StringBuilder();

		emitImports(sbf, rootPackage);

		/**
		 * class level comment. We are not putting generated date to ensure that the
		 * generated code is not changed unless the meta data is changed. this helps
		 * reducing load on the repositories
		 */
		sbf.append("\n\n/**").append("\n * ").append(this.descriptionToUse).append("\n */");
		sbf.append("\npublic class ").append(this.thisClassName).append(" extends ");
		if (this.isSp) {
			sbf.append("StoredProcedure {");
		} else {
			sbf.append("Sql {");
		}

		this.emitStaticFields(sbf);
		this.emitConstructor(sbf);

		/*
		 * emit setters, but only if input fields are used. If input record is used, the
		 * values are set to the record, and not to this object instance
		 */
		if (this.inputFields != null) {
			Util.emitSettersValues(sbf, this.inputFields, "this.inputValues");
		}

		/**
		 * similarly, getters, but only if fields are used
		 */
		if (this.outputFields != null) {
			Util.emitGettersFromValues(sbf, this.outputFields, "this.parameterValues");
		}

		if (this.isToCall) {
			this.emitSpCalls(sbf);
		} else if (this.isToReadOne) {
			this.emitReadMethods(sbf);
		} else if (this.isToReadMany) {
			this.emitReadManyMethods(sbf);
		} else if (this.isToWrite) {
			this.emitWriteMethods(sbf);
		} else {
			throw new ApplicationError("Sql generator has an internal error in handling the purpose of the sql");
		}

		sbf.append("\n}\n");

		Util.writeOut(folderName + this.thisClassName + ".java", sbf.toString());
		return true;
	}

	private void emitSpCalls(StringBuilder sbf) {
		if (this.inputRecord != null) {
			this.emitCallSpWithRecord(sbf);
		} else {
			this.emitCallSpWithFields(sbf);
		}
		sbf.append(SP_FETCHES);
	}

	private void emitReadMethods(StringBuilder sbf) {
		if (this.inputRecord != null) {
			if (this.outputRecord != null) {
				this.emitReadWithRecords(sbf);
				this.emitReadFailWithRecords(sbf);
				return;
			}
			this.emitReadWithRecordAndFields(sbf);
			emitReadFailWithRecordAndFields(sbf);
			return;
		}
		if (this.outputRecord != null) {
			this.emitReadWithFieldsAndRecord(sbf);
			this.emitReadFailWithFieldsAndRecord(sbf);
			return;
		}
		emitReadWithFields(sbf);
		emitReadFailWithFields(sbf);
	}

	private void emitWriteMethods(final StringBuilder sbf) {
		if (this.inputRecord != null) {
			this.emitWriteWithRecord(sbf);
			this.emitWriteFailWithRecord(sbf);
			// record based SQL can be used for batch as well
			this.emitWriteManyWithTable(sbf);
			return;
		}

		emitWriteWithFields(sbf);
		emitWriteFailWithFields(sbf);
	}

	private void emitReadManyMethods(StringBuilder sbf) {
		if (this.inputRecord == null) {
			this.emitReadManyWithFieldsAndRecord(sbf);
			return;
		}
		this.emitReadManyWithRecords(sbf);
	}

	/**
	 * this.sql starts with WHERE. We have to prefix that with SELECT a,b,c.... FROM
	 * readFrom
	 *
	 * @return
	 */
	private String prepareReadSql() {

		final StringBuilder sbf = new StringBuilder();
		sbf.append("SELECT ");
		for (String s : this.dbNames) {
			sbf.append(s).append(',');
		}
		sbf.setLength(sbf.length() - 1);

		sbf.append(" FROM ").append(this.readFrom).append(' ').append(this.sql);

		return sbf.toString() + this.sql;
	}

	private void emitImports(final StringBuilder sbf, final String rootPackage) {
		sbf.append("package ").append(rootPackage).append(".sql;\n");

		if (this.hasDate) {
			Util.emitImport(sbf, LocalDate.class);
		}
		if (this.hasTime) {
			Util.emitImport(sbf, Instant.class);
		}
		Util.emitImport(sbf, SQLException.class);

		Util.emitImport(sbf, ValueType.class);
		if (this.inputFields != null) {
			Util.emitImport(sbf, org.simplity.fm.core.data.Field.class);
			sbf.append("\nimport ").append(rootPackage).append('.')
					.append(Conventions.App.GENERATED_VALUE_SCHEMAS_CLASS_NAME).append(';');
		}

		if (this.isToRead) {
			Util.emitImport(sbf, IReadonlyHandle.class);
		} else {
			Util.emitImport(sbf, IReadWriteHandle.class);
		}
		/**
		 * table is required for batch-write and readMany
		 */
		if ((this.resultRecords != null) || (this.inputRecord != null && this.isToWrite)
				|| (this.outputRecord != null && this.isToReadMany)) {
			Util.emitImport(sbf, org.simplity.fm.core.data.DataTable.class);
		}

		if (this.isSp) {
			Util.emitImport(sbf, org.simplity.fm.core.db.StoredProcedure.class);
		} else {
			Util.emitImport(sbf, org.simplity.fm.core.db.Sql.class);
		}

		/**
		 * Records used in the SQL
		 */
		importRec(sbf, rootPackage, this.inRecClassName);
		importRec(sbf, rootPackage, this.outRecClassName);
		/**
		 * what about the records for SP?
		 */
		if (this.spOutputClassNames != null) {
			for (String className : this.spOutputClassNames) {
				importRec(sbf, rootPackage, className);
			}
		}
	}

	private static void importRec(StringBuilder sbf, String rootPackage, String className) {
		if (className == null) {
			return;
		}
		sbf.append("\nimport ").append(rootPackage).append(".rec.").append(className).append(';');
	}

	private void emitStaticFields(final StringBuilder sbf) {
		if (this.isSp) {
			sbf.append(P).append("String PROC_NAME = \"").append(this.procedureName).append("\";");
			sbf.append(P).append("ValueType RET_TYPE = ").append(this.returnValueType).append(';');
			sbf.append(P).append("Class<?>[] SP_OUT_CLASSES = ");
			emitClassArray(sbf, this.spOutputClassNames);
		}

		sbf.append(P).append("String SQL = ").append(Util.quotedString(this.preparedSql)).append(";");

		sbf.append(P).append("Field[] IN_FIELDS = ");

		if (this.inputFields != null) {
			emitFieldArray(this.inputFields, sbf);
		} else if (this.inputRecord != null) {
			emitFieldArray(this.inRecFields, sbf);
		} else {
			sbf.append("null;");
		}

		sbf.append(P).append("ValueType[] OUT_TYPES = ");
		if (this.outputFields == null) {
			sbf.append("null");
		} else {
			emitTypesArray(this.outputTypes, sbf);
		}
		sbf.append(";");

	}

	private static void emitFieldArray(Field[] fields, StringBuilder sbf) {
		sbf.append("new Field[]{");
		boolean isFirst = true;
		for (Field field : fields) {
			if (isFirst) {
				isFirst = false;
			} else {
				sbf.append(", ");
			}
			sbf.append("new Field(\"").append(field.name).append("\", ").append(field.index).append(", ")
					.append(Conventions.App.GENERATED_VALUE_SCHEMAS_CLASS_NAME).append('.').append(field.valueSchema)
					.append(", ").append(field.isRequired).append(", ").append(Util.quotedString(field.defaultValue))
					.append(")");
		}

		sbf.append("};");

	}

	private static void emitFieldArray(InputField[] fields, StringBuilder sbf) {
		sbf.append("new Field[]{");
		boolean isFirst = true;
		for (InputField field : fields) {
			if (isFirst) {
				isFirst = false;
			} else {
				sbf.append(", ");
			}
			sbf.append("new Field(\"").append(field.name).append("\", ").append(field.index).append(", ")
					.append(Conventions.App.GENERATED_VALUE_SCHEMAS_CLASS_NAME).append('.').append(field.valueSchema)
					.append(", ").append(field.isRequired).append(", ").append(Util.quotedString(field.defaultValue))
					.append(")");
		}

		sbf.append("};");
	}

	private static void emitTypesArray(final ValueType[] types, final StringBuilder sbf) {
		sbf.append("new ValueType[]{");
		boolean firstOne = true;
		for (final ValueType type : types) {
			if (firstOne) {
				firstOne = false;
			} else {
				sbf.append(", ");
			}
			sbf.append("ValueType.").append(type.name());
		}
		sbf.append('}');
	}

	private static void emitClassArray(StringBuilder sbf, String[] names) {
		if (names == null) {
			sbf.append("null;");
			return;
		}
		sbf.append("new Class<?>[]{");
		for (String name : names) {
			if (name == null) {
				sbf.append("null,");
			} else {
				sbf.append(Util.toClassName(name)).append(".class").append(",");
			}
		}
		sbf.setLength(sbf.length() - 1);
		sbf.append("};");
	}

	private void emitConstructor(final StringBuilder sbf) {
		sbf.append("\n\n\t/** \n\t * default constructor\n\t */\n\tpublic ").append(this.thisClassName).append("() {");
		if (this.isSp) {
			sbf.append("\n\t\tsuper(PROC_NAME, RET_TYPE, SP_OUT_CLASSES, IN_FIELDS, OUT_TYPES);");
		} else {
			sbf.append("\n\t\tsuper(SQL, IN_FIELDS, OUT_TYPES);");
		}
		sbf.append("\n\t}");
	}

	private void emitCallSpWithFields(final StringBuilder sbf) {
		sbf.append(BEGIN_COMMENT);
		sbf.append(COMMENT_SP);
		sbf.append(COMMENT_PREFIX).append(DESCRIPTION_SETTERS);
		sbf.append(HANDLE_PARAM);
		sbf.append(SP_RETURN);

		sbf.append(BEGIN_OVERRIDE_METHOD).append("Object callSp final ")
				.append(this.isToWrite ? "IReadWrite" : "IReadonly").append("handle").append(SQL_EX);
		sbf.append(SUPER_RETURN).append("callSp(handle);");
		sbf.append(END_METHOD);
	}

	private void emitCallSpWithRecord(final StringBuilder sbf) {
		sbf.append(BEGIN_COMMENT);
		sbf.append(COMMENT_SP);
		sbf.append(COMMENT_PREFIX).append(DESCRIPTION_INPUT_RECORD);
		sbf.append(HANDLE_PARAM);
		sbf.append(IN_REC_PARAM);
		sbf.append(SP_RETURN);

		sbf.append(BEGIN_METHOD).append("Object callSp final ").append(this.isToWrite ? "IReadWrite" : "IReadonly")
				.append("handle, ").append(this.inRecClassName).append(" inputRecord").append(SQL_EX);
		sbf.append(SUPER_RETURN).append("callSp(handle, inputRecord);");
		sbf.append(END_METHOD);
	}

	private void emitReadWithFieldsAndRecord(final StringBuilder sbf) {

		sbf.append(BEGIN_COMMENT);
		sbf.append(COMMENT_PREFIX).append(DESCRIPTION_SETTERS).append(DESCRIPTION_OUTPUT_RECORD);
		sbf.append(HANDLE_PARAM);
		sbf.append(OUT_REC_PARAM);
		sbf.append(BOOL_RETURN);
		sbf.append(END_COMMENT);

		sbf.append(BEGIN_METHOD).append("boolean read(final IReadonlyHandle handle, final ")
				.append(this.outRecClassName).append(" outputRecord").append(SQL_EX);
		sbf.append(SUPER_RETURN).append("read(handle, outputRecord);");
		sbf.append(END_METHOD);
	}

	private void emitReadFailWithFieldsAndRecord(final StringBuilder sbf) {
		sbf.append(BEGIN_COMMENT);
		sbf.append(COMMENT_PREFIX).append(DESCRIPTION_SETTERS).append(DESCRIPTION_OUTPUT_RECORD);

		sbf.append(HANDLE_PARAM);
		sbf.append(OUT_REC_PARAM);
		sbf.append(END_FAIL_COMMENT);
		sbf.append(BEGIN_METHOD).append("void readOrFail(final IReadonlyHandle handle, final ")
				.append(this.outRecClassName).append(" outputRecord").append(SQL_EX);
		sbf.append(SUPER_VOID).append("readOrFail(handle, outputRecord);");
		sbf.append(END_METHOD);
	}

	private void emitReadManyWithFieldsAndRecord(final StringBuilder sbf) {
		sbf.append(BEGIN_COMMENT);
		sbf.append(COMMENT_PREFIX).append(DESCRIPTION_SETTERS).append(DESCRIPTION_OUTPUT_TABLE);
		sbf.append(HANDLE_PARAM);
		sbf.append(OUT_TABLE_PARAM);
		sbf.append(END_COMMENT);
		sbf.append(BEGIN_METHOD).append("void readMany(final IReadonlyHandle handle, final DataTable<")
				.append(this.outRecClassName).append("> dataTable").append(SQL_EX);
		sbf.append(SUPER_VOID).append("readMany(handle, dataTable);");
		sbf.append(END_METHOD);
	}

	private void emitReadWithRecords(final StringBuilder sbf) {
		sbf.append(BEGIN_COMMENT);
		sbf.append(COMMENT_PREFIX).append(DESCRIPTION_INPUT_RECORD).append(DESCRIPTION_OUTPUT_RECORD);
		sbf.append(HANDLE_PARAM);
		sbf.append(IN_REC_PARAM);
		sbf.append(OUT_REC_PARAM);
		sbf.append(BOOL_RETURN);
		sbf.append(BEGIN_METHOD).append("boolean read(final IReadonlyHandle handle, final ").append(this.inRecClassName)
				.append(" inputRecord, final ").append(this.outRecClassName).append(" outputRecord").append(SQL_EX);
		sbf.append(SUPER_RETURN).append("read(handle, inputRecord, outputRecord);");
		sbf.append(END_METHOD);
	}

	private void emitReadFailWithRecords(final StringBuilder sbf) {
		sbf.append(BEGIN_COMMENT);
		sbf.append(COMMENT_PREFIX).append(DESCRIPTION_INPUT_RECORD).append(DESCRIPTION_OUTPUT_RECORD);
		sbf.append(HANDLE_PARAM);
		sbf.append(IN_REC_PARAM);
		sbf.append(OUT_REC_PARAM);
		sbf.append(END_FAIL_COMMENT);
		sbf.append(BEGIN_METHOD).append("void readOrFail(final IReadonlyHandle handle, final ")
				.append(this.inRecClassName).append(" inputRecord, ").append(this.outRecClassName)
				.append(" outputputRecord").append(SQL_EX);
		sbf.append(SUPER_VOID).append("readOrFail(handle, inputRecord, outputRecord);");
		sbf.append(END_METHOD);
	}

	private void emitReadManyWithRecords(final StringBuilder sbf) {
		sbf.append(BEGIN_COMMENT);
		sbf.append(COMMENT_PREFIX).append(DESCRIPTION_INPUT_RECORD).append(DESCRIPTION_OUTPUT_TABLE);
		sbf.append(HANDLE_PARAM);
		sbf.append(IN_REC_PARAM);
		sbf.append(OUT_TABLE_PARAM);
		sbf.append(BEGIN_METHOD).append("void readMany(final IReadonlyHandle handle, final").append(this.inRecClassName)
				.append(" inputRecord, final DataTable<").append(this.outRecClassName).append("> dataTable")
				.append(SQL_EX);
		sbf.append(SUPER_VOID).append("read(handle, inputRecord, dataTable);");
		sbf.append(END_METHOD);
	}

	private void emitReadWithRecordAndFields(final StringBuilder sbf) {
		sbf.append(BEGIN_COMMENT);
		sbf.append(COMMENT_PREFIX).append(DESCRIPTION_INPUT_RECORD).append(DESCRIPTION_GETTERS);
		sbf.append(HANDLE_PARAM);
		sbf.append(IN_REC_PARAM);
		sbf.append(BOOL_RETURN);
		sbf.append(END_COMMENT);
		sbf.append(BEGIN_METHOD).append("boolean readIn(final IReadonlyHandle handle, final ")
				.append(this.inRecClassName).append(" inputRecord").append(SQL_EX);
		sbf.append(SUPER_RETURN).append("readIn(handle, inputRecord);");
		sbf.append(END_METHOD);
	}

	private static void emitReadFailWithRecordAndFields(final StringBuilder sbf) {
		sbf.append(BEGIN_COMMENT);
		sbf.append(COMMENT_PREFIX).append(DESCRIPTION_INPUT_RECORD).append(DESCRIPTION_GETTERS);
		sbf.append(HANDLE_PARAM);
		sbf.append(IN_REC_PARAM);
		sbf.append(END_FAIL_COMMENT);
		sbf.append(BEGIN_METHOD).append("void readInOrFail(final IReadonlyHandle handle").append(SQL_EX);
		sbf.append(SUPER_VOID).append("readInOrFail(handle);");
		sbf.append(END_METHOD);
	}

	private static void emitReadWithFields(final StringBuilder sbf) {
		sbf.append(BEGIN_COMMENT);
		sbf.append(COMMENT_PREFIX).append(DESCRIPTION_SETTERS).append(DESCRIPTION_GETTERS);
		sbf.append(HANDLE_PARAM);
		sbf.append(BOOL_RETURN);
		sbf.append(END_COMMENT);
		sbf.append(BEGIN_OVERRIDE_METHOD).append("int readIn(final IReadonlyHandle handle").append(SQL_EX);
		sbf.append(SUPER_RETURN).append("readIn(handle);");
		sbf.append(END_METHOD);
	}

	private static void emitReadFailWithFields(final StringBuilder sbf) {
		sbf.append(BEGIN_COMMENT);
		sbf.append(COMMENT_PREFIX).append(DESCRIPTION_SETTERS).append(DESCRIPTION_GETTERS);
		sbf.append(HANDLE_PARAM);
		sbf.append(END_FAIL_COMMENT);
		sbf.append(BEGIN_OVERRIDE_METHOD).append("void readInOrFail(final IReadonlyHandle handle").append(SQL_EX);
		sbf.append(SUPER_VOID).append("readInOrFail(handle);");
		sbf.append(END_METHOD);
	}

	private static void emitWriteWithFields(final StringBuilder sbf) {
		sbf.append(BEGIN_COMMENT);
		sbf.append(COMMENT_PREFIX).append(DESCRIPTION_SETTERS);
		sbf.append(HANDLE_PARAM);
		sbf.append(INT_RETURN);
		sbf.append(END_COMMENT);
		sbf.append(BEGIN_OVERRIDE_METHOD).append("int write(final IReadWriteHandle handle").append(SQL_EX);
		sbf.append(SUPER_RETURN).append("write(handle);");
		sbf.append(END_METHOD);
	}

	private static void emitWriteFailWithFields(final StringBuilder sbf) {
		sbf.append(BEGIN_COMMENT);
		sbf.append(COMMENT_PREFIX).append(DESCRIPTION_SETTERS);
		sbf.append(HANDLE_PARAM);
		sbf.append(NON_ZERO_RETURN);
		sbf.append(END_FAIL_COMMENT);
		sbf.append(BEGIN_OVERRIDE_METHOD).append("int writeOrFail(final IReadWriteHandle handle").append(SQL_EX);
		sbf.append(SUPER_RETURN).append("write(handle);");
		sbf.append(END_METHOD);
	}

	private void emitWriteWithRecord(final StringBuilder sbf) {
		sbf.append(BEGIN_COMMENT);
		sbf.append(COMMENT_PREFIX).append(DESCRIPTION_INPUT_RECORD);
		sbf.append(HANDLE_PARAM);
		sbf.append(IN_REC_PARAM);
		sbf.append(INT_RETURN);
		sbf.append(END_COMMENT);
		sbf.append(BEGIN_METHOD).append("int write(final IReadWriteHandle handle, ").append(this.inRecClassName)
				.append(" inputRecord").append(SQL_EX);
		sbf.append(SUPER_RETURN).append("write(handle, inputRecord);");
		sbf.append(END_METHOD);
	}

	private void emitWriteFailWithRecord(final StringBuilder sbf) {
		sbf.append(BEGIN_COMMENT);
		sbf.append(COMMENT_PREFIX).append(DESCRIPTION_INPUT_RECORD);
		sbf.append(HANDLE_PARAM);
		sbf.append(IN_REC_PARAM);
		sbf.append(NON_ZERO_RETURN);
		sbf.append(END_FAIL_COMMENT);
		sbf.append(BEGIN_METHOD).append("int writeOrFail(final IReadWriteHandle handle, ").append(this.inRecClassName)
				.append(" inputRecord").append(SQL_EX);
		sbf.append(SUPER_RETURN).append("writeOrFail(handle, inputRecord);");
		sbf.append(END_METHOD);
	}

	private void emitWriteManyWithTable(final StringBuilder sbf) {
		sbf.append(BEGIN_COMMENT);
		sbf.append(COMMENT_PREFIX).append(DESCRIPTION_INPUT_TABLE);
		sbf.append(HANDLE_PARAM);
		sbf.append(IN_TABLE_PARAM);
		sbf.append(INT_RETURN);
		sbf.append(END_COMMENT);
		sbf.append(BEGIN_METHOD).append("int writeMany(final IReadWriteHandle handle, DataTable<")
				.append(this.inRecClassName).append("> table").append(SQL_EX);
		sbf.append(SUPER_RETURN).append("writeMany(handle, table);");
		sbf.append(END_METHOD);
	}

}
