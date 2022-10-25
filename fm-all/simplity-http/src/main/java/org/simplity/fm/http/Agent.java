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
package org.simplity.fm.http;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLDecoder;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.simplity.fm.core.Conventions;
import org.simplity.fm.core.Message;
import org.simplity.fm.core.UserContext;
import org.simplity.fm.core.app.App;
import org.simplity.fm.core.app.IApp;
import org.simplity.fm.core.serialize.ISerializer;
import org.simplity.fm.core.serialize.gson.JsonInputObject;
import org.simplity.fm.core.serialize.gson.JsonSerializer;
import org.simplity.fm.core.service.IService;
import org.simplity.fm.core.service.IServiceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Agent is the single-point-of-contact to invoke any service on this app.
 * Services are not to be invoked directly (bypassing the Agent) in production.
 * This design provides a simple and clean separation of web and service layer.
 * No code needs to be written for a service in the web layer.
 *
 * @author simplity.org
 *
 */
public class Agent {
	private static final Logger logger = LoggerFactory.getLogger(Agent.class);
	private static IRestAdapter restAdapter = null;
	/**
	 * various headers that we respond back with
	 */
	public static final String[] HDR_NAMES = { "Access-Control-Allow-Methods", "Access-Control-Allow-Headers",
			"Access-Control-Max-Age", "Connection", "Cache-Control", "Accept" };
	/**
	 * values for the headers
	 */
	public static final String[] HDR_TEXTS = { "POST, GET, OPTIONS",
			"content-type, authorization, " + Conventions.Http.HEADER_SERVICE, "1728000", "Keep-Alive",
			"no-cache, no-store, must-revalidate", "application/json" };

	/**
	 * set the parser to process REST requests
	 * @param adapter
	 */
	public static void setRestAdapter(IRestAdapter adapter) {
		restAdapter = adapter;
	}
	/**
	 *
	 * @return an instance of the agent
	 */
	public static Agent getAgent() {
		return new Agent();
	}

	private HttpServletRequest req;
	private HttpServletResponse resp;
	private final IApp app = App.getApp();

	private String token;
	private UserContext session;
	private String userId;
	private String serviceName;
	private IService service;
	private JsonObject inputData;
	private IServiceContext ctx;
	
	/**
	 * null means no payload.
	 */
	private String responsePaylod;
	/**
	 * null means request is valid and is processed.
	 */
	private String requestError;
	/**
	 * Relevant only if requestError is null.
	 * null means service succeeded. non-null text could be service-specific
	 */
	private String serviceError;

	/**
	 * response for a pre-flight request
	 *
	 * @param req
	 *
	 * @param resp
	 */
	@SuppressWarnings("static-method") //we will have some instance specific thing..
	public void setOptions(final HttpServletRequest req, final HttpServletResponse resp) {
		for (int i = 0; i < Conventions.Http.HDR_NAMES.length; i++) {
			resp.setHeader(Conventions.Http.HDR_NAMES[i], Conventions.Http.HDR_TEXTS[i]);
		}
		/*
		 * we have no issue with CORS. We are ready to respond to any client so
		 * long as the auth is taken care of
		 */
		resp.setHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
	}

	/**
	 * serve an in-bound request.
	 *
	 * @param request
	 * @param response
	 * @throws IOException
	 *             IO exception
	 *
	 */
	public void serve(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
		this.req = request;
		this.resp = response;

		this.processInput();
		this.processRequest();
		this.respond();
	}

