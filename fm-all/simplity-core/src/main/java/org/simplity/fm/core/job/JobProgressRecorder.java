// SPDX-License-Identifier: MIT
package org.simplity.fm.core.job;

/**
 * records the progress made by a job
 *
 * @author simplity.org
 *
 */
public interface JobProgressRecorder {
	/**
	 *
	 * @param percentage
	 *            percentage of progress. -1 if it in progress, but percentage
	 *            can not be determined
	 */
	void setPercentageCompleted(int percentage);

	/**
	 *
	 * @param count
	 *            relevant number of units of work completed. -1 if it can not
	 *            be determined
	 */
	void getProgressCount(long count);

	/**
	 *
	 * @param message
	 *            informative message.
	 */
	void getMessage(String message);
}
