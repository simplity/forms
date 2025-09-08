// SPDX-License-Identifier: MIT
package org.simplity.fm.core;

import java.io.Reader;
import java.io.Writer;

import org.simplity.fm.core.data.RecordOverride;

/**
 * Contains user specific data that services may use. It may also accumulate
 * certain data during the course of a user-session in an online (interactive)
 * environment like an online app.
 *
 * @author simplity.org
 *
 */
public interface UserContext {

	/**
	 *
	 * @param key
	 * @return object associated with this key, null if no such key, or the value is
	 *         null
	 */
	Object getValue(String key);

	/**
	 * put an name-value pair in the context
	 *
	 * @param key   non-null
	 * @param value null has same effect as removing it. hence remove not provided.
	 */
	void setValue(String key, Object value);

	/**
	 * @return non-null user on whose behalf this service is requested.
	 */
	long getUserId();

	/**
	 * @param recordName
	 * @return id with which this record is over-ridden. null if it is not
	 *         overridden
	 */
	String getRecordOverrideId(String recordName);

	/**
	 *
	 * @param recordName
	 * @return instance of recordOverride meta-data
	 */
	RecordOverride getRecordOverride(String recordName);

	/**
	 * @param formName
	 * @return id with which this form is over-ridden. null if it is not overridden
	 */
	String getFormOverrideId(String formName);

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

	/**
	 *
	 * @param jobId
	 */
	void addJob(final String jobId);

	/**
	 *
	 * @param jobId
	 */
	void removeJob(final String jobId);
}
