// SPDX-License-Identifier: MIT
package org.simplity.fm.core.infra;

import org.simplity.fm.core.ApplicationError;
import org.simplity.fm.core.service.ServiceContext;

/**
 * utility to trigger actions to investigate into this exception
 *
 * @author simplity.org
 */
public interface ExceptionListener {

	/**
	 * typically run-time exception that is raised by the platform/VM and is not
	 * raised explicitly by the frame-work or App code
	 *
	 * @param ctx
	 *            could be null if the exception is detected outside of a
	 *            service context.
	 *
	 * @param e
	 *            exception that is reported when we have no context as in input
	 *            or service data
	 */
	void listen(ServiceContext ctx, Throwable e);

	/**
	 * triggered when an ApplicationError is raised. This is generally raised by
	 * the code when a design error is encountered. That is, this exception is
	 * explicitly raised by the framework or the App code.
	 *
	 * @param ctx
	 *            could be null if the exception is detected outside of a
	 *            service context.
	 *
	 * @param e
	 *            exception that is reported when we have no context as in input
	 *            or service data
	 */
	void listen(ServiceContext ctx, ApplicationError e);
}
