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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.simplity.fm.core.Conventions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.stream.JsonReader;

/**
 * @author simplity.org
 *
 */
public class Generator {
	protected static final Logger logger = LoggerFactory
			.getLogger(Generator.class);
	private static final String FOLDER = "/";

	private static final String EXT_FRM = ".frm.json";
	private static final String EXT_REC = ".rec.json";
	private static final String EXT_SQL = ".sql.json";
	private static final String CORE_NAME = "simplity-client";

	/**
	 *
	 * @param args
	 * @throws Exception
	 */
	public static void main(final String[] args) throws Exception {
		if (args.length == 2) {
			generateClientComponentsNOT_USED(args[0], args[1]);
			return;
		}
		if (args.length == 5) {
			generate(args[0], args[1], args[2], args[3], args[4]);
			return;
		}
		System.err.println(
				"Usage : java Generator.class resourceRootFolder tsFormFolder\n or \n"
						+ "Usage : java Generator.class resourceRootFolder generatedSourceRootFolder generatedPackageName tsImportPrefix tsOutputFolder");
	}

	/**
	 * generate *.form.ts files from records
	 *
	 * @param inputRootFolder
	 * @param outputRootFolder
	 */
	private static void generateClientComponentsNOT_USED(
			final String inputRootFolder, final String outputRootFolder) {
		String inFolder = inputRootFolder;
		if (!inputRootFolder.endsWith(FOLDER)) {
			inFolder += FOLDER;
		}

		String outFolder = outputRootFolder;
		if (!outputRootFolder.endsWith(FOLDER)) {
			outFolder += FOLDER;
		}

		String fileName = inputRootFolder + Conventions.App.APP_FILE;
		Application app = Util.loadJson(fileName, Application.class);

		if (app == null) {
			logger.error(
					"Error while loading configuration file {} . Aborting..",
					fileName);
			return;
		}

		app.initialize();
		/*
		 * generate project level components like data types
		 */
		app.generateTs(outFolder, CORE_NAME);
		emitMsgsTsNOT_USED(inFolder, outFolder);

		logger.debug("Going to process records under folder {}", inFolder);
		File f = new File(inFolder + "rec/");

		if (f.exists() == false) {
			logger.error(
					"Records folder {} not found. No records are processed",
					f.getPath());
			return;
		}

		ensureFolder(new File(outFolder + "forms/"));
		ensureFolder(new File(outFolder + "pages/"));
		/*
		 * builders for the declaration files
		 */
		final StringBuilder forms = new StringBuilder(
				"\nexport const allForms: Forms = {");
		final StringBuilder formsImport = new StringBuilder();
		formsImport.append("import { Forms } from '").append(CORE_NAME)
				.append("';");

		/*
		 * transitional issue with forms on the server: server defines forms as
		 * a way to expose simple records as well as hierarchical structure of
		 * forms to the client. frm.json file essentially declares that the
		 * underlying record is available to the client and it also provides a
		 * way to send/receive hierarchical structures.
		 *
		 * For the new client, we do not need the hierarchical structure, but we
		 * do need the form as a record, in case the name is different from the
		 * record
		 *
		 * For example, if there is a form named a that just uses recordName=b,
		 * the new client requires a "form" named b that has the fields in a.
		 *
		 * Our design is to detect such cases, and keep them ready as 'aliases'
		 * of a record, and emit them as and when the underlying record is
		 * emitted.
		 *
		 * for example, in the above case, after emitting record b, we also emit
		 * the record b as if its name is a.
		 *
		 */

		Map<String, Set<String>> aliases = new HashMap<>();
		File formsFolder = new File(inFolder + "form/");
		if (formsFolder.exists()) {
			buildAliasesNOT_USED(aliases, formsFolder);
		}

		/*
		 * also, we want to ensure that a wrapped form name does not clash with
		 * another record name
		 */
		Set<String> allRecords = new HashSet<>();

		for (final File file : f.listFiles()) {
			final String fn = file.getName();
			if (fn.endsWith(EXT_REC) == false) {
				logger.debug("File {} skipped as it does not end with {}", fn,
						EXT_REC);
				continue;
			}
			logger.debug("Going to generate record " + fn);
			final Record record = Util.loadJson(file.getPath(), Record.class);
			if (record == null) {
				logger.error("Record {} not generated.", fn);
				continue;
			}
			final String recordName = fn.substring(0,
					fn.length() - ".rec.json".length());
			if (!recordName.equals(record.name)) {
				logger.error(
						"File {} contains record named {}. It is mandatory to use record name same as the filename",
						recordName, record.name);
				continue;
			}

			record.init(recordName, app.valueSchemas);
			writeRecordNOT_USED(record, outFolder, forms, formsImport,
					allRecords);

			Set<String> names = aliases.get(recordName);
			if (names != null) {
				for (String s : names) {
					record.name = s;
					writeRecordNOT_USED(record, outFolder, forms, formsImport,
							allRecords);
				}
			}

		}

		/*
		 * write-out declaration files
		 */
		formsImport.append(forms.toString()).append("\n}\n");
		Util.writeOut(outFolder + "allForms.ts", formsImport);
	}

