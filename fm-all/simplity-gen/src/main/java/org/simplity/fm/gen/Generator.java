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
import java.util.HashMap;
import java.util.Map;

import org.simplity.fm.core.Conventions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author simplity.org
 */
public class Generator {
	protected static final Logger logger = LoggerFactory
			.getLogger(Generator.class);

	private static final String FOLDER = "/";

	private static final String TS_FOLDER_FORM = "lib/form/";
	private static final String TS_FOLDER_LIB = "lib/";

	private static final String IMPORTS = "import _allListSources from \"./allListSources.json\";\n"
			+ "import _allMessages from \"./allMessages.json\";\n"
			+ "import _allValueSchemas from \"./allValueSchemas.json\";\n\n";
	private static final String EXPORTS = "export const generatedArtifacts = {\n"
			+ "\tallListSources: _allListSources,\n"
			+ "\tallMessages: _allMessages,\n"
			+ "\tallValueSchemas: _allValueSchemas,\n" + "\tallForms: {";
	/**
	 * folders to be created/ensured for java sources
	 */
	private static final String[] JAVA_FOLDERS = {
			Conventions.App.FOLDER_NAME_RECORD,
			Conventions.App.FOLDER_NAME_FORM, Conventions.App.FOLDER_NAME_LIST,
			Conventions.App.FOLDER_NAME_SQL};

	/**
	 * folders to be created/ensured for ts
	 */
	private static final String[] TS_FOLDERS = {TS_FOLDER_FORM};

	private static final String CREATE_SQL_COMMENT = "-- This file has the sql to create tables. It includes command to create primary keys.\n"
			+ "-- It is intended to be included in a sql after the script that would delete tables.";
	private static final String DATA_SQL_COMMENT = "-- This file has the template that can be used to create sql to add data to tables."
			+ "\n-- Values clause has one row of empty/0/false values."
			+ "\n-- we intend to introduce some syntax to generate this fiel WITH data in the future";

