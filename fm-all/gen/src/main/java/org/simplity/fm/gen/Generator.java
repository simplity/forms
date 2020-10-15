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

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import org.simplity.fm.core.Conventions;
import org.simplity.fm.gen.DataTypes.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.stream.JsonReader;

/**
 * @author simplity.org
 *
 */
public class Generator {
	protected static final Logger logger = LoggerFactory.getLogger(Generator.class);
	private static final String FOLDER = "/";

	private static final String EXT_FRM = ".frm.json";
	private static final String EXT_REC = ".rec.json";
	private static final String EXT_SQL = ".sql.json";

	/**
	 *
	 * @param args
	 * @throws Exception
	 */
	public static void main(final String[] args) throws Exception {
		if (args.length == 2) {
			generateForms(args[0], args[1]);
			return;
		}
		if (args.length == 5) {
			generate(args[0], args[1], args[2], args[3], args[4]);
			return;
		}
		System.err.println("Usage : java Generator.class resourceRootFolder tsFormFolder\n or \n"
				+ "Usage : java Generator.class resourceRootFolder generatedSourceRootFolder generatedPackageName tsImportPrefix tsOutputFolder");
	}

	/**
	 * generate *.form.ts files from records
	 *
	 * @param inputRootFolder
	 * @param outputRootFolder
	 */
	public static void generateForms(final String inputRootFolder, final String outputRootFolder) {
		String inFolder = inputRootFolder;
		if (!inputRootFolder.endsWith(FOLDER)) {
			inFolder += FOLDER;
		}

		String outFolder = outputRootFolder;
		if (!outputRootFolder.endsWith(FOLDER)) {
			outFolder += FOLDER;
		}

		final String fileName = inputRootFolder + Conventions.App.APP_FILE;
		File f = new File(fileName);
		if (f.exists() == false) {
			logger.error("project configuration file {} not found. Aborting..", fileName);
			return;
		}

		final Application app = new Application();
		try (JsonReader reader = new JsonReader(new FileReader(f))) {
			app.fromJson(reader);
		} catch (final Exception e) {
			logger.error("Exception while trying to read file {}. Error: {}", f.getPath(), e.getMessage());
			e.printStackTrace();
		}

		/*
		 * generate project level components like data types
		 */
		app.emitTsDataTypes(outFolder);
		app.emitTsLists(outFolder);
		emitMsgsTs(inFolder, outFolder);

		logger.debug("Going to process records under folder {}", inFolder);
		f = new File(inFolder + "rec/");

		if (f.exists() == false) {
			logger.error("Records folder {} not found. No records are processed", f.getPath());
			return;
		}

		ensureFolder(new File(outFolder + "forms/"));
		ensureFolder(new File(outFolder + "pages/"));
		/*
		 * builders for the declaration files
		 */
		final StringBuilder sbf = new StringBuilder();
		final StringBuilder menus = new StringBuilder(
				"import { MenuItems } from 'simplity';\nexport const generatedMenuItems: MenuItems = {");
		final StringBuilder forms = new StringBuilder("\nexport const generatedForms: Forms = {");
		final StringBuilder formsImport = new StringBuilder("import { Forms } from 'simplity';");
		final StringBuilder pages = new StringBuilder("\nexport const generatedPages: Pages = {");
		final StringBuilder pagesImport = new StringBuilder("import { Pages } from 'simplity';");

		for (final File file : f.listFiles()) {
			final String fn = file.getName();
			if (fn.endsWith(EXT_REC) == false) {
				logger.debug("Skipping non-form file {} ", fn);
				continue;
			}
			logger.info("file: {}", fn);
			logger.debug("Going to generate record " + fn);
			final Record record;
			try (final JsonReader reader = new JsonReader(new FileReader(file))) {
				record = Util.GSON.fromJson(reader, Record.class);
			} catch (final Exception e) {
				e.printStackTrace();
				logger.error("Record {} not generated. Error : {}, {}", fn, e, e.getMessage());
				continue;
			}
			final String RecordName = fn.substring(0, fn.length() - ".rec.json".length());
			if (!RecordName.equals(record.name)) {
				logger.error(
						"Fi le {} contains record named {}. It is mandatory to use record name same as the filename",
						RecordName, record.name);
				continue;
			}

			record.init(app.dataTypes.dataTypes);

			sbf.setLength(0);
			record.emitClientForm(sbf);
			String genFileName;
			if (sbf.length() > 0) {
				genFileName = outFolder + "forms/" + RecordName + ".form.ts";
				Util.writeOut(genFileName, sbf);
				logger.info("form {} generated", genFileName);
				forms.append("\n\t").append(RecordName).append(": ").append(RecordName).append("Form,");
				formsImport.append("\nimport { ").append(RecordName).append("Form } from './forms/").append(RecordName)
						.append(".form';");
			}

			if (record.generatePages == null || record.generatePages.length == 0) {
				continue;
			}

			for (final String t : record.generatePages) {
				switch (t) {
				case "list":
					sbf.setLength(0);
					record.emitListPage(sbf);
					genFileName = outFolder + "pages/" + RecordName + "-list.page.ts";
					Util.writeOut(genFileName, sbf);
					logger.info("list page {} generated", genFileName);
					/*
					 * menu for list operation
					 */
					menus.append("\n\t'").append(RecordName).append("-list': {");
					menus.append("\n\t\tid :'").append(RecordName).append("-list',");
					menus.append("\n\t\tpageName :'").append(RecordName).append("-list',");
					menus.append("\n\t\tlabel :'").append(Util.toClassName(RecordName)).append(" List',");
					menus.append("\n\t\tisHidden :false");
					menus.append("\n\t},");

					/*
					 * add the pages list
					 */
					pages.append("\n\t'").append(RecordName).append("-list': ").append(RecordName).append("List,");
					pagesImport.append("\nimport { ").append(RecordName).append("List } from './pages/")
							.append(RecordName).append("-list.page';");

					break;

				case "save":
					genFileName = outFolder + "pages/" + RecordName + "-save.page.ts";
					sbf.setLength(0);
					record.emitSavePage(sbf);
					Util.writeOut(genFileName, sbf);
					logger.info("save page {} generated", genFileName);

					/*
					 * menu entry : add and edit
					 */
					menus.append("\n\t'").append(RecordName).append("-add': {");
					menus.append("\n\t\tid :'").append(RecordName).append("-add',");
					menus.append("\n\t\tpageName :'").append(RecordName).append("-save',");
					menus.append("\n\t\tisHidden :true");
					menus.append("\n\t},");

					menus.append("\n\t'").append(RecordName).append("-edit': {");
					menus.append("\n\t\tid :'").append(RecordName).append("-edit',");
					menus.append("\n\t\tpageName :'").append(RecordName).append("-save',");
					menus.append("\n\t\tisHidden :true");
					menus.append("\n\t},");

					/*
					 * entry for this page
					 */
					pages.append("\n\t'").append(RecordName).append("-save':  ").append(RecordName).append("Save,");
					pagesImport.append("\nimport { ").append(RecordName).append("Save } from './pages/")
							.append(RecordName).append("-save.page';");
					break;
				case "view":
					sbf.setLength(0);
					record.ViewPage(sbf);
					genFileName = outFolder + "pages/" + RecordName + "-view.page.ts";
					Util.writeOut(genFileName, sbf);
					logger.info("view page {} generated", genFileName);
					/*
					 * menu for this operation
					 */
					menus.append("\n\t'").append(RecordName).append("-view': {");
					menus.append("\n\t\tid :'").append(RecordName).append("-view',");
					menus.append("\n\t\tpageName :'").append(RecordName).append("-view',");
					menus.append("\n\t\tlabel :'").append(Util.toClassName(RecordName)).append(" View',");
					menus.append("\n\t\tisHidden :true");
					menus.append("\n\t},");

					/*
					 * add to the pages list
					 */
					pages.append("\n\t'").append(RecordName).append("-view': ").append(RecordName).append("View,");
					pagesImport.append("\nimport { ").append(RecordName).append("View } from './pages/")
							.append(RecordName).append("-view.page';");

					break;

				default:
					logger.error(
							"{} is not a valid page type to generate. 'list', 'save' and 'view' are the only valid types",
							t);
				}
			}

		}

		/*
		 * write-out declaration files
		 */
		formsImport.append(forms.toString()).append("\n}\n");
		Util.writeOut(outFolder + "forms.ts", formsImport);

		pagesImport.append(pages.toString()).append("\n}\n");
		Util.writeOut(outFolder + "pages.ts", pagesImport);

		menus.setLength(menus.length() - 1);
		menus.append("\n}\n");
		Util.writeOut(outFolder + "menuItems.ts", menus);

	}