	private static void writeRecordNOT_USED(Record record, String outFolder,
			StringBuilder forms, StringBuilder formsImport,
			Set<String> allRecords) {
		String recordName = record.name;
		if (allRecords.add(recordName) == false) {
			logger.error(
					"{} is defined as a record. A form with the same name exists but it uses a different record name. This is incorrect",
					recordName);
			logger.error(
					"Form should use the same name as the primary record it is based on, or a name that is different from any other ecord name");
			return;
		}
		StringBuilder sbf = new StringBuilder();
		record.emitClientForm(sbf);
		if (sbf.length() > 0) {
			String genFileName = outFolder + "forms/" + recordName + ".form.ts";
			Util.writeOut(genFileName, sbf);
			forms.append("\n\t").append(recordName).append(": ")
					.append(recordName).append("Form,");
			formsImport.append("\nimport { ").append(recordName)
					.append("Form } from './forms/").append(recordName)
					.append(".form';");
		}
	}

	private static void buildAliasesNOT_USED(Map<String, Set<String>> aliases,
			File f) {
		for (final File file : f.listFiles()) {
			final String fn = file.getName();
			if (fn.endsWith(EXT_REC) == false) {
				logger.debug("Skipping non-form file {} ", fn);
				continue;
			}
			final Form form = Util.loadJson(file.getPath(), Form.class);
			if (form == null) {
				logger.error("Form {} not Processed.", fn);
				continue;
			}
			final String formName = fn.substring(0,
					fn.length() - ".form.json".length());
			if (!formName.equals(form.name)) {
				logger.error(
						"File {} contains form named {}. It is mandatory to use form name same as the filename",
						formName, form.name);
				continue;
			}

			String recordName = form.recordName;
			if (formName.equals(recordName)) {
				continue;
			}

			Set<String> names = aliases.get(recordName);
			if (names == null) {
				names = new HashSet<>();
				aliases.put(recordName, names);
			}
			names.add(formName);
		}
	}

