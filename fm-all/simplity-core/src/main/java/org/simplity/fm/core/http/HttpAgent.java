// SPDX-License-Identifier: MIT
package org.simplity.fm.core.http;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;

import org.simplity.fm.core.Conventions;
import org.simplity.fm.core.app.App;
import org.simplity.fm.core.app.RequestStatus;
import org.simplity.fm.core.json.JsonUtil;
import org.simplity.fm.core.service.InputData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Agent is the single-point-of-contact to invoke any service on this app.
 * Services are not to be invoked directly (bypassing the Agent) in production.
 * This design provides a simple and clean separation of web and service layer.
 * No code needs to be written for a service in the web layer.
 *
 * @author simplity.org
 *
 */
public class HttpAgent {
	private static final Logger logger = LoggerFactory.getLogger(HttpAgent.class);

	private final App app;

	/**
	 * set the parser to process REST requests
	 *
	 * @param app
	 */
	public HttpAgent(App app) {
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
		setStandardHeaders(resp);
		resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
	}

	private static void setStandardHeaders(final HttpServletResponse resp) {
		for (String[] hdr : Conventions.Http.STANDARD_HEADERS) {
			resp.setHeader(hdr[0], hdr[1]);
		}
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

		InputData inputData = null;
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

		StringWriter sw = new StringWriter();
		RequestStatus status = this.app.serve(inputData, sw);
		setStandardHeaders(resp);
		resp.setStatus(toHttpStatus(status));
		try (PrintWriter writer = resp.getWriter()) {
			writer.write(sw.toString());
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

	private static void extractIp(InputData inputData, HttpServletRequest req) {
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
