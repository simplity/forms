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

package org.simplity.fm.core.infra;

import java.io.Reader;

import org.simplity.fm.core.job.IJobHandle;

/**
 * @author simplity.org
 *
 */
public interface IJobManager {

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

	IJobHandle newJob(Reader reader, String serviceName);

	/**
	 * locate the handle to a job that was created earlier. A job may be deleted
	 * after it is run, or may be cleaned-up periodically.
	 *
	 * @param jobId
	 * @return handle to the job. null if no job is found with the id. Either is
	 *         id is wrong, or the job may have been cleaned-up.
	 */
	IJobHandle getJob(String jobId);

}