	private static void emitMsgsTs(final String inFolder, final String outFolder) {
		logger.info("Generating Messages..");
		final String fileName = inFolder + Conventions.App.MESSAGES_FILE;
		final Map<String, String> msgs = new HashMap<>();
		final File f = new File(fileName);
		if (f.exists()) {
			try (JsonReader reader = new JsonReader(new FileReader(f))) {
				Util.loadStringMap(msgs, reader);
				logger.info("{} messages read", msgs.size());
			} catch (final Exception e) {
				logger.error("Exception while trying to read file {}. Error: {}", f.getPath(), e.getMessage());
				e.printStackTrace();
				return;
			}
		} else {
			logger.error("project has not defined messages mapping in the file {}.", fileName);
		}

		final StringBuilder sbf = new StringBuilder();
		sbf.append("import { Messages } from 'simplity';");
		sbf.append("\n\nexport const allMessages: Messages = {");
		boolean isFirst = true;
		for (final Map.Entry<String, String> entry : msgs.entrySet()) {
			if (isFirst) {
				isFirst = false;
			} else {
				sbf.append(',');
			}
			sbf.append("\n\t").append(Util.escape(entry.getKey())).append(": ").append(Util.escape(entry.getValue()));
		}
		sbf.append("\n}\n");
		final String fn = outFolder + "messages.ts";
		Util.writeOut(fn, sbf);
		logger.info("Messages file {} generated", fn);
	}

