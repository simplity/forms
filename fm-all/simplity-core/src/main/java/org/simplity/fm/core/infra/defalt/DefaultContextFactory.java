// SPDX-License-Identifier: MIT
package org.simplity.fm.core.infra.defalt;

import org.simplity.fm.core.DefaultUserContext;
import org.simplity.fm.core.infra.ServiceContextFactory;
import org.simplity.fm.core.service.DefaultServiceContext;
import org.simplity.fm.core.service.OutputData;
import org.simplity.fm.core.service.ServiceContext;

/**
 * @author simplity.org
 *
 */
public class DefaultContextFactory implements ServiceContextFactory {

	@Override
	public ServiceContext newContext(final DefaultUserContext session,
			OutputData outData) {
		return new DefaultServiceContext(session, outData);
	}

	@Override
	public ServiceContext newSessionLessContext(OutputData outData) {
		return new DefaultServiceContext(null, outData);
	}

}
