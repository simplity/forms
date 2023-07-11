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

import org.simplity.fm.core.data.RecordOverride;

/**
 * Contains user specific data that services may use. It may also accumulate
 * certain data during the course of a user-session in an online (interactive)
 * environment like an online app.
 *
 * @author simplity.org
 *
 */
public interface IUserContext {

	/**
	 *
	 * @param key
	 * @return object associated with this key, null if no such key, or teh
	 *         value is null
	 */
	Object getValue(String key);

	/**
	 * put an name-value pair in the context
	 *
	 * @param key
	 *            non-null
	 * @param value
	 *            null has same effect as removing it. hence remove not
	 *            provided.
	 */
	void setValue(String key, Object value);

	/**
	 * @return non-null user on whose behalf this service is requested. Note
	 *         that this id COULD be different from the userId used by the
	 *         client-facing UserContext. For example, the app may use a mail-id
	 *         as userId for logging in, but may use a numeric userId internally
	 *         as the unique userId. In this case UserContext uses mail-id
	 *         (string) as userId while ServiceCOntext uses internalId (long) as
	 *         userId. <br />
	 *         Also, If a service is allowed for non-registered users, the app
	 *         may use a a specific (hard-coded) userId for any session for such
	 *         a user
	 */
	Object getUserId();

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
	 * @return id with which this form is over-ridden. null if it is not
	 *         overridden
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
