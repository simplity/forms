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
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.simplity.fm.core.Conventions;
import org.simplity.fm.core.app.IApp;
import org.simplity.fm.core.app.RequestStatus;
import org.simplity.fm.core.json.JsonUtil;
import org.simplity.fm.core.service.IInputData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Agent is the single-point-of-contact to invoke any service on this app.
 * Services are not to be invoked directly (bypassing the Agent) in production.
 * This design provides a simple and clean separation of web and service layer.
 * No code needs to be written for a service in the web layer.
 *
 * @author simplity.org
 *
 */
class HttpAgent {
	private static final Logger logger = LoggerFactory.getLogger(HttpAgent.class);

	private final IRestAdapter restAdapter;
	private final IApp app;

	/**
	 * set the parser to process REST requests
	 *
	 * @param app
	 * @param restAdapter
	 */
	public HttpAgent(IApp app, IRestAdapter restAdapter) {
		this.restAdapter = restAdapter;
		this.app = app;
	}

	/**
	 * response for a pre-flight request
	 *
	 * @param req
	 *
	 * @param resp
	 */
	@SuppressWarnings("static-method") // we may have instance specific code
										// later..
	public void setOptions(final HttpServletRequest req, final HttpServletResponse resp) {
		for (int i = 0; i < Conventions.Http.HDR_NAMES.length; i++) {
			resp.setHeader(Conventions.Http.HDR_NAMES[i], Conventions.Http.HDR_TEXTS[i]);
		}

		/*
		 * we have no issue with CORS. We are ready to respond to any client so long as
		 * the auth is taken care of
		 */
		resp.setHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
	}

	/**
	 * serve an in-bound request. client request pay-load is of the form {service:
	 * string, session; string, data; Vo}
	 *
	 * @param req
	 * @param resp
	 * @throws IOException IO exception
	 *
	 */
	public void serve(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {

		IInputData inputData = null;
		try (Reader reader = req.getReader()) {
			inputData = JsonUtil.newInputData(reader);
		} catch (final Exception e) {
			logger.error("Invalid data received from the client {}", e.getMessage());
			resp.setStatus(Conventions.Http.STATUS_INVALID_DATA);
			return;
		}
		if (inputData == null) {
			inputData = JsonUtil.newInputData();
		}

		extractIp(inputData, req);
		readQueryString(req.getQueryString(), inputData);

		if (this.restAdapter != null) {
			String path = req.getPathInfo();
			String urlServiceName = this.restAdapter.parsePath(path, req.getMethod(), inputData);
			if (urlServiceName == null) {
				logger.info("path {} is not mapped to any service", path);
			} else {
				inputData.addValue("serviceName", urlServiceName);
			}
		}

		StringWriter sw = new StringWriter();
		RequestStatus status = this.app.serve(inputData, sw);
		resp.setStatus(toHttpStatus(status));
		try (PrintWriter writer = resp.getWriter()) {
			writer.write(sw.toString());
		}
	}

	private static void readQueryString(String qry, IInputData inData) {
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
			inData.addValue(pair[0].trim(), val);
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

	private static int toHttpStatus(RequestStatus status) {
		switch (status) {
		case CompletedWithErrors:
			/*
			 * After much debate, we decided to use 200/allOk That is because this status is
			 * at the protocol level. As far as the protocol us concerned, the communication
			 * was a success. It is a matter between the two Apps that the service execution
			 * indicated an error for the client-app
			 */
			// return Conventions.Http.STATUS_SERVICE_FAILED;
			return Conventions.Http.STATUS_ALL_OK;
		case ServiceNameRequired:
			return Conventions.Http.STATUS_INVALID_DATA;
		case InvalidDataFormat:
			return Conventions.Http.STATUS_INVALID_DATA;
		case NoSuchService:
			return Conventions.Http.STATUS_INVALID_SERVICE;
		case SessionRequired:
		case NoSuchSession:
			return Conventions.Http.STATUS_AUTH_REQUIRED;
		case ServerError:
			return Conventions.Http.STATUS_INTERNAL_ERROR;
		case Completed:
			return Conventions.Http.STATUS_ALL_OK;
		default:
			return Conventions.Http.STATUS_INTERNAL_ERROR;
		}

	}

	private static void extractIp(IInputData inputData, HttpServletRequest req) {
		String ip = "Unknown";
		for (String hdr : Conventions.Http.HDR_NAMES_FOR_IP) {
			String s = req.getHeader(hdr);
			if (s != null) {
				ip = s;
				break;
			}
		}
		inputData.addValue(Conventions.Http.CLIENT_IP_FIELD_NAME, ip);
	}
}
