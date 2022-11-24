/*
 * Copyright (c) 2019 simplity.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.simplity.fm.core.app;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.UUID;

import org.simplity.fm.core.Message;
import org.simplity.fm.core.UserContext;
import org.simplity.fm.core.conf.defalt.DefaultAccessController;
import org.simplity.fm.core.conf.defalt.DefaultCompProvider;
import org.simplity.fm.core.conf.defalt.DefaultContextFactory;
import org.simplity.fm.core.conf.defalt.DefaultDbConFactory;
import org.simplity.fm.core.conf.defalt.DefaultEmailer;
import org.simplity.fm.core.conf.defalt.DefaultExceptionListener;
import org.simplity.fm.core.conf.defalt.DefaultRequestLogger;
import org.simplity.fm.core.conf.defalt.DefaultSessionCacher;
import org.simplity.fm.core.conf.defalt.DefaultTexter;
import org.simplity.fm.core.conf.impl.CompProvider;
import org.simplity.fm.core.data.IDbDriver;
import org.simplity.fm.core.infra.IAccessController;
import org.simplity.fm.core.infra.ICompProvider;
import org.simplity.fm.core.infra.IEmailer;
import org.simplity.fm.core.infra.IExceptionListener;
import org.simplity.fm.core.infra.IRequestLogger;
import org.simplity.fm.core.infra.IServiceContextFactory;
import org.simplity.fm.core.infra.ISessionCache;
import org.simplity.fm.core.infra.ITexter;
import org.simplity.fm.core.json.JsonUtil;
import org.simplity.fm.core.rdb.RdbDriver;
import org.simplity.fm.core.service.IInputData;
import org.simplity.fm.core.service.IOutputData;
import org.simplity.fm.core.service.IService;
import org.simplity.fm.core.service.IServiceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is designed as an internal class, accessible to this package. It is
 * instantiated by AppManager. AppManager exposes this as an instance of IAapp
 * to the external (Request) side, while the same instance is exposed as
 * IAppInfra to the down-stream components
 *
 */
class App implements IApp, IAppInfra {

	/*
	 * tag/property/member name
	 */
	private static final String TAG_SESSION_ID = "sessionId";
	private static final String TAG_SERVICE_NAME = "serviceName";
	private static final String TAG_STATUS = "status";
	private static final String TAG_STATUS_DESC = "statusDescription";
	private static final String TAG_DATA = "data";

	protected static final Logger logger = LoggerFactory.getLogger(App.class);

	private final String appName;
	private final String loginServiceName;
	private final String logoutServiceName;
	private final boolean serveGuests;
	private final ICompProvider compProvider;
	private final IAccessController guard;
	private final IDbDriver rdbDriver;
	private final IExceptionListener listener;
	private final ISessionCache cache;
	private final IRequestLogger reqLogger;
	private final ITexter texter;
	private final IEmailer emailer;
	private final IServiceContextFactory contextFactory;

	/**
	 * configure the app
	 *
	 * @param config
	 * @throws Exception
	 */
	App(final AppConfigInfo config) throws Exception {
		this.appName = config.appName;
		this.serveGuests = config.guestsOk;
		this.loginServiceName = config.loginServiceName;
		this.logoutServiceName = config.loginServiceName;

		String text = config.appRootPackage;

		if (text == null || text.isEmpty()) {
			logger.error(
					"root package name is required to locate app components. This app will throw exception if any component is requested");
			this.compProvider = new DefaultCompProvider();
		} else {
			this.compProvider = CompProvider.getPrivider(text);
			if (this.compProvider == null) {
				throw new Exception(
						"Error while initializing comp provider using root package "
								+ text);
			}
		}

		if (config.accessController == null) {
			logger.warn(
					"No access controller configured. All services granted for all users");
			this.guard = new DefaultAccessController();
		} else {
			this.guard = config.accessController;
		}

		if (config.dbConnectionFactory == null) {
			logger.warn("No DB connection configured. No db access");
			this.rdbDriver = new RdbDriver(new DefaultDbConFactory());
		} else {
			this.rdbDriver = new RdbDriver(config.dbConnectionFactory);
		}

		if (config.exceptionListener == null) {
			logger.warn(
					"No exception listener configured. All exceptions will just be logged before responding to the client");
			this.listener = new DefaultExceptionListener();
		} else {
			this.listener = config.exceptionListener;
		}

		if (config.sessionCache == null) {
			logger.warn(
					"No Session Cacher controller configured. local caching arranged instead..");
			this.cache = new DefaultSessionCacher();
		} else {
			this.cache = config.sessionCache;
		}

		if (config.requestLogger == null) {
			logger.warn(
					"No Request logger configured. requests will be merged with general logging..");
			this.reqLogger = new DefaultRequestLogger();
		} else {
			this.reqLogger = config.requestLogger;
		}

		if (config.texter == null) {
			logger.warn(
					"SMS texts can not be sent as the facility is not configured. SMS text will insted be just logged");
			this.texter = new DefaultTexter();
		} else {
			this.texter = config.texter;
		}

		if (config.contextFactory == null) {
			logger.warn(
					"No custom factory is defined to create service context. A default one is used");
			this.contextFactory = new DefaultContextFactory();
		} else {
			this.contextFactory = config.contextFactory;
		}

		if (config.emailer == null) {
			logger.warn(
					"No custom factory is defined to create service context. A default one is used");
			this.emailer = new DefaultEmailer();
		} else {
			this.emailer = config.emailer;
		}
	}

