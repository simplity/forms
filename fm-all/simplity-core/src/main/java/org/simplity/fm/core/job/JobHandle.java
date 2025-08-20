// SPDX-License-Identifier: MIT
package org.simplity.fm.core.job;

import java.io.Writer;

/**
 * @author simplity.org
 *
 */
public interface JobHandle {

	/**
	 *
	 * @return id of this job. Can be used to get access the handle later
	 */

	String getId();

	/**
	 *
	 * @return current status of the job
	 */
	JobStatus getStatus();

	/**
	 * cancel the job if it is still waiting. Optionally try to abort it if it
	 * is running
	 *
	 * @param abortIfRunning
	 *            used if the job is running. if true, the job is aborted, if it
	 *            designed for such an operation
	 * @return true if it is cancelled. false if the job is already run, or if
	 *         it could not be aborted
	 */

	boolean cancelJob(boolean abortIfRunning);

	/**
	 * write the output, and remove the output
	 *
	 * @param writer
	 * @return true if the output is written, and the output is deleted. False
	 *         if the job has (not yet) produced any output, or if it is already
	 *         written out and deleted.
	 */
	boolean writeOutput(Writer writer);

	/**
	 * copy the output. Output is retained for subsequent access.
	 *
	 * @param writer
	 * @return true if copied. false if output is not available
	 */
	boolean copyOutput(Writer writer);

	/**
	 * run the job
	 *
	 * @return true if the job was started successfully. false if it was not in
	 *         a state to start.
	 */
	boolean start();
}
