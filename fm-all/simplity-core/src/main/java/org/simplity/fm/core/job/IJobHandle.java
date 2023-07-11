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

package org.simplity.fm.core.job;

import java.io.Writer;

/**
 * @author simplity.org
 *
 */
public interface IJobHandle {

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
