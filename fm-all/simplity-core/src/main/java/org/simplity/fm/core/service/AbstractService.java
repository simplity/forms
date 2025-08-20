// SPDX-License-Identifier: MIT
package org.simplity.fm.core.service;

/**
 * Simple implementation of IService, with default implementation of methods
 * except serve()
 * Service API is still in its initial stages, and hence is likely to be revised
 * in every new version.
 * Hence it is better to extend this class rather than implement IService.
 *
 * @author simplity.org
 *
 */
public abstract class AbstractService implements Service {
	protected String serviceName;

	protected AbstractService() {
		// allowing extended classes to set serviceName later
	}

	protected AbstractService(final String name) {
		this.serviceName = name;
	}

	@Override
	public String getId() {
		return this.serviceName;
	}

	@Override
	public boolean serveGuests() {
		return false;
	}

	@Override
	public boolean isAbortable() {
		return false;
	}

	@Override
	public boolean isAsynch() {
		return false;
	}

}
