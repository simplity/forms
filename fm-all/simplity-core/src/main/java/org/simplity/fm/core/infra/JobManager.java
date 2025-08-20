// SPDX-License-Identifier: MIT
package org.simplity.fm.core.infra;

import java.io.Reader;

import org.simplity.fm.core.job.JobHandle;

/**
 * @author simplity.org
 *
 */
public interface JobManager {

	/**
	 * create a job and return a handle to it. Throws application error in case
	 * the job was not be created
	 *
	 * @param reader
	 *            from which to read the input data
	 * @param serviceName
	 *            service to be executed
	 * @return Handle for the new Job that is created.
	 */

	JobHandle newJob(Reader reader, String serviceName);

	/**
	 * locate the handle to a job that was created earlier. A job may be deleted
	 * after it is run, or may be cleaned-up periodically.
	 *
	 * @param jobId
	 * @return handle to the job. null if no job is found with the id. Either is
	 *         id is wrong, or the job may have been cleaned-up.
	 */
	JobHandle getJob(String jobId);

}
