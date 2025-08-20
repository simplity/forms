// SPDX-License-Identifier: MIT
package org.simplity.fm.core.infra.defalt;

import org.simplity.fm.core.infra.AccessController;
import org.simplity.fm.core.service.Service;
import org.simplity.fm.core.service.ServiceContext;

/**
 * @author simplity.org
 *
 */
public class DefunctAccessController implements AccessController {

	@Override
	public boolean okToServe(final Service service, final ServiceContext ctx) {
		return true;
	}

}
