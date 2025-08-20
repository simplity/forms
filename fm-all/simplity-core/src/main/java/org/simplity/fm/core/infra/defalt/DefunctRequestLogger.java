// SPDX-License-Identifier: MIT
package org.simplity.fm.core.infra.defalt;

import org.simplity.fm.core.infra.RequestLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author simplity.org
 *
 */
public class DefunctRequestLogger implements RequestLogger {
	private static final Logger logger = LoggerFactory.getLogger(DefunctRequestLogger.class);

	@Override
	public void log(final String loginId, final String serviceName, final String ip, final String input) {
		logger.info("user {} from IP: {} requested for service:{} with data\n{}", loginId, ip, serviceName, input);

	}
}
