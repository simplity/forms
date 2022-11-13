package org.simplity.fm.core.app;

import org.simplity.fm.core.infra.IAccessController;
import org.simplity.fm.core.infra.IDbConnectionFactory;
import org.simplity.fm.core.infra.IEmailer;
import org.simplity.fm.core.infra.IExceptionListener;
import org.simplity.fm.core.infra.IRequestLogger;
import org.simplity.fm.core.infra.IServiceContextFactory;
import org.simplity.fm.core.infra.ISessionCache;
import org.simplity.fm.core.infra.ITexter;

/**
 *
 * Data structure with details of how the App is configured to run at this time
 *
 */
public class AppConfigInfo {
	/**
	 * utility method to get an instance of this class from a provider class
	 * name
	 *
	 * @param className
	 *            valid qualified class-name of a class that implements
	 *            <code>IAppCnfigProvider</code>
	 * @return instance if all OK. null otherwise
	 */
	public static AppConfigInfo fromProviderClassName(String className) {
		try {
			Class<?> cls = Class.forName(className);
			Class<?>[] params = {};
			Object c = cls.getConstructor(params).newInstance();
			if (c instanceof IAppConfigProvider == false) {
				return null;
			}
			return ((IAppConfigProvider) c).getConfigInfo();
		} catch (Exception e) {
			return null;
		}

	}
	/**
	 * must be set to a unique name
	 */
	public String appName;
	/**
	 * is the app available to non-authenticated users?
	 *
	 * true if at least one service can be responded without authentication.
	 * false if every service requires authentication
	 */
	public boolean guestsOk;

	/**
	 * whether this server serves services only inside of a session. Relevant
	 * only if guestsOk is true. If guests are not OK, then the server will
	 * always use session
	 *
	 * true if session is a must while requesting any service. false if session
	 * is optional. However, a given service may require session
	 */
	public boolean requireSession;

	/**
	 * root package name used for this app. like "com.myCompany.myApp". In this
	 * case records are found with package name "com.myCompany.myApp.gen.rec"
	 *
	 * If this is not specified, then any request for component will result in
	 * an exception
	 */
	public String appRootPackage;

	/**
	 * optional. Instance that is called to check if the logged-in user is
	 * authorized for the requested service.
	 */
	public IAccessController accessController;

	/**
	 * optional. if not set, any request for db access will result in an
	 * exception
	 */

	public IDbConnectionFactory dbConnectionFactory;

	/**
	 * optional.
	 */
	public IExceptionListener exceptionListener;

	/**
	 * optional. a simple map-based cacher is used. Entries do not expire
	 */
	public ISessionCache sessionCache;

	/**
	 * optional. requests are logged using the underlying logger-framework
	 */

	public IRequestLogger requestLogger;

	/**
	 * optional. if not specified, text messages are just logged.
	 */
	public ITexter texter;

	/**
	 * optional. if not specified, emails are just logged.
	 */
	public IEmailer emailer;

	/**
	 * optional. if not specified, Default context is created
	 */
	public IServiceContextFactory contextFactory;

}
