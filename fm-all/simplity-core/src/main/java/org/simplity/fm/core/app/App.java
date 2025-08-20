// SPDX-License-Identifier: MIT
package org.simplity.fm.core.app;

import java.io.IOException;
import java.io.Writer;

import org.simplity.fm.core.db.DbDriver;
import org.simplity.fm.core.infra.CompProvider;
import org.simplity.fm.core.infra.Emailer;
import org.simplity.fm.core.infra.Texter;
import org.simplity.fm.core.service.InputData;

/**
 * App is the highest level component that responds to service requests. It uses
 * other components to process the request and return a response
 *
 * @author simplity.org
 *
 */
public interface App {
	/**
	 *
	 * @return non-null unique name assigned to this app.
	 */
	String getName();

	/**
	 * is the app available to non-authenticated users?
	 *
	 * @return true if at least one service can be responded without authentication.
	 *         false if every service requires authentication
	 */
	boolean guestsOk();

	/**
	 * @return name of the service to login. null if the App has no such service
	 */
	String getLoginServiceName();

	/**
	 * @return name of the service to logout. null if the App has no such service
	 */
	String getLogoutServiceName();

	/**
	 * safety against large db operation.
	 *
	 * @return number of max rows to be extracted in a db fetch/read operation
	 */
	int getMaxRowsToExtractFromDb();

	/**
	 * nullable db fields are generally bug-prone. We recommend that you avoid them
	 * by using empty string. However, Oracle creates bigger mess by treating
	 * empty-string as null, but not quite that way!!
	 *
	 * @return true if any null text field from db is extracted as empty string
	 */

	boolean treatNullAsEmptyString();

	/**
	 *
	 * @return non-null
	 */

	CompProvider getCompProvider();

	/**
	 *
	 * @return non-null
	 */
	DbDriver getDbDriver();

	/**
	 *
	 * @return non-null
	 */
	Texter getTexter();

	/**
	 *
	 * @return non-null
	 */
	Emailer getEmailer();

	/**
	 * designed to facilitate writing the response directly to the stream. internal
	 * calls can use a StringWriter to get the response as an string
	 *
	 * @param request as per schema for RequestData, that has details like
	 *                sessionId, serviceId and input data
	 * @param writer  to which the response is to be written.
	 * @return non-null response to the request
	 * @throws IOException in case of errors while writing to the supplied writer
	 */
	RequestStatus serve(InputData request, Writer writer) throws IOException;

}