	/**
	 *
	 * @param inputRootFolder
	 *            folder where application.xlsx file, and spec folder are
	 *            located. e.g.
	 * @param javaRootFolder
	 *            java source folder where the sources are to be generated
	 * @param javaRootPackage
	 *            root
	 * @param tsImportPrefix
	 *            relative path of form folder from the folder where named forms
	 *            are generated.for example ".." in case the two folders are in
	 *            the same parent folder
	 * @param tsRootFolder
	 *            folder where generated ts files are to be saved
	 */
	public static void generate(final String inputRootFolder, final String javaRootFolder, final String javaRootPackage,
			final String tsRootFolder, final String tsImportPrefix) {

		String resourceRootFolder = inputRootFolder;
		if (!inputRootFolder.endsWith(FOLDER)) {
			resourceRootFolder += FOLDER;
		}

		String generatedSourceRootFolder = javaRootFolder;
		if (!generatedSourceRootFolder.endsWith(FOLDER)) {
			generatedSourceRootFolder += FOLDER;
		}
		generatedSourceRootFolder += javaRootPackage.replace('.', '/') + FOLDER;

		/*
		 * create output folders if required
		 */
		if (createOutputFolders(generatedSourceRootFolder,
				new String[] { "rec/", "form/", "list/", "sql/" }) == false) {
			return;
		}

		/*
		 * ts folder
		 */
		if (!ensureFolder(new File(tsRootFolder))) {
			logger.error("Unable to clean/create ts root folder {}", tsRootFolder);
			return;
		}

		final String fileName = resourceRootFolder + Conventions.App.APP_FILE;
		File f = new File(fileName);
		if (f.exists() == false) {
			logger.error("project configuration file {} not found. Aborting..", fileName);
			return;
		}

		final Application app = new Application();
		try (JsonReader reader = new JsonReader(new FileReader(f))) {
			app.fromJson(reader);
		} catch (final Exception e) {
			logger.error("Exception while trying to read file {}. Error: {}", f.getPath(), e.getMessage());
			e.printStackTrace();
			return;
		}

		/*
		 * generate project level components like data types
		 */
		app.emitJava(generatedSourceRootFolder, javaRootPackage, Conventions.App.GENERATED_DATA_TYPES_CLASS_NAME);

		logger.debug("Going to process records under folder {}", resourceRootFolder);
		final Map<String, Record> recs = new HashMap<>();
		f = new File(resourceRootFolder + "rec/");
		if (f.exists() == false) {
			logger.error("Records folder {} not found. No records are processed", f.getPath());
		} else {

			for (final File file : f.listFiles()) {
				final String fn = file.getName();
				if (fn.endsWith(EXT_REC) == false) {
					logger.debug("Skipping non-record file {}", fn);
					continue;
				}

				logger.info("file: {}", fn);
				final Record record = emitRecord(file, generatedSourceRootFolder, tsRootFolder, app.dataTypes, app,
						javaRootPackage, tsImportPrefix);
				if (record != null) {
					recs.put(record.name, record);
				}
			}
		}

		logger.debug("Going to process forms under folder {}", resourceRootFolder);
		f = new File(resourceRootFolder + "form/");
		if (f.exists() == false) {
			logger.error("Forms folder {} not found. No forms are processed", f.getPath());
		} else {

			for (final File file : f.listFiles()) {
				final String fn = file.getName();
				if (fn.endsWith(EXT_FRM) == false) {
					logger.debug("Skipping non-form file {} ", fn);
					continue;
				}
				logger.info("file: {}", fn);
				emitForm(file, generatedSourceRootFolder, tsRootFolder, app.dataTypes, app, javaRootPackage,
						tsImportPrefix, recs);
			}
		}

		logger.debug("Going to process sqls under folder {}sql/", resourceRootFolder);
		f = new File(resourceRootFolder + "sql/");
		if (f.exists() == false) {
			logger.error("Sql folder {} not found. No sqls processed", f.getPath());
		} else {

			for (final File file : f.listFiles()) {
				final String fn = file.getName();
				if (fn.endsWith(EXT_SQL) == false) {
					logger.debug("Skipping non-sql file {} ", fn);
					continue;
				}
				logger.info("file: {}", fn);
				emitSql(file, generatedSourceRootFolder, app.dataTypes, javaRootPackage);
			}
		}
	}

