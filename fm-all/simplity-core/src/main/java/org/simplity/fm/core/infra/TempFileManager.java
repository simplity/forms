// SPDX-License-Identifier: MIT
package org.simplity.fm.core.infra;

import java.io.Reader;
import java.io.Writer;

/**
 * @author simplity.org
 *
 */
public interface TempFileManager {

	/**
	 * We use call-back method with the writer to ensure that the resources are
	 * closed.
	 *
	 * @author simplity.org
	 *
	 */
	public interface IFileWriter {
		/**
		 * call-back method invoked by FileManager.
		 *
		 * @param writer
		 * @return true if the file writing was successful. false if the
		 *         operation failed.
		 */
		boolean writeToFile(Writer writer);
	}

	/**
	 * We use call-back method with the writer to ensure that the resources are
	 * closed.
	 *
	 * @author simplity.org
	 *
	 */

	public interface IFileReader {
		/**
		 * call-back from File manager
		 *
		 * @param reader
		 */
		void readFromFile(Reader reader);
	}

	/**
	 *
	 * @param fileName
	 * @return true if it exists
	 */
	boolean fileExists(String fileName);

	/**
	 * create a new file with the content being written by the call-back method.
	 *
	 * @param fileName
	 * @param fileWriter
	 */

	void newFile(String fileName, IFileWriter fileWriter);

	/**
	 *
	 * @param fileName
	 * @param removeAfterRead
	 *            if true, the file is deleted after the reader returns
	 * @return true if the file was indeed opened and read. false if it could
	 *         not be opened for reading.
	 */

	boolean readFile(String fileName, boolean removeAfterRead);

	/**
	 *
	 * @param fileName
	 */

	void removeFile(String fileName);
}
