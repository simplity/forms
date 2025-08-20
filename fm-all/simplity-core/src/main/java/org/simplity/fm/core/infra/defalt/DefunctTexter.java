// SPDX-License-Identifier: MIT
package org.simplity.fm.core.infra.defalt;

import org.simplity.fm.core.infra.Texter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author simplity.org
 *
 */
public class DefunctTexter implements Texter {
	private static final Logger logger = LoggerFactory.getLogger(DefunctTexter.class);

	@Override
	public void sendText(final String senderId, final String numbers, final String sms) {
		logger.warn("Text messages NOT SENT from:{} to:{},  text:{}", senderId, numbers, sms);
	}

}