	private static void emitMsgsTsNOT_USED(final String inFolder,
			final String outFolder) {
		logger.info("Generating Messages..");
		final String fileName = inFolder + Conventions.App.MESSAGES_FILE;
		final Map<String, String> msgs = new HashMap<>();
		final File f = new File(fileName);
		if (f.exists()) {
			try (JsonReader reader = new JsonReader(new FileReader(f))) {
				Util.loadStringMap(msgs, reader);
				logger.info("{} messages read", msgs.size());
			} catch (final Exception e) {
				logger.error(
						"Exception while trying to read file {}. Error: {}",
						f.getPath(), e.getMessage());
				e.printStackTrace();
				return;
			}
		} else {
			logger.error(
					"project has not defined messages mapping in the file {}.",
					fileName);
		}

		final StringBuilder sbf = new StringBuilder();
		sbf.append("import { Messages } from 'simplity-core';");
		sbf.append("\n\nexport const allMessages: Messages = {");
		boolean isFirst = true;
		for (final Map.Entry<String, String> entry : msgs.entrySet()) {
			if (isFirst) {
				isFirst = false;
			} else {
				sbf.append(',');
			}
			sbf.append("\n\t").append(Util.qoutedString(entry.getKey()))
					.append(": ").append(Util.qoutedString(entry.getValue()));
		}
		sbf.append("\n}\n\n");
		final String fn = outFolder + "allMessages.ts";
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
	public static void generate(final String inputRootFolder,
			final String javaRootFolder, final String javaRootPackage,
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
				new String[]{"rec/", "form/", "list/", "sql/"}) == false) {
			return;
		}

		/*
		 * ts folder
		 */
		if (!ensureFolder(new File(tsRootFolder))) {
			logger.error("Unable to clean/create ts root folder {}",
					tsRootFolder);
			return;
		}

		final String fileName = resourceRootFolder + Conventions.App.APP_FILE;

		final Application app = Util.loadJson(fileName, Application.class);
		if (app == null) {
			logger.error("Exception while trying to read file {}", fileName);
			return;
		}
		app.initialize();

		/*
		 * generate project level components like data types
		 */
		app.generateJava(generatedSourceRootFolder, javaRootPackage);

		logger.debug("Going to process records under folder {}",
				resourceRootFolder);
		final Map<String, Record> recs = new HashMap<>();
		File f = new File(resourceRootFolder + "rec/");
		if (f.exists() == false) {
			logger.error(
					"Records folder {} not found. No records are processed",
					f.getPath());
		} else {

			for (final File file : f.listFiles()) {
				final String fn = file.getName();
				if (fn.endsWith(EXT_REC) == false) {
					logger.debug("Skipping non-record file {}", fn);
					continue;
				}

				logger.info("file: {}", fn);
				final Record record = emitRecord(file,
						generatedSourceRootFolder, tsRootFolder,
						app.valueSchemas, app, javaRootPackage, tsImportPrefix);
				if (record != null) {
					recs.put(record.name, record);
				}
			}
		}

		logger.debug("Going to process forms under folder {}",
				resourceRootFolder);
		f = new File(resourceRootFolder + "form/");
		if (f.exists() == false) {
			logger.error("Forms folder {} not found. No forms are processed",
					f.getPath());
		} else {

			for (final File file : f.listFiles()) {
				final String fn = file.getName();
				if (fn.endsWith(EXT_FRM) == false) {
					logger.debug("Skipping non-form file {} ", fn);
					continue;
				}
				logger.info("file: {}", fn);
				emitForm(file, generatedSourceRootFolder, tsRootFolder, app,
						javaRootPackage, tsImportPrefix, recs);
			}
		}

