// SPDX-License-Identifier: MIT
package org.simplity.fm.core.service;

/**
 * Interface for service. The instance is expected to be re-usable, and
 * thread-safe. (immutable). Singleton pattern is suitable or this.
 *
 *
 * @author simplity.org
 *
 */
public interface Service {
	/**
	 * serve when data is requested in a Map
	 *
	 * @param ctx
	 *            service context provides certain data structures and methods.
	 * @param inputData
	 *            non-null, could be empty if no pay-load was received from the
	 *            client
	 * @throws Exception
	 *             so that the caller can wire exceptions to the right exception
	 *             handler that is configured for the app
	 */
	void serve(ServiceContext ctx, InputData inputData) throws Exception;

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
	 * execution. For example, if the job is to process large number of records,
	 * it may be designed to be able to stop after every records..
	 *
	 * @return is this job designed to gracefully abort before completing its
	 *         assigned job?
	 */
	boolean isAbortable();

	/**
	 * should this be run in asynch mode? TODO: requires review to see if this
	 * should be outside of a service.
	 *
	 * @return if true, this service is run through the JobManager. Also, if
	 *         this is accessible to the client-apps, then they need to be aware
	 *         of this to manage the logistics of getting the output, if any
	 *
	 */
	boolean isAsynch();
}
