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

package org.simplity.fm.core.app;

import java.io.IOException;
import java.io.Writer;

import org.simplity.fm.core.service.IInputData;

/**
 * App is the highest level component that responds to service requests. It uses
 * other components to process the request and return a response
 *
 * @author simplity.org
 *
 */
public interface IApp {
	/**
	 *
	 * @return non-null unique name assigned to this app.
	 */
	String getName();

	/**
	 * is the app available to non-authenticated users?
	 *
	 * @return true if at least one service can be responded without
	 *         authentication. false if every service requires authentication
	 */
	boolean guestsOk();

	/**
	 * @return name of the service to login. null if the App has no such service
	 */
	String getLoginServiceName();

	/**
	 * @return name of the service to logout. null if the App has no such
	 *         service
	 */
	String getLogoutServiceName();

	/**
	 * designed to facilitate writing the response directly to the stream.
	 * internal calls can use a StringWriter to get the response as an string
	 *
	 * @param request
	 *            as per schema for RequestData, that has details like
	 *            sessionId, serviceId and input data
	 * @param writer
	 *            to which the response is to be written.
	 * @return non-null response to the request
	 * @throws IOException
	 *             in case of errors while writing to the supplied writer
	 */
	RequestStatus serve(IInputData request, Writer writer) throws IOException;

}
