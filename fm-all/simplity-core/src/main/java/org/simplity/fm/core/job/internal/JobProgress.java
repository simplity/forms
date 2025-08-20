// SPDX-License-Identifier: MIT
package org.simplity.fm.core.job.internal;

import org.simplity.fm.core.job.JobProgressRecorder;
import org.simplity.fm.core.job.JobProgressReporter;

/**
 * @author simplity.org
 *
 */
public class JobProgress implements JobProgressRecorder, JobProgressReporter {

	private int percent;
	private long count;
	private String info = "";

	@Override
	public int getPercentageCompleted() {
		return this.percent;
	}

	@Override
	public long getProgressCount() {
		return this.count;
	}

	@Override
	public String getMessage() {
		return this.info;
	}

	@Override
	public void setPercentageCompleted(final int percentage) {
		this.percent = percentage;
	}

	@Override
	public void getProgressCount(final long progressCount) {
		this.count = progressCount;
	}

	@Override
	public void getMessage(final String message) {
		if (message == null) {
			this.info = "";
		} else {
			this.info = message;
		}
	}

}