	@Override
	public int getMaxRowsToExtractFromDb() {
		return 0;
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
	public ICompProvider getCompProvider() {
		return this.compProvider;
	}

	@Override
	public IDbDriver getDbDriver() {
		return this.rdbDriver;
	}

	@Override
	public ITexter getTexter() {
		return this.texter;
	}

	@Override
	public IEmailer getEmailer() {
		return this.emailer;
	}

	@Override
	public boolean guestsOk() {
		return this.serveGuests;
	}

	@Override
	public RequestStatus serve(IInputData inData, Writer writer)
			throws IOException {
		IServiceContext ctx = null;
		try {
			String serviceName = inData.getString(TAG_SERVICE_NAME);
			if (serviceName == null || serviceName.isEmpty()) {
				logger.error("Attribute named {} is required for service name",
						TAG_SERVICE_NAME);
				return writeErrorResponse(RequestStatus.ServiceNameRequired,
						writer);
			}

			UserContext utx = null;
			String userId = null;
			String sessionId = inData.getString(TAG_SESSION_ID);
			if (sessionId != null && sessionId.isEmpty()) {
				sessionId = null;
			}
			StringWriter stringWriter = new StringWriter();
			IOutputData outData = JsonUtil.newOutputData(stringWriter);
			outData.beginObject();

			if (sessionId == null) {
				// login service is allowed always
				if (this.guestsOk()
						|| serviceName.equals(this.getLoginServiceName())) {
					ctx = this.contextFactory.newSessionLessContext(outData);
				} else {
					return writeErrorResponse(RequestStatus.SessionRequired,
							writer);
				}
			} else {
				utx = this.cache.get(sessionId);
				if (utx == null) {
					logger.error("sessionId {} has no attached cache.",
							sessionId);
					return writeErrorResponse(RequestStatus.NoSuchSession,
							writer);
				}
				userId = utx.getUserId();
				ctx = this.contextFactory.newContext(utx, outData);
			}

			IService service = this.compProvider.getService(serviceName, ctx);

			if (service == null) {
				return writeErrorResponse(RequestStatus.NoSuchService, writer);
			}

			if (service.serveGuests() == false
					&& (userId == null || userId.isEmpty())) {
				return writeErrorResponse(RequestStatus.NoSuchService, writer);
			}

			if (this.guard.okToServe(service, ctx) == false) {
				return writeErrorResponse(RequestStatus.NoSuchService, writer);
			}

			IInputData data = inData.getData(TAG_DATA);
			if (data == null) {
				data = JsonUtil.newInputData();
			}

			this.reqLogger.log(userId, serviceName, inData.toString());

			outData.addName(TAG_DATA).beginObject();
			service.serve(ctx, data);
			outData.endObject();

			RequestStatus status = ctx.allOk()
					? RequestStatus.Served
					: RequestStatus.ServedWithErrors;
			outData.addName(TAG_STATUS).addValue(status.getMessageId());

			if (sessionId != null && ctx.toResetUserContext()) {
				this.cache.remove(sessionId);
			}

			UserContext newCtx = ctx.getNewUserContext();
			if (newCtx != null) {
				if (sessionId != null) {
					this.cache.remove(sessionId);
				}
				String token = UUID.randomUUID().toString();
				this.cache.put(token, newCtx);
				outData.addName(TAG_SESSION_ID).addValue(token);
			}

			Message[] messages = ctx.getMessages();
			if (messages != null && messages.length > 0) {
				writeMessages(messages, outData);
			}

			outData.endObject();
			writer.write(stringWriter.toString());
			return status;

		} catch (Exception | Error e) {
			logger.error("Service {} threw an exception: " + e.getMessage());
			e.printStackTrace();
			this.listener.listen(ctx, e);
			return writeErrorResponse(RequestStatus.ServerError, writer);
		}

	}

	private static void writeMessages(Message[] messages, IOutputData outData) {
		outData.addName("messages").beginArray();
		for (Message msg : messages) {
			msg.toOutputData(outData);
		}
		outData.endArray();
	}

	private static RequestStatus writeErrorResponse(RequestStatus status,
			Writer outWriter) throws IOException {

		StringWriter stringWriter = new StringWriter();
		IOutputData outData = JsonUtil.newOutputData(stringWriter);
		outData.beginObject();

		String messageId = status.getMessageId();
		outData.addName(TAG_STATUS).addValue(messageId);
		outData.addName(TAG_STATUS_DESC).addValue(status.getDescription());
		Message[] messages = {Message.newError(messageId)};
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