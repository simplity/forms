// SPDX-License-Identifier: MIT
package org.simplity.fm.core.app;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.UUID;

import org.simplity.fm.core.Conventions;
import org.simplity.fm.core.DefaultUserContext;
import org.simplity.fm.core.Message;
import org.simplity.fm.core.db.DbDriver;
import org.simplity.fm.core.infra.AccessController;
import org.simplity.fm.core.infra.CompProvider;
import org.simplity.fm.core.infra.Emailer;
import org.simplity.fm.core.infra.ExceptionListener;
import org.simplity.fm.core.infra.RequestLogger;
import org.simplity.fm.core.infra.ServiceContextFactory;
import org.simplity.fm.core.infra.SessionCache;
import org.simplity.fm.core.infra.Texter;
import org.simplity.fm.core.infra.defalt.DefaultCompProvider;
import org.simplity.fm.core.infra.defalt.DefaultContextFactory;
import org.simplity.fm.core.infra.defalt.DefaultSessionCacher;
import org.simplity.fm.core.infra.defalt.DefunctAccessController;
import org.simplity.fm.core.infra.defalt.DefunctCompProvider;
import org.simplity.fm.core.infra.defalt.DefunctDbConFactory;
import org.simplity.fm.core.infra.defalt.DefunctEmailer;
import org.simplity.fm.core.infra.defalt.DefunctExceptionListener;
import org.simplity.fm.core.infra.defalt.DefunctRequestLogger;
import org.simplity.fm.core.infra.defalt.DefunctTexter;
import org.simplity.fm.core.jdbc.JdbcDriver;
import org.simplity.fm.core.json.JsonUtil;
import org.simplity.fm.core.service.InputData;
import org.simplity.fm.core.service.OutputData;
import org.simplity.fm.core.service.Service;
import org.simplity.fm.core.service.ServiceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is designed as an internal class, accessible to this package. It is
 * instantiated by AppManager. AppManager exposes this as an instance of IAapp
 * to the external (Request) side, while the same instance is exposed as
 * IAppInfra to the down-stream components
 *
 */
class DefaultApp implements App {

	/*
	 * tag/property/member name
	 */
	private static final String TAG_SESSION_ID = Conventions.Http.SESSION_ID_FIELD_NAME;
	private static final String TAG_SERVICE = "service";
	private static final String TAG_STATUS = "status";
	private static final String TAG_STATUS_DESC = "statusDescription";
	private static final String TAG_DATA = "data";

	protected static final Logger logger = LoggerFactory.getLogger(DefaultApp.class);

	private final String appName;
	private final String loginServiceName;
	private final String logoutServiceName;
	private final boolean serveGuests;
	private final CompProvider compProvider;
	private final AccessController guard;
	private final DbDriver rdbDriver;
	private final ExceptionListener listener;
	private final SessionCache cache;
	private final RequestLogger reqLogger;
	private final Texter texter;
	private final Emailer emailer;
	private final ServiceContextFactory contextFactory;
	private int maxRowsForFilter = 10000;

	/**
	 * configure the app
	 *
	 * @param config
	 * @throws Exception
	 */
	DefaultApp(final AppConfig config) throws Exception {
		this.appName = config.appName;
		this.serveGuests = config.guestsOk;
		this.loginServiceName = config.loginServiceName;
		this.logoutServiceName = config.loginServiceName;
		if (config.maxRowsForFilter != 0) {
			this.maxRowsForFilter = config.maxRowsForFilter;
		}

		String text = config.appRootPackage;

		if (text == null || text.isEmpty()) {
			logger.error(
					"root package name is required to locate app components. This app will throw exception if any component is requested");
			this.compProvider = new DefunctCompProvider();
		} else {
			this.compProvider = new DefaultCompProvider(text);
			if (this.compProvider == null) {
				throw new Exception("Error while initializing comp provider using root package " + text);
			}
		}

		if (config.accessController == null) {
			logger.warn("No access controller configured. All services granted for all users");
			this.guard = new DefunctAccessController();
		} else {
			this.guard = config.accessController;
		}

		if (config.dbConnectionFactory == null) {
			logger.warn("No DB connection configured. No db access");
			this.rdbDriver = new JdbcDriver(new DefunctDbConFactory());
		} else {
			this.rdbDriver = new JdbcDriver(config.dbConnectionFactory);
		}

		if (config.exceptionListener == null) {
			logger.warn(
					"No exception listener configured. All exceptions will just be logged before responding to the client");
			this.listener = new DefunctExceptionListener();
		} else {
			this.listener = config.exceptionListener;
		}

		if (config.sessionCache == null) {
			logger.warn("No Session Cacher controller configured. local caching arranged instead..");
			this.cache = new DefaultSessionCacher();
		} else {
			this.cache = config.sessionCache;
		}

		if (config.requestLogger == null) {
			logger.warn("No Request logger configured. requests will be merged with general logging..");
			this.reqLogger = new DefunctRequestLogger();
		} else {
			this.reqLogger = config.requestLogger;
		}

		if (config.texter == null) {
			logger.warn(
					"SMS texts can not be sent as the facility is not configured. SMS text will insted be just logged");
			this.texter = new DefunctTexter();
		} else {
			this.texter = config.texter;
		}

		if (config.contextFactory == null) {
			logger.warn("No custom factory is defined to create service context. A default one is used");
			this.contextFactory = new DefaultContextFactory();
		} else {
			this.contextFactory = config.contextFactory;
		}

		if (config.emailer == null) {
			logger.warn("No custom factory is defined to create service context. A default one is used");
			this.emailer = new DefunctEmailer();
		} else {
			this.emailer = config.emailer;
		}
	}