	private void processInput() {
		this.processPayload();
		this.readQueryString();
		
		String hdrServiceName = this.req.getHeader(Conventions.Http.HEADER_SERVICE);
		
		if (hdrServiceName == null) {
			logger.info("header directive {} not received. No explicit service name requested by the client.", Conventions.Http.HEADER_SERVICE);
		}
		
		String urlServiceName = null;
		if(restAdapter != null) {
			String path = this.req.getPathInfo();
			urlServiceName = restAdapter.parsePath(path, this.req.getMethod(), inputData);
			if (urlServiceName == null) {
				logger.info("path {} is not mapped to any service", path);
			}
		}
		
		//service name to be decided..
		if(urlServiceName == null) {
			if(hdrServiceName == null) {
				logger.error("No serviceName specified, or mapped for this request");
				return;
			}
			this.serviceName = hdrServiceName;
		}else {
			if(hdrServiceName != null && urlServiceName.equals(hdrServiceName) == false) {
				logger.info("Request specified {} as service in the header, but the path {} is mapped to service {}. mapped service is used.");
			}
			this.serviceName = urlServiceName;
		}			
		
		if (this.serviceName == null) {
			return;
		}

		logger.info("Requested service = {}", this.serviceName);
		this.token = this.req.getHeader(Conventions.Http.HEADER_AUTH);
		if (this.token == null) {
			logger.info("Request received with  no token. Assumed guest request");
			return;
		}

		this.session = this.app.getSessionCache().get(this.token);
		if (this.session == null) {
			logger.info("Token {} is not valid. possibly timed out. Treating this as a guest request", this.token);
			this.token = null;
			return;
		}

		this.userId = this.session.getUserId();
		logger.info("Request from authuenticated user {} ", this.userId);
	}

	private void processRequest() {
		if (this.serviceName == null) {
			logger.error("requested has not specified any service.");
			this.requestError = Conventions.Request.ERROR_NO_SERVICE;
			return;
		}
		if (this.inputData == null) {
			logger.info("Invalid JSON recd from client ");
			this.requestError = Conventions.Request.ERROR_INVALID_DATA;
			return;
		}

		final StringWriter writer = new StringWriter();
		final ISerializer outputObject = new JsonSerializer(writer);
		this.ctx = this.app.getContextFactory().newContext(this.session, outputObject);

		this.service = this.app.getCompProvider().getService(this.serviceName, this.ctx);
		if (this.service == null) {
			logger.error("No service. Responding with 404");
			this.requestError = Conventions.Request.ERROR_INVALID_SERVICE;
			return;
		}

		if (this.userId == null) {
			if (this.service.serveGuests() == false) {
				logger.info("No user. Service {} requires an authenticated user.");
				this.requestError = Conventions.Request.ERROR_AUTH_REQUIRED;
				return;
			}
		} else {
			if (this.app.getAccessController().okToServe(this.service, this.ctx) == false) {
				logger.error("User {} does not have the preveleges for service {}. Responding with 404", this.userId,
						this.service.getId());
				this.requestError = Conventions.Request.ERROR_INVALID_SERVICE;
				return;
			}
		}


		/*
		 * we are ready to execute this service.
		 */
		this.app.getRequestLogger().log(this.userId, this.service.getId(), this.inputData.toString());

		try {
			this.service.serve(this.ctx, new JsonInputObject(this.inputData));
			if (this.ctx.allOk()) {
				logger.info("Service returned with All Ok");
			} else {
				logger.error("Service returned with error messages");
				this.requestError = Conventions.Request.ERROR_SERVICE_FAILED;
			}
		} catch (final Throwable e) {
			logger.error("internal Error", e);
			this.app.getExceptionListener().listen(this.ctx, e);
			this.ctx.addMessage(Message.newError(Message.MSG_INTERNAL_ERROR));
			this.requestError = Conventions.Request.ERROR_SERVICE_FAILED;
		}
		
		String pl = writer.toString();
		if(pl != null && pl.isEmpty() == false) {
			this.responsePaylod = pl;
		}
	}

