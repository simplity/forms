// SPDX-License-Identifier: MIT
package org.simplity.fm.core.infra;

/**
 * utility to log each request that is served by this app. Responses are not
 * logged at this time.
 *
 * @author simplity.org
 *
 */
public interface RequestLogger {
	/**
	 *
	 * @param loginId
	 * @param serviceName
	 * @param ip          IP address of the requester
	 * @param input       pay-load as received
	 * @param output      response
	 */
	void log(String loginId, String serviceName, String ip, String input);
}
