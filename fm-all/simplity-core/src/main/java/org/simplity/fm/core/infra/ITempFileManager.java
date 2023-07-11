/*
 * Copyright (c) 2020 simplity.org
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

package org.simplity.fm.core.infra;

import java.io.Reader;
import java.io.Writer;

/**
 * @author simplity.org
 *
 */
public interface ITempFileManager {

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
