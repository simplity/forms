// SPDX-License-Identifier: MIT
package org.simplity.fm.core.infra;

import org.simplity.fm.core.service.Service;
import org.simplity.fm.core.service.ServiceContext;

/**
 * decide whether the user has access to the requested service
 *
 * @author simplity.org
 */
public interface AccessController {

	/**
	 * @param service
	 * @param ctx
	 * @return true if the user has access, false if prohibited
	 */
	boolean okToServe(Service service, ServiceContext ctx);
}
