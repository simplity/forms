package org.simplity.fm.http;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.simplity.fm.core.app.IApp;
import org.simplity.fm.core.app.IAppServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author simplity.org
 *
 */
public class JettyServer extends Server implements IAppServer {

	/**
	 * start a Jetty server as a server-wrapper for an APP
	 *
	 * @param port
	 *            to listen on
	 * @param app
	 * @param restAdapter
	 * @return null in case the Jetty Server could not be started
	 */
	public static IAppServer newServer(int port, IApp app,
			IRestAdapter restAdapter) {

		try {
			JettyServer server = new JettyServer(port);
			server.setHandler(new JettyHandler(app, restAdapter));
			server.start();
			server.join();
			JettyHandler.logger.info(
					"App {} started as a Jetty Server on port {}",
					app.getName(), port);
			return server;
		} catch (Exception e) {
			JettyHandler.logger.error("Error while starting Jetty Server. {}",
					e.getMessage());
			return null;
		}
	}

	/**
	 *
	 * @param port
	 */
	public JettyServer(int port) {
		super(port);
	}

	@Override
	public void shutdown() {
		this.destroy();
	}
}

class JettyHandler extends AbstractHandler {
	protected static final Logger logger = LoggerFactory
			.getLogger(JettyHandler.class);
	private static final int STATUS_METHOD_NOT_ALLOWED = 405;
	private HttpAgent httpAgent;

	JettyHandler(IApp app, IRestAdapter restAdapter) {
		this.httpAgent = new HttpAgent(app, restAdapter);
	}
	@Override
	public void handle(final String target, final Request baseRequest,
			final HttpServletRequest request,
			final HttpServletResponse response)
			throws IOException, ServletException {
		final String method = baseRequest.getMethod().toUpperCase();
		logger.info("Received request path:{} and method {}",
				baseRequest.getPathInfo(), method);
		final long start = System.currentTimeMillis();
		this.httpAgent.setOptions(baseRequest, response);

		if (method.equals("POST") || method.equals("GET")) {
			this.httpAgent.serve(baseRequest, response);
		} else if (method.equals("OPTIONS")) {
			logger.info("Got a pre-flight request. responding generously.. ");
		} else {
			logger.error("Rejected a request with method {}",
					baseRequest.getMethod());
			response.setStatus(STATUS_METHOD_NOT_ALLOWED);
		}

		logger.info("Responded in {}ms", System.currentTimeMillis() - start);
		baseRequest.setHandled(true);
	}
}
