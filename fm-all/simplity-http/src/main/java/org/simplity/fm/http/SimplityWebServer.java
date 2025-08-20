package org.simplity.fm.http;

import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.simplity.fm.core.app.App;
import org.simplity.fm.core.app.AppConfig;
import org.simplity.fm.core.app.AppManager;
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

	/**
	 * launches the embedded Jetty server with SimplityServlet.
	 *
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		int port = Integer.parseInt(System.getProperty("PORT", System.getenv().getOrDefault("PORT", "8080")));

		String contextPath = System.getProperty("CONTEXT_PATH", "/");
		String mapping = System.getProperty("MAPPING", "/*");

		Server server = new Server(port);

		// EE10 servlet handler for Jakarta Servlet 6.0 apps
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath(contextPath);

		/*
		 * Create an app server instance
		 */
		App app = AppManager.newAppInstance(new AppConfig());
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
		System.out.printf("Jetty started on http://localhost:%d%s (mapping: %s)%n", "" + port, contextPath, mapping);
		server.join();
	}
}
