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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.simplity.fm.core.app.AppManager;
import org.simplity.fm.core.data.FieldType;
import org.simplity.fm.core.data.IoType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author simplity.org
 *
 */
public class Form {
	protected static final Logger logger = LoggerFactory.getLogger(Form.class);

	/**
	 * create a default form for a record
	 *
	 * @param rec
	 * @return
	 */
	static Form fromRecord(Record rec) {
		Form form = new Form();
		form.name = rec.name;
		form.initialize(rec);
		return form;
	}

	String name;
	String mainRecordName;
	boolean serveGuests;
	String[] operations;
	ChildForm[] childForms;

	// derived fields
	Record record;

	final Set<String> keyFieldNames = new HashSet<>();

	boolean gotErrors;

	void initialize(final Record rec) {
		this.record = rec;
		this.gotErrors = rec.gotErrors;
		this.mainRecordName = rec.name;

		if (this.operations == null) {
			this.operations = rec.operations;
		}

		if (rec.keyFields != null) {
			for (final Field f : rec.keyFields) {
				final FieldType ct = f.fieldTypeEnum;
				if (ct == FieldType.PrimaryKey || ct == FieldType.GeneratedPrimaryKey) {
					this.keyFieldNames.add(f.name);
				}
			}
		}

		if (this.childForms != null) {
			int idx = 0;
			for (final ChildForm child : this.childForms) {
				child.index = idx;
				idx++;
			}
		}
	}

	boolean generateJava(final String folderName, final String packageName) {
		if (this.gotErrors) {
			logger.error("Record {} is in error. Java Code for Form {} NOT generated", this.mainRecordName, this.name);
			return false;
		}
		final StringBuilder sbf = new StringBuilder();
		/*
		 * our package name is rootPackage + any prefix/qualifier in our name
		 *
		 * e.g. if name a.b.record1 then prefix is a.b and className is Record1
		 */
		String pck = packageName + ".form";
		final String qual = Util.getClassQualifier(this.name);
		if (qual != null) {
			pck += '.' + qual;
		}
		sbf.append("package ").append(pck).append(";\n");
		Util.emitImport(sbf, AppManager.class);
		Util.emitImport(sbf, org.simplity.fm.core.data.Form.class);
		Util.emitImport(sbf, org.simplity.fm.core.data.ChildForm.class);
		Util.emitImport(sbf, org.simplity.fm.core.data.ChildMetaData.class);
		final String recordClass = Util.toClassName(this.mainRecordName) + "Record";
		sbf.append("\nimport ").append(packageName).append(".rec.").append(recordClass).append(';');

		final String cls = Util.toClassName(this.name) + "Form";
		/*
		 * class declaration
		 */
		sbf.append("\n/** class for form ").append(this.name).append("  */\npublic class ");
		sbf.append(cls).append(" extends Form<").append(recordClass).append("> {");

		final String p = "\n\tprotected static final ";

		/*
		 * protected static final Field[] FIELDS = {.....};
		 */
		sbf.append(p).append("String NAME = \"").append(this.name).append("\";");
		/*
		 * protected static final String RECORD = "....";
		 */
		sbf.append(p).append(recordClass).append(" RECORD = (").append(recordClass);
		sbf.append(") AppManager.getAppInfra().getCompProvider().getRecord(\"").append(this.mainRecordName).append("\");");

		/*
		 * protected static final boolean[] OPS = {true, false,..};
		 */
		sbf.append(p);
		getOps(this.operations, sbf);

		/*
		 * linked forms
		 */
		final String lf = "\n\tprivate static final ChildForm<?>[] LINKS = ";
		if (this.childForms == null) {
			sbf.append(lf).append("null;");
		} else {
			final StringBuilder bf = new StringBuilder();
			for (int i = 0; i < this.childForms.length; i++) {
				/*
				 * declare linkedMeta and Form
				 */
				this.childForms[i].emitJavaCode(sbf, this.record.fieldsMap, i);

				if (i != 0) {
					bf.append(',');
				}

				bf.append("new ChildForm<>(L").append(i).append(", F").append(i).append(')');
			}
			sbf.append(lf).append('{').append(bf).append("};");
		}

		/*
		 * constructor
		 *
		 */
		sbf.append("\n/** constructor */\npublic ").append(cls).append("() {");
		sbf.append("\n\t\tsuper(NAME, RECORD, OPS, LINKS);");
		if (this.serveGuests) {
			sbf.append("\n\t\tthis.serveGuests = true;");
		}

		sbf.append("\n\t}\n}\n");

		Util.writeOut(folderName + cls + ".java", sbf.toString());
		return true;
	}