	private void respond() {
		
		setHeaderResponse();

		boolean addToken = false;
		if(this.serviceError == null) {
			/*
			 * are we to set a user session?
			 */
			final UserContext seshan = this.ctx.getNewUserContext();
			if (seshan != null) {
				if (this.token == null) {
					/*
					 * this is a new session. We have to create a token and send
					 * that to the client in the header as well
					 */
					this.token = UUID.randomUUID().toString();
					this.resp.setHeader(Conventions.Http.HEADER_SERVICE, this.token);
					logger.info("Auth token set to {} ", this.token);
					addToken = true;
				}
				App.getApp().getSessionCache().put(this.token, seshan);
			}
		}
		
		if(this.responsePaylod == null) {
			return;
		}
		
		try (Writer writer = this.resp.getWriter()) {
			writer.write("{\"");
			writer.write(Conventions.Request.TAG_ALL_OK);
			writer.write("\":");
			if (this.ctx.allOk()) {
				writer.write("true");
				if (addToken) {
					writer.write(",\"");
					writer.write(Conventions.Request.TAG_TOKEN);
					writer.write("\":\"");
					writer.write(this.token);
					writer.write('"');
				}
				if (this.responsePaylod != null) {
					writer.write(",\"");
					writer.append(Conventions.Request.TAG_DATA);
					writer.write("\":");
					writer.write(this.responsePaylod);
				}
			} else {
				writer.write("false");
			}
			writeMessage(writer, this.ctx.getMessages());
			writer.write("}");
		} catch (final Exception e) {
			e.printStackTrace();
			try {
				this.resp.sendError(500);
			} catch (final IOException e1) {
				//
			}
		}
	}

	private void processPayload() {
		if (this.req.getContentLength() == 0) {
			this.inputData = new JsonObject();
		} else {
			try (Reader reader = this.req.getReader()) {
				/*
				 * read it as json
				 */
				final JsonElement node = new JsonParser().parse(reader);
				if (!node.isJsonObject()) {
					return;
				}
				this.inputData = (JsonObject) node;

			} catch (final Exception e) {
				logger.error("Invalid data recd from client {}", e.getMessage());
			}
		}
	}

	private void readQueryString() {
		final String qry = this.req.getQueryString();
		if (qry == null) {
			return;
		}

		for (final String part : qry.split("&")) {
			final String[] pair = part.split("=");
			String val;
			if (pair.length == 1) {
				val = "";
			} else {
				val = decode(pair[1]);
			}
			this.inputData.addProperty(pair[0].trim(), val);
		}
	}

	private static String decode(final String text) {
		try {
			return URLDecoder.decode(text, "UTF-8");
		} catch (final UnsupportedEncodingException e) {
			/*
			 * we do know that this is supported. so, this is unreachable code.
			 */
			return text;
		}
	}

	private void setHeaderResponse() {
		int status = Conventions.Http.STATUS_ALL_OK;
		if(this.requestError != null) {
			switch(this.requestError) {
			case Conventions.Request.ERROR_SERVICE_FAILED :
				status = Conventions.Http.STATUS_SERVICE_FAILED;
				break;
			case Conventions.Request.ERROR_NO_SERVICE :
				status = Conventions.Http.STATUS_INVALID_DATA;
				break;
			case Conventions.Request.ERROR_INVALID_DATA :
				status = Conventions.Http.STATUS_INVALID_DATA;
				break;
			case Conventions.Request.ERROR_INVALID_SERVICE :
				status = Conventions.Http.STATUS_INVALID_SERVICE;
				break;
			case Conventions.Request.ERROR_AUTH_REQUIRED :
				status = Conventions.Http.STATUS_AUTH_REQUIRED;
				break;
			default :
				status = Conventions.Http.STATUS_SERVICE_FAILED;
			}
		}
		this.resp.setStatus(status);
		
	}
	/**
	 * @param writer
	 * @param messages
	 * @throws IOException
	 */
	private static void writeMessage(final Writer writer, final Message[] msgs) throws IOException {
		if (msgs == null || msgs.length == 0) {
			return;
		}
		writer.write(",\"");
		writer.write(Conventions.Request.TAG_MESSAGES);
		writer.write("\":[");
		boolean isFirst = true;
		for (final Message msg : msgs) {
			if (msg == null) {
				continue;
			}
			if (isFirst) {
				isFirst = false;
			} else {
				writer.write(",");
			}
			msg.toJson(writer);
		}
		writer.write("]");
	}

}