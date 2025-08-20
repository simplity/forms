// SPDX-License-Identifier: MIT
package org.simplity.fm.core.app;

/**
 * Data structure that holds User information. Simplity uses userId. App should
 * extend this class to include all the data about the logged in user that are
 * used across services. Typically this set of data is saved in session
 *
 * We have used an immutable data structure design.
 * 
 * @author simplity.org
 *
 */
public class AppUserInfo {

	/**
	 * logged-in user id
	 */
	protected final String userId;

	/**
	 * @param userId
	 *
	 */
	public AppUserInfo(final String userId) {
		this.userId = userId;
	}

	/**
	 * @return the userId
	 */
	public String getUserId() {
		return this.userId;
	}
}