// SPDX-License-Identifier: MIT
package org.simplity.fm.core.job;

/**
 * represents the progress made by a job
 *
 * @author simplity.org
 *
 */
public interface JobProgressReporter {
	/**
	 *
	 * @return percentage of progress. -1 if it in progress, but percentage can
	 *         not be determined
	 */
	int getPercentageCompleted();

	/**
	 *
	 * @return relevant number of units of work completed. -1 if it can not be
	 *         determined
	 */
	long getProgressCount();

	/**
	 *
	 * @return informative message. status text if the job does not report any
	 *         progress message
	 */
	String getMessage();
}
