// SPDX-License-Identifier: MIT
package org.simplity.fm.core.infra;

/**
 * utility to send a text message to a mobile no
 *
 * @author simplity.org
 *
 */
public interface Emailer {

	/**
	 * send an email. If the toMailIds has more than one ids, then they are all
	 * added to the to-recipients, and a single mail is sent. Refer to
	 * sendBulkMails if you want to send the same content as individual mails to
	 * several ids.
	 *
	 * @param toMailIds
	 *            possibly comma separated list of email ids to which this mail
	 *            is to be sent to.
	 * @param subject
	 * @param content
	 * @param emailSubject
	 * @param EmailContent
	 */

	void sendEmail(final String toMailIds, final String subject,
			final String content);
	/**
	 * send an email to the to-address. From address is part of the
	 * configuration process.
	 *
	 * @param toMailIds
	 *            possibly comma separated list of email ids to which this mail
	 *            is to be sent to. Note that the mail is sent individually to
	 *            each of the recepientIds. for example, if the toMailIds has
	 *            tree ids, then three separate mails are sent
	 * @param subject
	 * @param content
	 * @param emailSubject
	 * @param EmailContent
	 */
	void sendBulkEmails(final String toMailIds, final String subject,
			final String content);
}