	@Override
	public int getMaxRowsToExtractFromDb() {
		return this.maxRowsForFilter;
	}

	@Override
	public boolean treatNullAsEmptyString() {
		return false;
	}

	@Override
	public String getName() {
		return this.appName;
	}

	@Override
	public CompProvider getCompProvider() {
		return this.compProvider;
	}

	@Override
	public DbDriver getDbDriver() {
		return this.rdbDriver;
	}

	@Override
	public Texter getTexter() {
		return this.texter;
	}

	@Override
	public Emailer getEmailer() {
		return this.emailer;
	}

	@Override
	public boolean guestsOk() {
		return this.serveGuests;
	}

	@SuppressWarnings("boxing")
	@Override
	public RequestStatus serve(InputData inData, Writer writer) throws IOException {
		ServiceContext ctx = null;
		String serviceName = inData.getString(TAG_SERVICE);
		if (serviceName == null || serviceName.isEmpty()) {
			logger.error("Attribute named {} is required for service name", TAG_SERVICE);
			return writeErrorResponse(RequestStatus.ServiceNameRequired, writer);
		}
		try {

			DefaultUserContext utx = null;
			long userId = 0;
			String sessionId = inData.getString(TAG_SESSION_ID);
			if (sessionId != null && sessionId.isEmpty()) {
				sessionId = null;
			}

			StringWriter stringWriter = new StringWriter();
			OutputData outData = JsonUtil.newOutputData(stringWriter);
			outData.beginObject();

			// get user context
			if (sessionId != null) {
				utx = this.cache.get(sessionId);
				if (utx == null) {
					logger.info("SessionId {} not found in cache. May be timed-out", sessionId);
					sessionId = null;
				}
			}

			if (utx == null) {
				ctx = this.contextFactory.newSessionLessContext(outData);
			} else {
				userId = utx.getUserId();
				ctx = this.contextFactory.newContext(utx, outData);
				logger.info("Session for user-id {} retrieved", userId);
			}

			Service service = this.compProvider.getService(serviceName, ctx);

			if (service == null) {
				return writeErrorResponse(RequestStatus.NoSuchService, writer);
			}

			if (service.serveGuests() == false && userId == 0) {
				return writeErrorResponse(RequestStatus.SessionRequired, writer);
			}

			if (this.guard.okToServe(service, ctx) == false) {
				return writeErrorResponse(RequestStatus.NoSuchService, writer);
			}

			InputData data = inData.getData(TAG_DATA);
			if (data == null) {
				data = JsonUtil.newInputData();
			}

			/**
			 * make the IP available to the service as well
			 */
			String ip = inData.getString(Conventions.Http.CLIENT_IP_FIELD_NAME);
			ctx.setValue(Conventions.Http.CLIENT_IP_FIELD_NAME, ip);
			ctx.setValue(Conventions.Http.SESSION_ID_FIELD_NAME, sessionId);

			this.reqLogger.log("" + userId, serviceName, ip, inData.toString());

			outData.addName(TAG_DATA).beginObject();
			service.serve(ctx, data);
			outData.endObject();

			RequestStatus status = ctx.allOk() ? RequestStatus.Completed : RequestStatus.CompletedWithErrors;
			outData.addName(TAG_STATUS).addValue(status.getMessageId());

			if (sessionId != null && ctx.toResetUserContext()) {
				this.cache.remove(sessionId);
			}

			DefaultUserContext newCtx = ctx.getNewUserContext();
			if (newCtx != null) {
				if (sessionId != null) {
					this.cache.remove(sessionId);
				}
				sessionId = UUID.randomUUID().toString();
				this.cache.put(sessionId, newCtx);
				outData.addName(TAG_SESSION_ID).addValue(sessionId);
				logger.info("Session created for user {}", newCtx.getUserId());
			}

			Message[] messages = ctx.getMessages();
			if (messages != null && messages.length > 0) {
				writeMessages(messages, outData);
			}

			outData.endObject();
			writer.write(stringWriter.toString());
			return status;

		} catch (Exception | Error e) {
			logger.error("Service {} threw an exception: {} ", serviceName);
			e.printStackTrace();
			this.listener.listen(ctx, e);
			return writeErrorResponse(RequestStatus.ServerError, writer);
		}

	}

	private static void writeMessages(Message[] messages, OutputData outData) {
		outData.addName("messages").beginArray();
		for (Message msg : messages) {
			msg.toOutputData(outData);
		}
		outData.endArray();
	}

	private static RequestStatus writeErrorResponse(RequestStatus status, Writer outWriter) throws IOException {

		StringWriter stringWriter = new StringWriter();
		OutputData outData = JsonUtil.newOutputData(stringWriter);
		outData.beginObject();

		String messageId = status.getMessageId();
		outData.addName(TAG_STATUS).addValue(messageId);
		outData.addName(TAG_STATUS_DESC).addValue(status.getDescription());
		Message[] messages = { Message.newError(messageId) };
		writeMessages(messages, outData);

		outData.endObject();

		outWriter.write(stringWriter.toString());
		return status;

	}

	@Override
	public String getLoginServiceName() {
		return this.loginServiceName;
	}

	@Override
	public String getLogoutServiceName() {
		return this.logoutServiceName;
	}

}