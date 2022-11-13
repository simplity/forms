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

import java.io.StringWriter;

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
import org.simplity.fm.core.service.IServiceResponse;
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

	protected static final Logger logger = LoggerFactory.getLogger(App.class);

	private final String appName;
	private final boolean serveGuests;
	private final boolean sessionIsAMust;
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
		this.sessionIsAMust = config.requireSession;

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
	public boolean requireSession() {
		return this.sessionIsAMust;
	}

	@Override
	public String createSession(String userId, String password) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IServiceResponse serve(IInputData inputData) {
		return new Assistant(inputData).serve();
	}

	class Assistant {
		private final IInputData inData;
		private StringWriter writer;
		private IOutputData outData;

		protected Assistant(final IInputData inData) {
			this.inData = inData;
		}

		protected IServiceResponse serve() {

			this.initOutput();
			IServiceContext ctx = null;
			try {
				String serviceName = this.inData.getString("serviceName");
				if (serviceName == null || serviceName.isEmpty()) {
					return this
							.errorResponse(RequestStatus.ServiceNameRequired);
				}

				UserContext utx = null;
				String userId = null;
				String sessionId = this.inData.getString("sessionId");

				if (sessionId == null) {
					if (App.this.sessionIsAMust) {
						return errorResponse(RequestStatus.SessionRequired);
					}
					ctx = App.this.contextFactory
							.newSessionLessContext(this.outData);
				} else {
					utx = App.this.cache.get(sessionId);
					if (utx == null) {
						return errorResponse(RequestStatus.NoSuchSession);
					}
					userId = utx.getUserId();
					ctx = App.this.contextFactory.newContext(utx, this.outData);
				}

				IService service = App.this.compProvider.getService(serviceName,
						ctx);

				if (service == null) {
					return errorResponse(RequestStatus.NoSuchService);
				}

				if (service.serveGuests() == false
						&& (userId == null || userId.isEmpty())) {
					return errorResponse(RequestStatus.NoSuchService);
				}

				if (App.this.guard.okToServe(service, ctx) == false) {
					return errorResponse(RequestStatus.NoSuchService);
				}

				IInputData data = this.inData.getData("data");
				if (data == null) {
					data = JsonUtil.newInputObject();
				}

				App.this.reqLogger.log(userId, serviceName, serviceName);

				this.outData.beginObject().addName("data");
				service.serve(ctx, inData);
				this.outData.endObject();
				RequestStatus status = ctx.allOk()
						? RequestStatus.Served
						: RequestStatus.ServedWithErrors;
				return new ServiceResponse(status, this.writer.toString());

			} catch (Exception | Error e) {
				App.this.listener.listen(ctx, e);
				// reset output that might have been initiated..
				this.initOutput();
				return errorResponse(RequestStatus.ServerError);
			}

		}
		private void initOutput() {
			this.writer = new StringWriter();
			this.outData = JsonUtil.newOutputData(writer);
		}
		private IServiceResponse errorResponse(RequestStatus status) {
			outData.beginObject();

			String messageId = status.getMessageId();
			this.outData.addName("status").addValue(messageId);
			this.outData.addName("statusDescription")
					.addValue(status.getDescription());

			this.outData.addName("messages").beginArray();
			Message.newError(messageId).toOutputData(outData);
			this.outData.endArray();

			this.outData.endObject();

			return new ServiceResponse(status, this.writer.toString());
		}
	}

}