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
import java.util.Map;

import org.simplity.fm.core.Conventions;
import org.simplity.fm.core.IValueSchemas;
import org.simplity.fm.core.valueschema.BooleanSchema;
import org.simplity.fm.core.valueschema.DateSchema;
import org.simplity.fm.core.valueschema.DecimalSchema;
import org.simplity.fm.core.valueschema.IntegerSchema;
import org.simplity.fm.core.valueschema.TextSchema;
import org.simplity.fm.core.valueschema.TimestampSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Design Considerations:
 * Problem on hand:
 * Generate desired Java classes and TypeScript files for the meta data specified for this app.
 *
 * Approach:
 * Meta data is in Json files. We can either read them as Json Objects, or create data structures/classes.
 * We decided to use classes with matching data-structures for ease of reading/loading.
 *
 * Source code generation is lengthy, but not complex. This is probably reflected in our design as well.
 * Lengthy methods with heavily hard-coded strings.
 *
 * Since this is a fairly focused, non-generic code, we have used package-private attributes and avoided setters/getters
 */
/**
 * attributes read from application.json are output as generated sources
 *
 * @author simplity.org
 *
 */
public class Application {
	private static final Logger logger = LoggerFactory
			.getLogger(Application.class);
	private static final String C = ", ";

	/**
	 * if a text field's length is less than this, it is rendered as text-field,
	 * else as text-area
	 */
	public static int TEXT_AREA_CUTOFF = 199;

	String appName;
	int maxLengthForTextField = 199;
	Map<String, ValueSchema> valueSchemas;
	Map<String, ValueList> valueLists;

	/**
	 * must be called after loading, and before using it
	 */
	public void initialize() {
		if (this.maxLengthForTextField > 0) {
			TEXT_AREA_CUTOFF = this.maxLengthForTextField;
		}
		Util.initializeMapEntries(this.valueSchemas);
		Util.initializeMapEntries(this.valueLists);
	}
	/**
	 * generate java classes for value schemas and value lists
	 *
	 * @param rootFolder
	 *            source folder of the project where code is to be generated
	 * @param packageName
	 *            fully qualified package name like a.b.c
	 * @return true if all ok, false otherwise
	 */
	public boolean generateJava(final String rootFolder,
			final String packageName) {
		this.javaSchemas(rootFolder, packageName);
		this.javaLists(rootFolder, packageName);

		return true;
	}

	private void javaSchemas(final String rootFolder,
			final String packageName) {

		/*
		 * create DataTypes.java in the root folder.
		 */
		final StringBuilder sbf = new StringBuilder();
		sbf.append("package ").append(packageName).append(';');
		sbf.append('\n');

		Util.emitImport(sbf, HashMap.class);
		Util.emitImport(sbf, Map.class);
		sbf.append("\n");

		Util.emitImport(sbf, IValueSchemas.class);
		Util.emitImport(sbf, ValueSchema.class);
		Util.emitImport(sbf, TextSchema.class);
		Util.emitImport(sbf, IntegerSchema.class);
		Util.emitImport(sbf, DecimalSchema.class);
		Util.emitImport(sbf, BooleanSchema.class);
		Util.emitImport(sbf, DateSchema.class);
		Util.emitImport(sbf, TimestampSchema.class);

		final String clsName = Conventions.App.GENERATED_DATA_TYPES_CLASS_NAME;

		sbf.append(
				"\n\n/**\n * class that has static attributes for all value schemas defined for this project. It also extends <code>DataTypes</code>");
		sbf.append("\n */ ");
		sbf.append("\npublic class ").append(clsName)
				.append(" implements IValueSchemas {");

		if (this.valueSchemas == null) {
			this.valueSchemas = new HashMap<>();
		}

		final StringBuilder schemaNames = new StringBuilder();
		for (final ValueSchema vs : this.valueSchemas.values()) {
			vs.emitJava(sbf);
			schemaNames.append(vs.name).append(C);
		}

		if (schemaNames.length() > 0) {
			schemaNames.setLength(schemaNames.length() - C.length());
		}

		sbf.append("\n\n\tpublic static final DataType[] allTypes = {")
				.append(schemaNames.toString()).append("};");

		sbf.append("\n\t private Map<String, DataType> typesMap;");

		sbf.append("\n\t/**\n\t * default constructor\n\t */");

		sbf.append("\n\tpublic ").append(clsName).append("() {");
		sbf.append("\n\t\tthis.typesMap = new HashMap<>();");
		sbf.append("\n\t\tfor(DataType dt: allTypes) {");
		sbf.append("\n\t\t\tthis.typesMap.put(dt.getName(), dt);");
		sbf.append("\n\t\t}\n\t}");

		sbf.append("\n\n@Override");
		sbf.append("\n\tpublic DataType getDataType(String typeName) {");
		sbf.append("\n\t\treturn this.typesMap.get(typeName);");
		sbf.append("\n\t}");

		sbf.append("\n}\n");

		Util.writeOut(rootFolder + clsName + ".java", sbf.toString());
	}

	private void javaLists(final String rootFolder, final String packageName) {
		final String pck = packageName + ".list";
		final String folder = rootFolder + "list/";

		/**
		 * lists are created under list sub-package
		 */
		if (this.valueLists == null || this.valueLists.size() == 0) {
			logger.warn("No value lists created for this project");
			return;
		}
		for (final ValueList list : this.valueLists.values()) {
			list.generateJava(folder, pck);
		}
	}

	/**
	 * generate TypeScript code
	 *
	 * @param folder
	 *            client-side source folder (typically src/)
	 * @return true if allOK, false otherwise
	 */
	public boolean generateTs(final String folder) {

		this.generateTsForLists(folder);
		this.generateTsForSchemas(folder);
		return true;
	}

	private void generateTsForSchemas(final String folder) {
		logger.info("Generatng TS code for value schemas ...");
		final StringBuilder sbf = new StringBuilder();
		sbf.append('{');

		for (ValueSchema vs : this.valueSchemas.values()) {
			vs.emitTs(sbf);
			sbf.append(',');
		}
		sbf.setLength(sbf.length() - 1);
		sbf.append("\n}\n");

		String fn = folder + "allValueSchemas.json";
		Util.writeOut(fn, sbf.toString());
		logger.info("File {} generated", fn);

	}

	private void generateTsForLists(final String folder) {
		logger.info("Generating TS code for lists...");

		StringBuilder sbf = new StringBuilder();
		sbf.append('{');

		for (ValueList list : this.valueLists.values()) {
			list.emitTs(sbf);
			sbf.append(',');
		}
		sbf.setLength(sbf.length() - 1);
		sbf.append("\n}\n");
		Util.writeOut(folder + "allListSources.json", sbf.toString());
		logger.info("TS code for {} lists generated", this.valueLists.size());
	}
}
