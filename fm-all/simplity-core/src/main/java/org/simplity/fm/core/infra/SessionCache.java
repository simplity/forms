// SPDX-License-Identifier: MIT
package org.simplity.fm.core.infra;

import org.simplity.fm.core.DefaultUserContext;

/**
 * @author simplity.org
 *
 */
public interface SessionCache {

	/**
	 *
	 * @param id
	 *            non-null unique id/token to the user-session
	 * @param session
	 *            non-null session
	 */
	void put(String id, DefaultUserContext session);

	/**
	 * get a copy of the session that is associated with this session. The
	 * session object may be mutable. However, the cached object is not altered
	 * when the returned object is mutated.
	 *
	 * If the modified session is to be used instead of the old tone, then it
	 * must be cached explicitly with a all to put() method;
	 *
	 * @param id
	 *            unique id/token assigned to this session.
	 * @return user-session for this id. null if the id is not valid, or the
	 *         session has expired and it is
	 *         removed from the cache.
	 */
	DefaultUserContext get(String id);

	/**
	 *
	 * @param id
	 *            unique id/token assigned to this session.
	 * @return user-session that
	 */
	DefaultUserContext remove(String id);

	/**
	 * clear all entries
	 */
	void clear();
}
