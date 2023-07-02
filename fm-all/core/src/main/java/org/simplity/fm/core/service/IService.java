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

import org.simplity.fm.core.serialize.IInputObject;

/**
 * Interface for service. The instance is expected to be re-usable, and
 * thread-safe. (immutable). Singleton pattern is suitable or this.
 *
 *
 * @author simplity.org
 *
 */
public interface IService {
	/**
	 * serve when data is requested in a Map
	 *
	 * @param ctx
	 *            service context provides certain data structures and methods.
	 * @param inputObject
	 *            non-null, could be empty if no pay-load was received from the
	 *            client
	 * @throws Exception
	 *             so that the caller can wire exceptions to the right exception
	 *             handler that is configured for the app
	 */
	void serve(IServiceContext ctx, IInputObject inputObject) throws Exception;

	/**
	 *
	 * @return unique name/id of this service
	 */
	String getId();

	/**
	 *
	 * @return true if this service is allowed for non-authenticated users.
	 *         false implies that the service is offered only to authenticated
	 *         users
	 */
	boolean serveGuests();

	/**
	 * A service may be designed to gracefully abort in the middle of its
	 * execution.
	 * For example, if the job is to process large number of records, it may be
	 * designed to be able to stop after every records..
	 *
	 * @return is this job designed to gracefully abort before completing its
	 *         assigned job?
	 */
	boolean isAbortable();

	/**
	 * should this be run in asynch mode?
	 * TODO: requires review to see if this should be outside of a service.
	 *
	 * @return if true, this service is run through the JobManager.
	 *         Also, if this is accessible to the client-apps, then they need to
	 *         be aware of this to manage the logistics of getting the output,
	 *         if any
	 *
	 */
	boolean isAsynch();
}