		logger.debug("Going to process sqls under folder {}sql/",
				resourceRootFolder);
		f = new File(resourceRootFolder + "sql/");
		if (f.exists() == false) {
			logger.error("Sql folder {} not found. No sqls processed",
					f.getPath());
		} else {

			for (final File file : f.listFiles()) {
				final String fn = file.getName();
				if (fn.endsWith(EXT_SQL) == false) {
					logger.debug("Skipping non-sql file {} ", fn);
					continue;
				}
				logger.info("file: {}", fn);
				emitSql(file, generatedSourceRootFolder, app.valueSchemas,
						javaRootPackage);
			}
		}
	}

	private static void emitForm(final File file,
			final String generatedSourceRootFolder, final String tsOutputFolder,
			final Application app, final String rootPackageName,
			final String tsImportPrefix, final Map<String, Record> records) {
		String fn = file.getName();
		fn = fn.substring(0, fn.length() - EXT_FRM.length());
		logger.debug("Going to generate Form " + fn);
		final Form form = Util.loadJson(file.getPath(), Form.class);
		if (form == null) {
			logger.error("Form {} not generated", fn);
			return;
		}

		if (!fn.equals(form.name)) {
			logger.error(
					"File {} contains form named {}. It is mandatory to use form name same as the filename",
					fn, form.name);
			return;
		}
		Record record = null;
		record = records.get(form.recordName);
		if (record == null) {
			logger.error(
					"Form {} uses record {}, but that record is not defined",
					form.name, form.recordName);
			return;
		}
		form.initialize(record);
		final StringBuilder sbf = new StringBuilder();

		final String cls = Util.toClassName(fn);
		form.emitJavaForm(sbf, rootPackageName);
		final String outPrefix = generatedSourceRootFolder + "form/" + cls;
		Util.writeOut(outPrefix + "Form.java", sbf);

		sbf.setLength(0);
		form.emitTs(sbf, app.valueLists, tsImportPrefix);
		Util.writeOut(tsOutputFolder + fn + "Form.ts", sbf);

	}

	/**
	 * @param files
	 * @param generatedSourceRootFolder
	 * @param tsOutputFolder
	 * @param valueSchemas
	 * @param app
	 * @param rootPackageName
	 * @param tsImportPrefix
	 */
	private static Record emitRecord(final File file,
			final String generatedSourceRootFolder, final String tsOutputFolder,
			final Map<String, ValueSchema> valueSchemas, final Application app,
			final String packageName, final String tsImportPrefix) {
		String fn = file.getName();
		fn = fn.substring(0, fn.length() - EXT_REC.length());
		logger.debug("Going to generate record " + fn);
		final Record record = Util.loadJson(file.getPath(), Record.class);
		if (record == null) {
			logger.error("Record {} not generated. ", fn);
			return null;
		}
		if (!fn.equals(record.name)) {
			logger.error(
					"File {} contains record named {}. It is mandatory to use record name same as the filename",
					fn, record.name);
			return null;
		}

		record.init(fn, valueSchemas);

		final String outNamePrefix = generatedSourceRootFolder + "rec/"
				+ Util.toClassName(fn);
		/*
		 * Record.java
		 */
		final StringBuilder sbf = new StringBuilder();
		record.emitJavaClass(sbf, packageName);
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

	private static void emitSql(final File file,
			final String generatedSourceRootFolder,
			final Map<String, ValueSchema> valueSchemas,
			final String packageName) {
		String fn = file.getName();
		fn = fn.substring(0, fn.length() - EXT_SQL.length());
		logger.debug("Going to generate Sql " + fn);
		final Sql sql = Util.loadJson(file.getPath(), Sql.class);
		if (sql == null) {
			logger.error("Sql {} not generated.", fn);
			return;
		}
		sql.init(valueSchemas);
		final String cls = Util.toClassName(fn) + "Sql";
		final StringBuilder sbf = new StringBuilder();
		sql.emitJava(sbf, packageName, cls,
				Conventions.App.GENERATED_DATA_TYPES_CLASS_NAME);
		final String outName = generatedSourceRootFolder + "sql/"
				+ Util.toClassName(fn) + "Sql.java";
		Util.writeOut(outName, sbf);

	}

	private static boolean createOutputFolders(final String root,
			final String[] folders) {
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
						logger.error("Unable to delete file {}",
								ff.getAbsolutePath());
						return false;
					}
				}
				return true;
			}

			if (f.delete()) {
				logger.debug(
						"{} is a file. It is deleted to make way for a directory with the same name",
						folder);
			} else {
				logger.error(
						"{} is a file. Unable to delete it to create a folder with that name",
						folder);
				return false;
			}
		}
		if (!f.mkdirs()) {
			logger.error(
					"Unable to create folder {}. Aborting..." + f.getPath());
			return false;
		}
		return true;
	}

}
