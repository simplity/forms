// SPDX-License-Identifier: MIT
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
public interface DataContext {

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
