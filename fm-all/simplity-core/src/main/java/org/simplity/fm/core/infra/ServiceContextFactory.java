// SPDX-License-Identifier: MIT
package org.simplity.fm.core.infra;

import org.simplity.fm.core.DefaultUserContext;
import org.simplity.fm.core.service.OutputData;
import org.simplity.fm.core.service.ServiceContext;

/**
 * interface for client-code to create a custom IServiceContext or use the
 * default one provided by the framework
 *
 * @author simplity.org
 *
 */
public interface ServiceContextFactory {

	/**
	 * create a service context for the logged-in user
	 *
	 * @param userSession
	 *            non-null
	 * @param outData
	 *            non-null
	 * @return non-null instance of IServiceCOntext that will be passed to the
	 *         service execution thread.
	 */
	ServiceContext newContext(DefaultUserContext userSession, OutputData outData);

	/**
	 * create a service context for the logged-in user
	 *
	 * @param outData
	 *            non-null
	 *
	 * @param userSession
	 *            non-null
	 * @return non-null instance of IServiceCOntext that wil be passed to the
	 *         service execution thread.
	 */
	ServiceContext newSessionLessContext(OutputData outData);

}
