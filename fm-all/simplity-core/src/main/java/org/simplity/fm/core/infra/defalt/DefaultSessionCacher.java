// SPDX-License-Identifier: MIT
package org.simplity.fm.core.infra.defalt;

import java.util.HashMap;
import java.util.Map;

import org.simplity.fm.core.DefaultUserContext;
import org.simplity.fm.core.infra.SessionCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * a stand-in cacher that is nothing but a map.
 *
 * @author simplity.org
 *
 */
public class DefaultSessionCacher implements SessionCache {
	private static final Logger logger = LoggerFactory.getLogger(DefaultSessionCacher.class);
	private final Map<String, DefaultUserContext> sessions = new HashMap<>();

	@Override
	public void put(final String id, final DefaultUserContext session) {
		if (id == null) {
			logger.error("Caching not possible for a null key.");
		} else if (session == null) {
			logger.error("Null sessions are not cached.");
		} else {
			this.sessions.put(id, session);
		}
	}

	@Override
	public DefaultUserContext get(final String id) {
		if (id == null) {
			logger.error("key is to be non-null for a get().");
			return null;
		}
		return this.sessions.get(id);
	}

	@Override
	public DefaultUserContext remove(final String id) {
		if (id == null) {
			logger.error("key is to be non-null for a remove().");
			return null;
		}
		return this.sessions.remove(id);
	}

	@Override
	public void clear() {
		logger.info("Sessions cleared");
		this.sessions.clear();
	}

}
