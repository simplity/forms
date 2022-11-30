package org.simplity.fm.core.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;

import org.simplity.fm.core.app.AppConfigInfo;
import org.simplity.fm.core.app.AppManager;
import org.simplity.fm.core.app.IApp;
import org.simplity.fm.core.app.RequestStatus;
import org.simplity.fm.core.json.JsonUtil;
import org.simplity.fm.core.service.IInputArray;
import org.simplity.fm.core.service.IInputData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Utility to test services based on meta-data
 *
 */
public class ServiceTester {
	private static final Logger logger = LoggerFactory
			.getLogger(ServiceTester.class);

	private static final String FOLDER = "test/";
	private static final String CONFIG_RES = "_config.json";
	private static final String NAMES_NAME = "serviceNames";
	private static final String EXTN = ".test.json";
	private static final String LOGIN = "login";

	private final IApp app;
	private final String[] serviceNames;
	private final IInputData loginData;
	private String sessionId;

	/**
	 *
	 * @param resourceRoot
	 *            where test-meta data is located
	 */
	public ServiceTester(String resourceRoot) {
		String folder = resourceRoot;
		if (resourceRoot.endsWith("/") == false) {
			folder += '/';
		}
		String configSource = folder + CONFIG_RES;
		IInputData data = JsonUtil.newInputData(configSource);
		assertNotNull(data, "Test resource " + configSource
				+ " should contain configuration details for this app");

		IInputData configData = data.getData("config");
		assertNotNull(configData,
				"Attribute config should be an object with attributes for app configuration");

		AppConfigInfo info = JsonUtil.load(configData, AppConfigInfo.class);
		logger.info("{}", configData.toString());
		logger.info("APpInfo.appName: {}", info.appName);
		this.app = AppManager.newAppInstance(info);
		assertNotNull(this.app,
				"App Config details should be good enough to start this App Instance");

		String[] names = null;
		IInputArray arr = data.getArray(NAMES_NAME);
		if (arr != null) {
			names = arr.toStringArray();
		}
		assertNotNull(names, "Attribute " + NAMES_NAME
				+ "should have an array of service names to be tested");
		this.serviceNames = names;

		this.loginData = data.getData(LOGIN);
	}

	/**
	 * test all the services as per the meta data with which this instance is
	 * initiated
	 */
	public void testAllServices() {

		this.login();

		for (String serviceName : this.serviceNames) {
			String res = FOLDER + serviceName + EXTN;

			logger.info("Testing service {}", serviceName);
			IInputData data = JsonUtil.newInputData(res);
			assertNotNull(data, "Test case for service " + serviceName
					+ " exists as " + res);

			IInputData tests = data.getData("tests");
			assertNotNull(tests,
					"Test cases exist for service with attribute 'tests'");

			for (String testName : tests.getMemberNames()) {

				IInputData test = tests.getData(testName);
				assertNotNull(test,
						"Test case exists for service " + serviceName);

				IInputData req = test.getData("request");
				if (req == null) {
					req = JsonUtil.newInputData();
				}
				if (this.sessionId != null) {
					req.addValue("sessionId", this.sessionId);
				}

				IInputData response = test.getData("response");
				assertNotNull(response,
						"Expected response exists for service " + serviceName);

				String expectedStatus = response.getString("status");
				assertNotNull(expectedStatus,
						"expected status specified in the response");

				StringWriter sw = new StringWriter();
				RequestStatus status;
				try {
					status = this.app.serve(req, sw);
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}

				String responseText = sw.toString();
				logger.info("Response : {}", responseText);
				StringReader reader = new StringReader(responseText);
				IInputData actualResponse = JsonUtil.newInputData(reader);

				assertEquals(expectedStatus, status.getMessageId(),
						"service response should match");

				// test for expected messages
				IInputData msgs = response.getData("messages");
				if (msgs != null) {
					IInputArray actualMessages = actualResponse
							.getArray("messages");
					assertNotNull(actualMessages,
							"messages should exist in the response");

					// get message types into a set
					Set<String> messageIds = new HashSet<>();
					for (IInputData aMessage : actualMessages.toDataArray()) {
						messageIds.add(aMessage.getString("id"));
					}

					// now assert message types from expected types
					for (String expectedId : msgs.getMemberNames()) {
						boolean shouldExist = msgs.getBoolean(expectedId);
						boolean exists = messageIds.contains(expectedId);
						if (shouldExist) {
							assertTrue(exists, expectedId
									+ " should exist in the response messages");
						} else {
							assertFalse(exists, expectedId
									+ " should not exist in the response messages");
						}
					}

				}

				// check for expected data
				IInputData expectedData = response.getData("data");
				if (expectedData == null) {
					continue;
				}

				IInputData actualData = response.getData("data");
				for (String attr : expectedData.getMemberNames()) {
					String expectedValue = expectedData.getString(attr);
					String actualValue = JsonUtil.qryString(actualData, attr);
					if (expectedValue == null) {
						assertNull(actualValue,
								"response should not java value at " + attr);
					} else {
						assertEquals(expectedValue, expectedValue,
								"Value of " + attr + " in response");
					}
				}
			}
		}
		this.logout();
	}

	private void logout() {
		StringWriter writer = new StringWriter();
		IInputData inData = JsonUtil.newInputData();
		inData.addValue("serviceName",
				AppManager.getApp().getLogoutServiceName());
		try {
			RequestStatus status = this.app.serve(this.loginData, writer);
			assertEquals(RequestStatus.Served, status,
					"logout service should succeed");
		} catch (IOException e) {
			logger.error("I/O Error while executing login");
		}

	}
	private void login() {
		if (this.loginData == null) {
			logger.info("No Login info provided. Tests carried with no login");
		}
		StringWriter writer = new StringWriter();

		try {
			RequestStatus status = this.app.serve(this.loginData, writer);
			assertEquals(RequestStatus.Served, status,
					"login service should succeed");
			StringReader reader = new StringReader(writer.toString());
			IInputData data = JsonUtil.newInputData(reader);
			this.sessionId = data.getString("sessionId");

			assertNotNull(this.sessionId,
					"login service should set sessionId in the response");
		} catch (IOException e) {
			logger.error("I/O Error while executing login");
		}
	}
}
