// SPDX-License-Identifier: MIT
package org.simplity.fm.core.infra;

/**
 * utility to send a text message to a mobile no
 * 
 * @author simplity.org
 *
 */
public interface Texter {

	/**
	 * send the sms text to the numbers with the specified sender-id
	 * 
	 * @param senderId
	 * @param numbers
	 *            comma separated mobile numbers
	 * @param sms
	 */
	void sendText(final String senderId, final String numbers, final String sms);
}
