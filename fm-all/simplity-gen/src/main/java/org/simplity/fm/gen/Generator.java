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

	private static final String CORE_NAME = "simplity-client";

	private static final String[] JAVA_FOLDERS = {
			Conventions.App.FOLDER_NAME_RECORD,
			Conventions.App.FOLDER_NAME_FORM, Conventions.App.FOLDER_NAME_LIST,
			Conventions.App.FOLDER_NAME_SQL};
	private static final String[] TS_FOLDERS = {
			Conventions.App.FOLDER_NAME_FORM};

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
		System.err.println(
				"Usage : java Generator.class resourceRootFolder tsFormFolder\n or \n"
						+ "Usage : java Generator.class resourceRootFolder generatedSourceRootFolder generatedPackageName tsOutputFolder");
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
	 * @return true if all OK. false in case of any error. Notet
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
	private final String tsOutputRoot;
	private final boolean toGenerateJava;
	private final boolean toGenerateTs;

	private boolean allOk = true;

	private Application app;
	private Map<String, Record> records;

	private Generator(String inputRoot, String javaOutputRoot,
			String packageName, String tsOutputRoot) {
		this.inputRoot = inputRoot;
		this.javaOutputRoot = javaOutputRoot;
		this.packageName = packageName;
		this.tsOutputRoot = tsOutputRoot;
		this.toGenerateJava = javaOutputRoot != null;
		this.toGenerateTs = tsOutputRoot != null;

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
			this.accumulate(app.generateTs(this.tsOutputRoot, CORE_NAME));
			fileName = this.inputRoot + Conventions.App.MESSAGES_FILE;
			MessageMap messages = Util.loadJson(fileName, MessageMap.class);
			if (messages != null) {
				this.accumulate(
						messages.generateTs(this.tsOutputRoot, CORE_NAME));
			}
		}

		this.generateRecords();

		if (this.toGenerateJava) {
			this.generateForms();
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
		}
		if (this.toGenerateTs) {

			for (final String folder : TS_FOLDERS) {
				if (!ensureFolder(new File(this.tsOutputRoot + folder))) {
					this.allOk = false;
				}
			}
		}
	}

	private void generateForms() {
		String folderName = this.inputRoot + Conventions.App.FOLDER_NAME_FORM;
		File folder = new File(folderName);
		if (folder.exists() == false) {
			logger.error("Forms folder {} not found. No forms are processed",
					folderName);
			return;
		}

		logger.info("Going to process forms under folder {}", folderName);

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
				logger.error("Form {} not generated", fn);
				return;
			}

			if (!fn.equals(form.name)) {
				logger.error(
						"Form name {} does not match with its file named {}",
						form.name, fn);
				return;
			}

			Record record = this.records.get(form.recordName);
			if (record == null) {
				logger.error(
						"Form {} uses record {}, but that record is not defined",
						form.name, form.recordName);
				return;
			}

			form.initialize(record);
			form.generateJava(javaFolder, this.packageName);
		}
	}

	private void generateRecords() {
		String folderName = this.inputRoot + Conventions.App.FOLDER_NAME_RECORD;
		File folder = new File(folderName);
		if (folder.exists() == false) {
			logger.error(
					"Records folder {} not found. No Records are processed",
					folderName);
			return;
		}

		logger.info("Going to process records under folder {}", folderName);

		this.records = new HashMap<>();
		String javaFolder = null;
		String tsFolder = null;
		StringBuilder allForms = new StringBuilder();
		StringBuilder imports = new StringBuilder();
		if (this.javaOutputRoot != null) {
			javaFolder = this.javaOutputRoot
					+ Conventions.App.FOLDER_NAME_RECORD + '/';
		}

		if (this.tsOutputRoot != null) {
			// note that a record is generated as a form on the client side
			tsFolder = this.tsOutputRoot + Conventions.App.FOLDER_NAME_FORM
					+ '/';
			imports.append("import { Forms } from '").append(CORE_NAME)
					.append("';\n");
			allForms.append("\nexport const allForms: Forms = {");
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

			if (!fn.equals(record.name)) {
				logger.error(
						"Record name {} does not match with its file named {}",
						record.name, fn);
				return;
			}

			record.init(fn, this.app.valueSchemas);

			this.records.put(record.name, record);
			if (this.toGenerateJava) {
				record.generateJava(javaFolder, packageName);
			}

			if (this.toGenerateTs) {
				final boolean done = record.generateTs(tsFolder, CORE_NAME);
				if (done) {
					imports.append("\nimport { ").append(fn)
							.append("Form } from './form/").append(fn)
							.append(".form';");
					allForms.append("\n\t").append(fn).append(": ").append(fn)
							.append("Form,");
				}
			}
		}

		if (this.tsOutputRoot != null) {
			imports.append('\n').append(allForms).append("\n};\n");
			Util.writeOut(this.tsOutputRoot + "allForms.ts", imports);
		}
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
				+ Conventions.App.FOLDER_NAME_SQL;
		String javaPackage = this.packageName + ".sql";

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
			sql.generateJava(javaFolder, javaPackage);
		}
	}

}