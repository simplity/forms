// SPDX-License-Identifier: MIT
package org.simplity.fm.core.job;

/**
 * STatus of a job
 *
 * @author simplity.org
 *
 */
public enum JobStatus {
	/**
	 * waiting in the queue
	 */
	Waiting,
	/**
	 * started running, yet to be completed
	 */
	Running,

	/**
	 * completed successfully
	 */
	Completed,
	/**
	 * job failed to run
	 */
	Failed,
	/**
	 * cancelled by request
	 */
	Cancelled
}