	private static final Map<String, Integer> OP_INDEXES = getOpIndexes();

	static void getOps(final String[] dbOps, final StringBuilder sbf) {
		final IoType[] types = IoType.values();
		final boolean[] ops = new boolean[types.length];
		if (dbOps != null) {

			for (final String op : dbOps) {
				final Integer idx = OP_INDEXES.get(op.toLowerCase());
				if (idx == null) {
					logger.error("{} is not a valid db operation (IoType). Ignored.");
				} else {
					ops[idx] = true;
				}
			}
		}
		sbf.append(" boolean[] OPS = {");
		boolean firstOne = true;
		for (final boolean b : ops) {
			if (firstOne) {
				firstOne = false;
			} else {
				sbf.append(", ");
			}
			sbf.append(b);
		}
		sbf.append("};");
	}

	/**
	 * @return
	 */
	private static Map<String, Integer> getOpIndexes() {
		final Map<String, Integer> indexes = new HashMap<>();
		for (final IoType iot : IoType.values()) {
			indexes.put(iot.name().toLowerCase(), iot.ordinal());
		}
		return indexes;
	}

	private static final char Q = '"';

	boolean generateTs(String folderName) {
		if (this.gotErrors) {
			logger.error("Record {} is in error. TS code for form {} not generated", this.mainRecordName, this.name);
			return false;
		}

		logger.info("TS for form {} being generated into folder {}", this.name, folderName);
		final StringBuilder sbf = new StringBuilder();
		sbf.append("export const ").append(this.name).append(" = {");
		sbf.append("\n\t\"name\": \"").append(this.mainRecordName).append("\",");
		sbf.append("\n\t\"operations\": {");
		if (this.operations == null || this.operations.length == 0) {
			logger.warn(
					"No operations are allowed for record {}. Client app will not be able to use auto-service for this record",
					this.mainRecordName);
		} else {
			for (final String oper : this.operations) {
				if (oper == null) {
					logger.error("{} is not a valid form operation. skipped", oper);
				} else {
					sbf.append("\n\t\t\"").append(oper).append("\": true,");
				}
			}
			sbf.setLength(sbf.length() - 1); // removing the last comma
		}
		sbf.append("\n\t},");

		sbf.append("\n\t\"fields\": {");
		final StringBuilder names = new StringBuilder();
		for (final Field field : this.record.fields) {
			field.emitFormTs(sbf);
			sbf.append(',');
			names.append(Q).append(field.name).append(Q).append(',');
		}
		sbf.setLength(sbf.length() - 1);
		names.setLength((names.length() - 1));
		sbf.append("\n\t},");

		sbf.append("\n\t\"fieldNames\": [").append(names.toString()).append("]");

		Field[] keys = this.record.keyFields;
		if (keys != null && (keys.length > 0)) {
			// we generally have only one key field
			sbf.append(",\n\t\"keyFields\": [\"").append(keys[0].name).append(Q);
			for (int i = 1; i < keys.length; i++) {
				sbf.append(",\"").append(keys[i].name).append(Q);
			}
			sbf.append("]");
		}

		if (this.childForms != null) {
			sbf.append(",\n\t\"childForms\": {");
			for (ChildForm cf : this.childForms) {
				sbf.append("\n\t\t\"").append(cf.linkName).append("\": {");
				cf.emitTs(sbf, "\n\t\t\t");
				sbf.append("\n\t\t},");
			}
			sbf.setLength(sbf.length() - 1);
			sbf.append("\n\t}");
		}

		sbf.append("\n}\n");

		Util.writeOut(folderName + this.name + ".form.ts", sbf.toString());

		return true;

	}

}
