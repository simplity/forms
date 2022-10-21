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

import org.simplity.fm.core.app.App;
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

	String name;
	String recordName;
	/*
	 * used only for the client as of now. We are worried that careless
	 * programmers may expose services by mistake. Hence we insist that any
	 * service that can be served to guests MUST be hand-coded
	 *
	 */
	boolean serveGuests;
	String[] operations;
	Map<String, Control> controls;
	LinkedForm[] linkedForms;
	// Section[] sections;

	/*
	 * derived attributes
	 */
	Map<String, Field> fields;
	Record record;

	final Set<String> keyFieldNames = new HashSet<>();

	Field[] keyFields;

	void initialize(final Record rec) {
		this.fields = new HashMap<>();

		this.record = rec;
		for (final Field f : rec.fieldsMap.values()) {
			final FieldType ct = f.fieldTypeEnum;
			if (ct == FieldType.PrimaryKey
					|| ct == FieldType.GeneratedPrimaryKey) {
				this.keyFieldNames.add(f.name);
			}
		}

		this.fields.putAll(rec.fieldsMap);

		if (this.linkedForms != null) {
			int idx = 0;
			for (final LinkedForm lf : this.linkedForms) {
				lf.index = idx;
				idx++;
			}
		}
		if (this.controls != null) {
			Util.initializeMapEntries(this.controls);
		}
	}

	void generateJava(final String folderName, final String packageName) {
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
		Util.emitImport(sbf, App.class);
		Util.emitImport(sbf, org.simplity.fm.core.data.Form.class);
		Util.emitImport(sbf, org.simplity.fm.core.data.LinkMetaData.class);
		Util.emitImport(sbf, org.simplity.fm.core.data.LinkedForm.class);
		final String recordClass = Util.toClassName(this.recordName) + "Record";
		sbf.append("\nimport ").append(packageName).append(".rec.")
				.append(recordClass).append(';');

		final String cls = Util.toClassName(this.name) + "Form";
		/*
		 * class declaration
		 */
		sbf.append("\n/** class for form ").append(this.name)
				.append("  */\npublic class ");
		sbf.append(cls).append(" extends Form<").append(recordClass)
				.append("> {");

		final String p = "\n\tprotected static final ";

		/*
		 * protected static final Field[] FIELDS = {.....};
		 */
		sbf.append(p).append("String NAME = \"").append(this.name)
				.append("\";");
		/*
		 * protected static final String RECORD = "....";
		 */
		sbf.append(p).append(recordClass).append(" RECORD = (")
				.append(recordClass);
		sbf.append(") App.getApp().getCompProvider().getRecord(\"")
				.append(this.recordName).append("\");");

		/*
		 * protected static final boolean[] OPS = {true, false,..};
		 */
		sbf.append(p);
		getOps(this.operations, sbf);

		/*
		 * linked forms
		 */
		final String lf = "\n\tprivate static final LinkedForm<?>[] LINKS = ";
		if (this.linkedForms == null) {
			sbf.append(lf).append("null;");
		} else {
			final StringBuilder bf = new StringBuilder();
			for (int i = 0; i < this.linkedForms.length; i++) {
				/*
				 * declare linkedMeta and Form
				 */
				this.linkedForms[i].emitJavaCode(sbf, this.fields, i);

				if (i != 0) {
					bf.append(',');
				}
				bf.append("new LinkedForm<>(L").append(i).append(", F")
						.append(i).append(')');
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

		Util.writeOut(folderName + cls + ".java", sbf);
	}

	private static final Map<String, Integer> OP_INDEXES = getOpIndexes();

	static void getOps(final String[] dbOps, final StringBuilder sbf) {
		final IoType[] types = IoType.values();
		final boolean[] ops = new boolean[types.length];
		if (dbOps != null) {

			for (final String op : dbOps) {
				final Integer idx = OP_INDEXES.get(op.toLowerCase());
				if (idx == null) {
					logger.error(
							"{} is not a valid db operation (IoType). Ignored.");
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

}
