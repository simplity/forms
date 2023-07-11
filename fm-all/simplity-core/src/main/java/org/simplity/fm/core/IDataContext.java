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

package org.simplity.fm.core;

import java.io.Reader;
import java.io.Writer;

/**
 * Collection of name-data (object) that can be used to share data across units
 * of
 * executions.<br/>
 *
 * In a collaborative computing environment, this may have to be transmitted
 * across system, or may have to be saved/restored
 *
 * @author simplity.org
 *
 */
public interface IDataContext {

	/**
	 *
	 * @param key
	 * @return object associated with this key, null if no such key, or the
	 *         value is null
	 */
	Object getObject(String key);

	/**
	 * put a name-value pair in the context
	 *
	 * @param key
	 *            non-null
	 * @param object
	 *            non-null
	 */
	void setObject(String key, Object object);

	/**
	 * serialize and write for persistence
	 *
	 * @param writer
	 */
	void persist(Writer writer);

	/**
	 *
	 * @param reader
	 * @return true if loaded successfully. false in case of any issue. Error
	 *         message would have been added in case of any failure
	 */
	boolean load(Reader reader);

}