	private static void emitForm(final File file, final String generatedSourceRootFolder, final String tsOutputFolder,
			final DataTypes dataTypes, final Application app, final String rootPackageName, final String tsImportPrefix,
			final Map<String, Record> records) {
		String fn = file.getName();
		fn = fn.substring(0, fn.length() - EXT_FRM.length());
		logger.debug("Going to generate Form " + fn);
		final Form form;
		try (final JsonReader reader = new JsonReader(new FileReader(file))) {
			form = Util.GSON.fromJson(reader, Form.class);
		} catch (final Exception e) {
			e.printStackTrace();
			logger.error("Form {} not generated. Error : {}, {}", fn, e, e.getMessage());
			return;
		}

		if (!fn.equals(form.name)) {
			logger.error("File {} contains form named {}. It is mandatory to use form name same as the filename", fn,
					form.name);
			return;
		}
		Record record = null;
		record = records.get(form.recordName);
		if (record == null) {
			logger.error("Form {} uses record {}, but that record is not defined", form.name, form.recordName);
			return;
		}
		form.initialize(record);
		final StringBuilder sbf = new StringBuilder();

		final String cls = Util.toClassName(fn);
		form.emitJavaForm(sbf, rootPackageName);
		final String outPrefix = generatedSourceRootFolder + "form/" + cls;
		Util.writeOut(outPrefix + "Form.java", sbf);

		sbf.setLength(0);
		form.emitTs(sbf, dataTypes.dataTypes, app.valueLists, app.keyedLists, tsImportPrefix);
		Util.writeOut(tsOutputFolder + fn + "Form.ts", sbf);

	}

	/**
	 * @param files
	 * @param generatedSourceRootFolder
	 * @param tsOutputFolder
	 * @param dataTypes
	 * @param app
	 * @param rootPackageName
	 * @param tsImportPrefix
	 */
	private static Record emitRecord(final File file, final String generatedSourceRootFolder,
			final String tsOutputFolder, final DataTypes dataTypes, final Application app, final String packageName,
			final String tsImportPrefix) {
		String fn = file.getName();
		fn = fn.substring(0, fn.length() - EXT_REC.length());
		logger.debug("Going to generate record " + fn);
		final Record record;
		try (final JsonReader reader = new JsonReader(new FileReader(file))) {
			record = Util.GSON.fromJson(reader, Record.class);
		} catch (final Exception e) {
			e.printStackTrace();
			logger.error("Record {} not generated. Error : {}, {}", fn, e, e.getMessage());
			return null;
		}
		if (!fn.equals(record.name)) {
			logger.error("File {} contains record named {}. It is mandatory to use record name same as the filename",
					fn, record.name);
			return null;
		}

		record.init(dataTypes.dataTypes);

		final String outNamePrefix = generatedSourceRootFolder + "rec/" + Util.toClassName(fn);
		/*
		 * Record.java
		 */
		final StringBuilder sbf = new StringBuilder();
		record.emitJavaClass(sbf, packageName, dataTypes);
		String outName = outNamePrefix + "Record.java";
		Util.writeOut(outName, sbf);

		/*
		 * dbTable.java
		 */
		sbf.setLength(0);
		record.emitJavaTableClass(sbf, packageName);
		if (sbf.length() > 0) {
			outName = outNamePrefix + "Table.java";
			Util.writeOut(outName, sbf);
		}
		return record;
	}