	/**
	 *
	 * @param args
	 * @throws Exception
	 */
	public static void main(final String[] args) throws Exception {
		if (args.length == 4) {
			generate(args[0], args[1], args[2], args[3]);
			return;
		}
		generate("c:/bitBucket/simeta/simeta-meta/meta/",
				"c:/bitBucket/simeta/simeta-meta/simeta-server-gen/src/main/java/",
				"org.simplity.simeta.gen",
				"c:/bitBucket/simeta/simeta-meta/simeta-client-gen/src/");
		// System.err.println(
		// "Usage : java Generator.class resourceRootFolder tsFormFolder\n or
		// \n"
		// + "Usage : java Generator.class resourceRootFolder
		// generatedSourceRootFolder generatedPackageName tsOutputFolder");
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

	/**
	 *
	 * @param inputRootFolder
	 *            folder where application.xlsx file, and spec folder are
	 *            located. e.g.
	 * @param javaRootFolder
	 *            java source folder where the sources are to be generated. null
	 *            if java is not to be generated
	 * @param javaRootPackage
	 *            root
	 * @param tsRootFolder
	 *            folder where generated ts files are to be saved. null to not
	 *            generate TS code.
	 * @return true if all OK. false in case of any error.
	 */
	public static boolean generate(final String inputRootFolder,
			final String javaRootFolder, final String javaRootPackage,
			final String tsRootFolder) {

		if (javaRootFolder == null && tsRootFolder == null) {
			logger.error(
					"Both javaRootFolder and tsRootFolder are null. Nothing to generate");
			return false;
		}

		/**
		 * we need the folder to end with '/'
		 */
		String inputRoot = inputRootFolder.endsWith(FOLDER)
				? inputRootFolder
				: inputRootFolder + FOLDER;

		String javaRoot = null;
		if (javaRootFolder != null) {
			javaRoot = javaRootFolder.endsWith(FOLDER)
					? javaRootFolder
					: javaRootFolder + FOLDER;
			javaRoot += javaRootPackage.replace('.', '/') + FOLDER;
		}
		String tsRoot = null;
		if (tsRootFolder != null) {
			tsRoot = tsRootFolder.endsWith(FOLDER)

					? tsRootFolder
					: tsRootFolder + FOLDER;
		}
		Generator gen = new Generator(inputRoot, javaRoot, javaRootPackage,
				tsRoot);
		return gen.go();

	}

	/*
	 * instance is private, to be used by the static class only. we used
	 * instance to avoid passing large number of parameters across static
	 * functions
	 */

	private final String inputRoot;
	private final String javaOutputRoot;
	private final String packageName;
	private final String tsSrcFolder;
	private final String tsLibFolder;
	private final String tsFormFolder;
	private final String sqlOutputRoot;
	private final boolean toGenerateJava;
	private final boolean toGenerateTs;

	private boolean allOk = true;

	private Application app;
	private Map<String, Record> records;

	private Generator(String inputRoot, String javaOutputRoot,
			String packageName, String tsSourceRoot) {
		this.inputRoot = inputRoot;
		this.sqlOutputRoot = inputRoot + "dbSqls/";
		this.javaOutputRoot = javaOutputRoot;
		this.packageName = packageName;
		this.toGenerateJava = javaOutputRoot != null;
		if (tsSourceRoot == null) {
			this.toGenerateTs = false;
			this.tsSrcFolder = null;
			this.tsLibFolder = null;
			this.tsFormFolder = null;
		} else {
			this.toGenerateTs = true;
			this.tsSrcFolder = tsSourceRoot;
			this.tsLibFolder = tsSourceRoot + TS_FOLDER_LIB;
			this.tsFormFolder = tsSourceRoot + TS_FOLDER_FORM;
		}
	}

	private boolean go() {
		String fileName = this.inputRoot + Conventions.App.APP_FILE;

		this.app = Util.loadJson(fileName, Application.class);
		if (this.app == null) {
			logger.error("Exception while trying to read file {}", fileName);
			return false;
		}
		this.app.initialize();

		/*
		 * ensure all output folders are clean and ready
		 */
		createOutputFolders();
		if (!this.allOk) {
			return false;
		}

		/*
		 * generate project level components like data types
		 */
		if (this.toGenerateJava) {
			this.accumulate(
					app.generateJava(this.javaOutputRoot, this.packageName));
		}
		if (this.toGenerateTs) {
			this.accumulate(app.generateTs(this.tsLibFolder));

			fileName = this.inputRoot + Conventions.App.MESSAGES_FILE;
			MessageMap messages = Util.loadJson(fileName, MessageMap.class);
			if (messages == null) {
				messages = new MessageMap();
			}
			this.accumulate(messages.generateTs(this.tsLibFolder));
			writeIndexTs();
		}

		// exports and import are built to write into gen.ts
		StringBuilder imports = new StringBuilder(IMPORTS);
		StringBuilder exports = new StringBuilder(EXPORTS);

		this.generateRecords(exports, imports);
		this.generateForms(exports, imports);

		if (this.toGenerateTs) {
			exports.setLength(exports.length() - 1); // remove the last comma
			exports.append("\n\t}\n};\n\n");
			imports.append('\n').append(exports);

			Util.writeOut(this.tsLibFolder + "gen.ts", imports.toString());
		}

		if (this.toGenerateJava) {
			this.generateSqls();
		}
		return this.allOk;
	}

	private void accumulate(boolean ok) {
		this.allOk = this.allOk && ok;
	}

	private void createOutputFolders() {
		if (this.toGenerateJava) {
			for (final String folder : JAVA_FOLDERS) {
				if (!ensureFolder(new File(this.javaOutputRoot + folder))) {
					this.allOk = false;
				}
			}
			if (!ensureFolder(new File(this.sqlOutputRoot))) {
				this.allOk = false;
			}

		}
		if (this.toGenerateTs) {

			for (final String folder : TS_FOLDERS) {
				if (!ensureFolder(new File(this.tsSrcFolder + folder))) {
					this.allOk = false;
				}
			}
		}
	}

	private void generateForms(StringBuilder exports, StringBuilder imports) {
		String folderName = this.inputRoot + Conventions.App.FOLDER_NAME_FORM;
		File folder = new File(folderName);
		if (folder.exists() == false) {
			logger.error("Forms folder {} not found. No forms are processed",
					folderName);
			return;
		}

		logger.info("Going to process forms from folder {}", folderName);

		String javaFolder = this.javaOutputRoot
				+ Conventions.App.FOLDER_NAME_FORM + '/';

		for (final File file : folder.listFiles()) {
			String fn = file.getName();
			if (fn.endsWith(Conventions.App.EXTENSION_FORM) == false) {
				logger.debug("Skipping non-form file {} ", fn);
				continue;
			}
			logger.info("processing form : {}", fn);

			fn = fn.substring(0,
					fn.length() - Conventions.App.EXTENSION_FORM.length());
			final Form form = Util.loadJson(file.getPath(), Form.class);
			if (form == null) {
				logger.error("Form {} did not load properly. Not processed",
						fn);
				return;
			}

			if (!fn.equals(form.formName)) {
				logger.error(
						"Form name {} does not match with its file named {}",
						form.formName, fn);
				return;
			}

			Record record = this.records.get(form.recordName);
			if (record == null) {
				logger.error(
						"Form {} uses record {}, but that record is not defined",
						form.formName, form.recordName);
				return;
			}

			form.initialize(record);
			if (this.toGenerateJava) {
				form.generateJava(javaFolder, this.packageName);
			}
			if (this.toGenerateTs) {
				boolean done = form.generateTs(tsFormFolder);
				if (done) {
					emitCommonFormTs(exports, imports, fn);
				}
			}
		}
	}

	private void generateRecords(StringBuilder exports, StringBuilder imports) {
		String folderName = this.inputRoot + Conventions.App.FOLDER_NAME_RECORD;
		File folder = new File(folderName);
		if (folder.exists() == false) {
			logger.error(
					"Records folder {} not found. No Records are processed",
					folderName);
			return;
		}

		logger.info("Going to process records from folder {}", folderName);

		this.records = new HashMap<>();
		String javaFolder = null;
		StringBuilder createSqls = new StringBuilder(CREATE_SQL_COMMENT);
		StringBuilder dataSqls = new StringBuilder(DATA_SQL_COMMENT);

		if (this.toGenerateJava) {
			javaFolder = this.javaOutputRoot
					+ Conventions.App.FOLDER_NAME_RECORD + '/';
		}

		for (final File file : folder.listFiles()) {
			String fn = file.getName();
			if (fn.endsWith(Conventions.App.EXTENSION_RECORD) == false) {
				logger.debug("Skipping non-record file {} ", fn);
				continue;
			}
			logger.info("processing record : {}", fn);

			fn = fn.substring(0,
					fn.length() - Conventions.App.EXTENSION_RECORD.length());
			final Record record = Util.loadJson(file.getPath(), Record.class);
			if (record == null) {
				logger.error("Record {} not generated", fn);
				return;
			}

			if (!fn.equals(record.recordName)) {
				logger.error(
						"Record name {} does not match with its file named {}",
						record.recordName, fn);
				return;
			}

			record.init(fn, this.app.valueSchemas);

			this.records.put(record.recordName, record);
			if (this.toGenerateJava) {
				record.generateJava(javaFolder, packageName);
				record.emitSql(createSqls, dataSqls);
			}

			if (this.toGenerateTs) {
				Form form = Form.fromRecord(record);
				final boolean done = form.generateTs(this.tsFormFolder);
				if (done) {
					emitCommonFormTs(exports, imports, fn);
				}
			}
		}

		if (this.toGenerateJava) {
			Util.writeOut(this.sqlOutputRoot + "createTables.sql",
					createSqls.toString());
			Util.writeOut(this.sqlOutputRoot + "addData.sql",
					dataSqls.toString());
		}
	}

	static private void emitCommonFormTs(StringBuilder exports,
			StringBuilder imports, String formName) {
		// variable name is prefixed with _ to avoid clash with
		// reserved words
		imports.append("import _").append(formName).append(" from './form/")
				.append(formName).append(".form.json';\n");

		exports.append("\n\t\t'").append(formName).append("': _")
				.append(formName).append(',');
	}

	private void generateSqls() {
		String folderName = this.inputRoot + Conventions.App.FOLDER_NAME_SQL;
		File folder = new File(folderName);
		if (folder.exists() == false) {
			logger.error("Sqls folder {} not found. No Sqls are processed",
					folderName);
			return;
		}

		logger.info("Going to process SQLs under folder {}", folderName);

		String javaFolder = this.javaOutputRoot
				+ Conventions.App.FOLDER_NAME_SQL + '/';

		for (final File file : folder.listFiles()) {
			String fn = file.getName();
			if (fn.endsWith(Conventions.App.EXTENSION_SQL) == false) {
				logger.debug("Skipping non-sql file {} ", fn);
				continue;
			}
			logger.info("processing sql : {}", fn);

			fn = fn.substring(0,
					fn.length() - Conventions.App.EXTENSION_SQL.length());
			final Sql sql = Util.loadJson(file.getPath(), Sql.class);
			if (sql == null) {
				logger.error("Sql {} not generated", fn);
				return;
			}

			if (!fn.equals(sql.name)) {
				logger.error(
						"Record name {} does not match with its file named {}",
						sql.name, fn);
				return;
			}

			sql.init(this.app.valueSchemas);
			sql.generateJava(javaFolder, this.packageName);
		}
	}

	private static final String INDEX_TS = "export * from './lib/gen';";
	private static final String INDEX_NAME = "index.ts";

	private void writeIndexTs() {
		/**
		 * index in the root folder
		 */
		String f = this.tsSrcFolder + INDEX_NAME;
		Util.writeOut(f, INDEX_TS);
		logger.info("File {} generated", f);
	}

}
