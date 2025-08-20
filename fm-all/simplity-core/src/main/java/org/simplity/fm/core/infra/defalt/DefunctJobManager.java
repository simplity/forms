// SPDX-License-Identifier: MIT
package org.simplity.fm.core.infra.defalt;

import java.io.Reader;

import org.simplity.fm.core.infra.JobManager;
import org.simplity.fm.core.job.JobHandle;

/**
 * @author simplity.org
 *
 */
public class DefunctJobManager implements JobManager {

	@Override
	public JobHandle newJob(final Reader reader, final String serviceName) {
		return null;
	}

	@Override
	public JobHandle getJob(final String jobId) {
		return null;
	}

}
