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

package org.simplity.fm.core.service;

import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.List;

import org.simplity.fm.core.Message;
import org.simplity.fm.core.UserContext;
import org.simplity.fm.core.data.DbTable;
import org.simplity.fm.core.data.Field;
import org.simplity.fm.core.data.Record;
import org.simplity.fm.core.data.RecordOverride;

/**
 * context for a service execution thread. App specific instance is made
 * available to all components that participate in the service execution path
 *
 * @author simplity.org
 *
 */
public interface IServiceContext {

	/**
	 *
	 * @return true if the service context is associated with a serviceContext,
	 *         false otherwise.
	 */
	boolean hasUserContext();
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
	 *
	 * @return non-null <code>IOutputData</code> for constructing service
	 *         output. To be used for custom-way of creating output instead of
	 *         using the methods available in this class. Once this method is
	 *         called, this class will throw error if any attempt is made to get
	 *         it again, or try adding output data.
	 *
	 *         It is to be noted that the instance in the middle of an object.
	 *         Caller should leave it at that state. That is, a beginObject() is
	 *         already issued, and an endObject() will be issued after the api
	 *         returns.
	 */
	IOutputData getOutputData();

	/**
	 *
	 * @return true if all ok. false if at least one error message is added to
	 *         the context;
	 */
	boolean allOk();

	/**
	 *
	 * @param message
	 *            non-null message
	 */
	void addMessage(Message message);

	/**
	 *
	 * @param messages
	 *            non-null messages
	 */
	void addMessages(Collection<Message> messages);

	/**
	 *
	 * @return non-null array all messages added so far. empty if no message
	 *         added so far;
	 */
	Message[] getMessages();

	/**
	 * @return tenantId, if this APP is designed for multi-tenant deployment,
	 *         and the user context has set a tenant-id. null if it is not set
	 *         in the context.
	 */
	Object getTenantId();

	/**
	 * messages is not necessarily all errors. Some clients may want to track
	 * errors.
	 *
	 * @return number errors accumulated in the context. Note that the count
	 *         gets reset if the messages are reset
	 */
	int getNbrErrors();

	/**
	 * serialize this data as response. Note that this can be called only once
	 * with success. any subsequent call will result an ApplicationError()
	 * exception. Also, this cannot be called after a call to getSerializer() is
	 * called
	 *
	 * @param record
	 *            non-null;
	 */
	void setAsResponse(Record record);

	/**
	 *
	 * serialize this data as response. Note that this can be called only once
	 * with success. any subsequent call will result an ApplicationError()
	 * exception. Also, this cannot be called after a call to getSerializer() is
	 * called
	 *
	 * @param fields
	 * @param objects
	 */
	void setAsResponse(Field[] fields, Object[][] objects);

	/**
	 * serialize this dbTAble as the response
	 *
	 * @param table
	 */
	void setAsResponse(final DbTable<?> table);

	/**
	 * set a header-lines data structure as response
	 *
	 * @param header
	 * @param childName
	 * @param lines
	 */
	void setAsResponse(Record header, String childName, DbTable<?> lines);

	/**
	 * set a header-lines data structure as response
	 *
	 * @param header
	 * @param childName
	 * @param lines
	 */
	void setAsResponse(Record header, String childName,
			List<? extends Record> lines);

	/**
	 *
	 * @return null if no user session is set before this service. non-null user
	 *         session that is set for this user before servicing this service.
	 */
	UserContext getCurrentUserContext();

	/**
	 *
	 * @return null if this service is not setting/resetting user session.
	 *         non-null to set/reset user session after the service is executed
	 */
	UserContext getNewUserContext();

	/**
	 *
	 * @param utx
	 *            non-null user context to be set after the service completes.
	 */
	void setNewUserContext(UserContext utx);

	/**
	 * @param recordName
	 * @return id with which this record is over-ridden. null if it is not
	 *         overridden
	 */
	String getRecordOverrideId(String recordName);

	/**
	 *
	 * @param recordName
	 * @return instance for the current tenant
	 */
	RecordOverride getRecordOverride(String recordName);

	/**
	 * @param formName
	 * @return id with which this form is over-ridden. null if it is not
	 *         overridden
	 */
	String getFormOverrideId(String formName);
	/**
	 *
	 * @return should the user context be reset
	 */
	boolean toResetUserContext();

	/**
	 * mark the user context for reset so that toResetUserContext() will return
	 * true; note that there is no argument. This action can not be reversed!!
	 */
	void markUserContextForReset();
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
