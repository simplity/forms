// SPDX-License-Identifier: MIT
package org.simplity.fm.core;

/**
 * Base unchecked exception that represents an exception caused because of some
 * flaw in the internal design/implementation of the app. for example
 * inconsistent data in the DB, incompatible arguments etc..
 *
 * The framework throws this whenever it detects exceptions that are not
 * supposed to happen by design. It must be caught at the highest level, say
 * service agent.
 *
 * Motivation for this design is to provide a mechanism for the app to centrally
 * handle all such exceptions and plumb it to the org-wide infrastructure.
 *
 * Applications are encouraged to create sub-classes for better error-management
 */
public class ApplicationError extends Error {
	private static final long serialVersionUID = 1L;

	/**
	 *
	 * @param msg
	 */
	public ApplicationError(final String msg) {
		super(msg);
	}

	/**
	 *
	 * @param msg
	 * @param e
	 */
	public ApplicationError(final String msg, final Throwable e) {
		super(msg, e);
	}
}
