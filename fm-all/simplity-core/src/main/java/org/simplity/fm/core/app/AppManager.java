package org.simplity.fm.core.app;

import org.simplity.fm.core.ApplicationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages instances of Apps. This is actually a static class. However we would
 * like to cater to non-static style of design as well
 *
 * @author simplity.org
 *
 */
public class AppManager {

	protected static final Logger logger = LoggerFactory.getLogger(AppManager.class);

	protected static final String ERROR = "Error in Config Data: ";
	protected static final String NO_MULTI_APP_YET = ERROR
			+ "We are yet to implement multi-app environement. Only one APP can be instantiated";
	protected static final String NO_NAME = ERROR + "Application name is missing";
	protected static final String DUP_NAME = ERROR + "Application name is already in use";

	/**
	 * we will use some technique, like ThreadLocal in a multi-app environment. As
	 * of now we arr working with single app per process
	 */
	// private static final ThreadLocal<IApp> currentApp = new ThreadLocal<>();
	// private static final Map<String, IApp>allApps = new HashMap<>();

	private static App currentApp;

	/**
	 * create a new App instance with the given configuration details. WIll always
	 * return a non-null instance. Throws ApplicationError in case of any error with
	 * the configuration details. Caller must handle the exception
	 *
	 * @param config
	 * @return app non-null instance. Throws Application Error in case of any issue
	 *         with the configuration
	 */
	public static IApp newAppInstance(AppConfig config) {

		/**
		 * we are not yet ready with multi-app design
		 */
		if (currentApp != null) {
			throw new ApplicationError(NO_MULTI_APP_YET);
		}

		String text = config.appName;

		if (text == null || text.isEmpty()) {
			throw new ApplicationError(NO_NAME);
		}

		/**
		 * is it a duplicate? (to be implemented in a multi-app situation
		 */
		// if(allApps.containsKey(config.appName){
		// throw new ApplicationError(DUP_NAME);
		// }
		try {
			App app = new App(config);
			currentApp = app;

			return app;
		} catch (Exception e) {
			logger.error("Error while creating application with config {}. Error: {}", config.appName, e.getMessage());
			return null;
		}

	}

	/**
	 * app instance provides access to all the infrastructure components like
	 * DbDriver as well as app-components like services
	 * 
	 * @return app instance that is associated with this thread of execution. null
	 *         if no app is initiated
	 */
	public static IApp getApp() {
		return currentApp;
	}
}
