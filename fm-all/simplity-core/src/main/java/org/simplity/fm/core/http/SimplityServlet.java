// SPDX-License-Identifier: MIT
package org.simplity.fm.core.http;

import java.io.IOException;

import org.simplity.fm.core.app.App;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is the entry point for <code>Agent</code> from a web-server (http-server
 * or servlet container like Tomcat or Jetty) <br/>
 * <br/>
 * Our design is to have just one entry point for a web-app. We do not use REST
 * path standards. Instead, a service oriented approach is used , more like an
 * RPC. Name of service is expected as a header field. We use the the paradigm
 * <bold>response = serve(serviceName, request)</bold>
 *
 * @author simplity.org
 *
 */
public class SimplityServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	protected static final int STATUS_METHOD_NOT_ALLOWED = 405;

	private final HttpAgent agent;

	/**
	 *
	 * @param app
	 */

	public SimplityServlet(App app) {
		this.agent = new HttpAgent(app);
	}

	/**
	 * we expect OPTIONS method only as a pre-flight request in a CORS environment.
	 * We have a ready response
	 */
	@Override
	protected void doOptions(final HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException, IOException {
		this.agent.setOptions(req, resp);
	}

	/*
	 * we allow get, post and options. Nothing else
	 */
	@Override
	protected void doDelete(final HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException, IOException {
		resp.setStatus(STATUS_METHOD_NOT_ALLOWED);
	}

	@Override
	protected void doPut(final HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException, IOException {
		resp.setStatus(STATUS_METHOD_NOT_ALLOWED);
	}

	@Override
	protected void doHead(final HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException, IOException {
		resp.setStatus(STATUS_METHOD_NOT_ALLOWED);
	}

	@Override
	protected void doTrace(final HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException, IOException {
		resp.setStatus(STATUS_METHOD_NOT_ALLOWED);
	}

	@Override
	protected void doPost(final HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException, IOException {
		this.agent.serve(req, resp);
	}

	@Override
	protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException, IOException {
		resp.setStatus(STATUS_METHOD_NOT_ALLOWED);
	}
}
