// SPDX-License-Identifier: MIT
package org.simplity.fm.core.infra.defalt;

import org.simplity.fm.core.ApplicationError;
import org.simplity.fm.core.infra.ExceptionListener;
import org.simplity.fm.core.service.ServiceContext;

/**
 * @author simplity.org
 *
 */
public class DefunctExceptionListener implements ExceptionListener {

	@Override
	public void listen(final ServiceContext ctx, final Throwable e) {
		// great listeners just listen :-) :-)

	}

	@Override
	public void listen(final ServiceContext ctx, final ApplicationError e) {
		// great listeners just listen :-) :-)

	}

}
