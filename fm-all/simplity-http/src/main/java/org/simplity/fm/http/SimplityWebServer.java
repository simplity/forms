package org.simplity.fm.http;

import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.simplity.fm.core.app.App;
import org.simplity.fm.core.http.SimplityServlet;

/**
 * Starts an embedded Jetty 12 server (EE10 / Servlet 6.0) and maps
 * SimplityServlet.
 *
 * Configurable via system properties or env: - PORT (env or -DPORT): HTTP port
 * (default 8080) - CONTEXT_PATH (-DCONTEXT_PATH): context path (default "/") -
 * MAPPING (-DMAPPING): servlet mapping (default "/*")
 */

public class SimplityWebServer {
	private SimplityWebServer() {
		// this is a utility class. No instance
	}

	/**
	 * launches the embedded Jetty server with SimplityServlet.
	 *
	 * @param app application instance to be used as the back-end
	 * @return instance of the server that has been started
	 * @throws Exception
	 */
	public static Server startServer(App app) throws Exception {
		return startServer(app, 0);
	}

	/**
	 * launches the embedded Jetty server with SimplityServlet.
	 *
	 * @param app       application instance to be used as the back-end
	 * @param portToUse if > 0, this port is used. If 0, port is read from system
	 *                  property or env variable PORT (default 8080)
	 * @return instance of the server that has been started
	 * @throws Exception
	 */

	public static Server startServer(App app, int portToUse) throws Exception {
		int port = portToUse;
		if (port == 0) {
			port = Integer.parseInt(System.getProperty("PORT", System.getenv().getOrDefault("PORT", "8080")));
		}

		String contextPath = System.getProperty("CONTEXT_PATH", "/");
		String mapping = System.getProperty("MAPPING", "/*");

		Server server = new Server(port);

		// EE10 servlet handler for Jakarta Servlet 6.0 apps
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath(contextPath);

		/*
		 * Register simplity servlet
		 */
		ServletHolder holder = new ServletHolder("simplity", new SimplityServlet(app));
		context.addServlet(holder, mapping);

		server.setHandler(context);

		// Graceful shutdown on SIGTERM/Ctrl-C
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				server.stop();
				server.join();
			} catch (Exception ignored) {
				//
			}
		}));

		server.start();
		System.out.println("Jetty started on http://localhost:" + port + contextPath);
		server.join();
		return server;
	}
}
