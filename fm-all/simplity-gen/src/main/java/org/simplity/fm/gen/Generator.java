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
	protected static final Logger logger = LoggerFactory.getLogger(Generator.class);

	private static final String FOLDER = "/";

	/**
	 * folders to be created/ensured for java sources
	 */
	private static final String[] JAVA_FOLDERS = { Conventions.App.FOLDER_NAME_RECORD, Conventions.App.FOLDER_NAME_FORM,
			Conventions.App.FOLDER_NAME_LIST, Conventions.App.FOLDER_NAME_SQL };

	private static final String CREATE_SQL_COMMENT = "-- This file has the sql to create tables. It includes command to create primary keys.\n"
			+ "-- It is intended to be included in a sql after the script that would delete tables.";
	private static final String DATA_SQL_COMMENT = "-- This file has the template that can be used to create sql to add data to tables."
			+ "\n-- Values clause has one row of empty/0/false values."
			+ "\n-- we intend to introduce some syntax to generate this fiel WITH data in the future";

	private static final String INPUT_ROOT = "c:/gitHub/wip/trm/trm-meta/meta/json/";
	private static final String JAVA_ROOT = "c:/gitHub/wip/trm/trm-meta/trm-server-gen/src/main/java/";
	private static final String PACKAGE_NAME = "in.nsoft.trm.gen";

	// private static final String INPUT_ROOT =
	// "c:/bitBucket/simeta/simeta-meta/meta/";
	// private static final String JAVA_ROOT =
	// "c:/bitBucket/simeta/simeta-meta/simeta-server-gen/src/main/java/";
	// private static final String PACKAGE_NAME = "org.simplity.simeta.gen";

	/**
	 *
	 * @param args
	 * @throws Exception
	 */
	public static void main(final String[] args) throws Exception {
		if (args.length == 3) {
			generate(args[0], args[1], args[2]);
			return;
		}
		generate(INPUT_ROOT, JAVA_ROOT, PACKAGE_NAME);
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

	/**
	 *
	 * @param inputRootFolder folder where application.xlsx file, and spec folder
	 *                        are located.
	 * @param javaRootFolder  java source folder where the sources are to be
	 *                        generated.
	 * @param javaRootPackage root
	 * @return true if all OK. false in case of any error.
	 */
	public static boolean generate(final String inputRootFolder, final String javaRootFolder,
			final String javaRootPackage) {

		/**
		 * we need the folder to end with '/'
		 */
		String inputRoot = inputRootFolder.endsWith(FOLDER) ? inputRootFolder : inputRootFolder + FOLDER;

		String javaRoot = javaRootFolder.endsWith(FOLDER) ? javaRootFolder : javaRootFolder + FOLDER;
		javaRoot += javaRootPackage.replace('.', '/') + FOLDER;
		Generator gen = new Generator(inputRoot, javaRoot, javaRootPackage);
		return gen.go();

	}

	/*
	 * instance is private, to be used by the static class only. we used instance to
	 * avoid passing large number of parameters across static functions
	 */

	private final String inputRoot;
	private final String javaOutputRoot;
	private final String packageName;
	private final String sqlOutputRoot;

	private boolean allOk = true;

	private Application app;
	private Map<String, Record> records;
	private MessageMap messages;
	private ValueListMap valueLists;
	private ValueSchemaMap valueSchemas;

	private Generator(String inputRoot, String javaOutputRoot, String packageName) {
		this.inputRoot = inputRoot;
		this.sqlOutputRoot = inputRoot + "dbSqls/";
		this.javaOutputRoot = javaOutputRoot;
		this.packageName = packageName;
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
		 * load and generate project level components like messages, lists and schemas
		 */
		fileName = this.inputRoot + Conventions.App.MESSAGES_FILE;
		this.messages = Util.loadJson(fileName, MessageMap.class);
		if (this.messages == null) {
			this.messages = new MessageMap();
		} else {
			this.messages.init();
		}

		fileName = this.inputRoot + Conventions.App.VALUE_SCHEMAS_FILE;
		this.valueSchemas = Util.loadJson(fileName, ValueSchemaMap.class);
		if (this.valueSchemas == null) {
			this.valueSchemas = new ValueSchemaMap();
		} else {
			this.valueSchemas.init();
		}

		fileName = this.inputRoot + Conventions.App.LISTS_FILE;
		this.valueLists = Util.loadJson(fileName, ValueListMap.class);
		if (this.valueLists == null) {
			this.valueLists = new ValueListMap();
		} else {
			this.valueLists.init();
		}

		// no java for messages
		this.accumulate(this.messages.generateJava(this.javaOutputRoot, this.packageName));
		this.accumulate(this.valueLists.generateJava(this.javaOutputRoot, this.packageName));
		this.accumulate(this.valueSchemas.generateJava(this.javaOutputRoot, this.packageName));

		this.generateRecords();
		this.generateForms();

		// this is generated at the end to ensure that the required Records are loaded..
		this.accumulate(this.generateSqls());
		return this.allOk;
	}

	private void accumulate(boolean ok) {
		this.allOk = this.allOk && ok;
	}

	private void createOutputFolders() {
		for (final String folder : JAVA_FOLDERS) {
			if (!ensureFolder(new File(this.javaOutputRoot + folder))) {
				this.allOk = false;
			}
		}
		if (!ensureFolder(new File(this.sqlOutputRoot))) {
			this.allOk = false;
		}

	}

	private void generateForms() {
		String folderName = this.inputRoot + Conventions.App.FOLDER_NAME_FORM;
		File folder = new File(folderName);
		if (folder.exists() == false) {
			logger.error("Forms folder {} not found. No forms are processed", folderName);
			return;
		}

		logger.info("Going to process forms from folder {}", folderName);

		String javaFolder = this.javaOutputRoot + Conventions.App.FOLDER_NAME_FORM + '/';

		for (final File file : folder.listFiles()) {
			String fn = file.getName();
			if (fn.endsWith(Conventions.App.EXTENSION_FORM) == false) {
				logger.debug("Skipping non-form file {} ", fn);
				continue;
			}
			logger.info("processing form : {}", fn);

			fn = fn.substring(0, fn.length() - Conventions.App.EXTENSION_FORM.length());
			final Form form = Util.loadJson(file.getPath(), Form.class);
			if (form == null) {
				logger.error("Form {} did not load properly. Not processed", fn);
				continue;
			}

			if (!fn.equals(form.name)) {
				logger.error("Form name {} does not match with its file named {}", form.name, fn);
				continue;
			}

			Record record = this.records.get(form.mainRecordName);
			if (record == null) {
				logger.error("Form {} uses record {}, but that record is not defined", form.name, form.mainRecordName);
				continue;
			}

			if (record.gotErrors) {
				logger.error("Record {} is in error. Form {} uses this record. Hence this form is NOT processed",
						record.name, form.name);
				continue;
			}

			form.initialize(record);
			form.generateJava(javaFolder, this.packageName);
		}
	}

	private void generateRecords() {
		String folderName = this.inputRoot + Conventions.App.FOLDER_NAME_RECORD;
		File folder = new File(folderName);
		if (folder.exists() == false) {
			logger.error("Records folder {} not found. No Records are processed", folderName);
			return;
		}

		logger.info("Going to process records from folder {}", folderName);

		this.records = new HashMap<>();
		String javaFolder = null;
		StringBuilder createSqls = new StringBuilder(CREATE_SQL_COMMENT);
		StringBuilder dataSqls = new StringBuilder(DATA_SQL_COMMENT);

		javaFolder = this.javaOutputRoot + Conventions.App.FOLDER_NAME_RECORD + '/';

		final Map<String, Record> subRecords = new HashMap<>();
		final Map<String, ValueSchema> schemas = this.valueSchemas.getSchemas();

		for (final File file : folder.listFiles()) {
			String fn = file.getName();
			if (fn.endsWith(Conventions.App.EXTENSION_RECORD) == false) {
				logger.debug("Skipping non-record file {} ", fn);
				continue;
			}
			logger.info("processing record : {}", fn);

			fn = fn.substring(0, fn.length() - Conventions.App.EXTENSION_RECORD.length());
			final Record record = Util.loadJson(file.getPath(), Record.class);
			if (record == null) {
				logger.error("Record {} not generated", fn);
				continue;
			}

			if (!fn.equals(record.name)) {
				logger.error("Record name {} does not match with its file named {}. Skipped", record.name, fn);
				continue;
			}

			/**
			 * sub-record may have to wait till the main-record is read and processed
			 */
			if (record.getMainRecordName() != null) {
				subRecords.put(fn, record);
				continue;
			}

			record.init(schemas);

			this.records.put(record.name, record);
		}

		/**
		 * init sub records, as their main-records are in place now
		 */
		if (subRecords.size() > 0) {
			for (Record subRecord : subRecords.values()) {
				Record record = this.records.get(subRecord.getMainRecordName());
				if (record == null) {
					logger.error(
							"Sub-record {} uses {} as its main record but that main record is not defined. Sub-record SKIPPED",
							subRecord.name, subRecord.mainRecordName);
					continue;
				}
				subRecord.initExtendedRecord(schemas, record);
				this.records.put(subRecord.name, subRecord);
			}
		}

		for (Record record : this.records.values()) {
			record.generateJava(javaFolder, this.packageName);
			record.emitSql(createSqls, dataSqls);
		}
	}

	private boolean generateSqls() {
		String folderName = this.inputRoot + Conventions.App.FOLDER_NAME_SQL;
		File folder = new File(folderName);
		if (folder.exists() == false) {
			logger.error("Sqls folder {} not found. No Sqls are processed", folderName);
			return false;
		}

		logger.info("Going to process SQLs under folder {}", folderName);

		String javaFolder = this.javaOutputRoot + Conventions.App.FOLDER_NAME_SQL + '/';

		for (final File file : folder.listFiles()) {
			String fn = file.getName();
			if (fn.endsWith(Conventions.App.EXTENSION_SQL) == false) {
				logger.debug("Skipping non-sql file {} ", fn);
				continue;
			}
			logger.info("processing sql : {}", fn);

			fn = fn.substring(0, fn.length() - Conventions.App.EXTENSION_SQL.length());

			final Sql sql = Util.loadJson(file.getPath(), Sql.class);
			if (sql == null) {
				logger.error("Sql {} not generated", fn);
				return false;
			}

			if (!fn.equals(sql.name)) {
				logger.error("Sql name {} does not match with its file name: {}", sql.name, fn);
				return false;
			}

			sql.init(this.valueSchemas.getSchemas(), this.records);
			sql.generateJava(javaFolder, this.packageName);

		}
		return true;
	}

}
