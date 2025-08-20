// SPDX-License-Identifier: MIT
package org.simplity.fm.core.infra.defalt;

import org.simplity.fm.core.infra.Emailer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author simplity.org
 *
 */
public class DefunctEmailer implements Emailer {
	private static final Logger logger = LoggerFactory.getLogger(DefunctEmailer.class);

	@Override
	public void sendEmail(final String toMailIds, final String subject, final String content) {
		logger.warn("Emailer not configured. Message NOT SENT. to:{}, subject:{}, text:{}", toMailIds, subject, content);
	}

	@Override
	public void sendBulkEmails(final String toMailIds, final String subject, final String content) {
		logger.warn("Emailer not configured. Message NOT SENT. to:{}, subject:{}, text:{}", toMailIds, subject, content);
	}

}