	private static void emitSql(final File file, final String generatedSourceRootFolder, final DataTypes dataTypes,
			final String packageName) {
		String fn = file.getName();
		fn = fn.substring(0, fn.length() - EXT_SQL.length());
		logger.debug("Going to generate Sql " + fn);
		final Sql sql;
		try (final JsonReader reader = new JsonReader(new FileReader(file))) {
			sql = Util.GSON.fromJson(reader, Sql.class);
		} catch (final Exception e) {
			e.printStackTrace();
			logger.error("Sql {} not generated. Error : {}, {}", fn, e, e.getMessage());
			return;
		}

		final String cls = Util.toClassName(fn) + "Sql";
		final StringBuilder sbf = new StringBuilder();
		sql.emitJava(sbf, packageName, cls, Conventions.App.GENERATED_DATA_TYPES_CLASS_NAME, dataTypes.dataTypes);
		final String outName = generatedSourceRootFolder + "sql/" + Util.toClassName(fn) + "Sql.java";
		Util.writeOut(outName, sbf);

	}

	private static boolean createOutputFolders(final String root, final String[] folders) {
		boolean allOk = true;
		for (final String folder : folders) {
			if (!ensureFolder(new File(root + folder))) {
				allOk = false;
			}
		}
		return allOk;
	}

	private static boolean ensureFolder(final File f) {
		final String folder = f.getAbsolutePath();
		if (f.exists()) {
			if (f.isDirectory()) {
				logger.debug("All files in folder {} are deleted", folder);
				for (final File ff : f.listFiles()) {
					if (!ff.delete()) {
						logger.error("Unable to delete file {}", ff.getAbsolutePath());
						return false;
					}
				}
				return true;
			}

			if (f.delete()) {
				logger.debug("{} is a file. It is deleted to make way for a directory with the same name", folder);
			} else {
				logger.error("{} is a file. Unable to delete it to create a folder with that name", folder);
				return false;
			}
		}
		if (!f.mkdirs()) {
			logger.error("Unable to create folder {}. Aborting..." + f.getPath());
			return false;
		}
		return true;
	}

	static void emitJavaGettersAndSetters(final Field[] fields, final StringBuilder sbf,
			final Map<String, DataType> dataTypes) {
		for (final Field f : fields) {
			final DataType dt = dataTypes.get(f.dataType);
			String typ = "unknownBecauseOfUnknownDataType";
			String get = typ;
			if (dt == null) {
				logger.error("Field {} has an invalid data type of {}", f.name, f.dataType);
			} else {
				typ = Util.JAVA_VALUE_TYPES[dt.valueType.ordinal()];
				get = Util.JAVA_GET_TYPES[dt.valueType.ordinal()];
			}
			final String nam = f.name;
			final String cls = Util.toClassName(nam);

			sbf.append("\n\n\t/**\n\t * set value for ").append(nam);
			sbf.append("\n\t * @param value to be assigned to ").append(nam);
			sbf.append("\n\t */");
			sbf.append("\n\tpublic void set").append(cls).append('(').append(typ).append(" value){");
			sbf.append("\n\t\tthis.fieldValues[").append(f.index).append("] = value;");
			sbf.append("\n\t}");

			sbf.append("\n\n\t/**\n\t * @return value of ").append(nam).append("\n\t */");
			sbf.append("\n\tpublic ").append(typ).append(" get").append(cls).append("(){");
			sbf.append("\n\t\treturn super.fetch").append(get).append("Value(").append(f.index).append(");");
			sbf.append("\n\t}");
		}

	}
}
