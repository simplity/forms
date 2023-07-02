/*
 * Copyright (c) 2020 simplity.org
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

package org.simplity.fm.core.job.internal;

import org.simplity.fm.core.job.IJobProgressRecorder;
import org.simplity.fm.core.job.IJobProgressReporter;

/**
 * @author simplity.org
 *
 */
public class JobProgress implements IJobProgressRecorder, IJobProgressReporter {

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
