package org.simplity.fm.core.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.io.Reader;

import org.junit.jupiter.api.Test;
import org.simplity.fm.core.IoUtil;
import org.simplity.fm.core.service.InputArray;
import org.simplity.fm.core.service.InputData;
import org.slf4j.LoggerFactory;

class JsonUtilTest {
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(JsonUtilTest.class);
	private static final String RES_NAME = "qry/qry.json";
	private InputData[] tests;
	private InputData testData;

	JsonUtilTest() {
		try (Reader reader = IoUtil.getReader(RES_NAME)) {
			InputData data = JsonUtil.newInputData(reader);
			this.tests = data.getArray("tests").toDataArray();
			this.testData = data.getData("testData");
		} catch (IOException | JsonException e) {//
		}

	}

	@Test
	void testAll() {
		assertNotNull(this.tests, "json should have an array named 'tests'");
		assertNotNull(this.testData, "json should have an object named 'testData'");
		assertEquals(4, this.tests.length, "json should have 4 tests");
		this.testInvalidMemberNames();
		this.testValidMemberNames();
		this.testInvalidQueries();
		this.testValidQueries();
	}

	private void testInvalidMemberNames() {
		InputData testCase = this.tests[0];
		String testName = testCase.getString("name");
		logger.info("Running Test Case : {}", testName);
		/*
		 * when member names are valid, query does not fetch the value
		 */
		InputData data = testCase.getData("data");
		for (String memberName : data.getMemberNames()) {
			String desc = data.getString(memberName);
			String qryResult = JsonUtil.qryString(data, memberName);
			assertNull(qryResult, "\"" + memberName + "\": " + desc);
		}

	}

	private void testValidMemberNames() {
		InputData testCase = this.tests[1];
		String testName = testCase.getString("name");
		logger.info("Running Test Case : {}", testName);
		InputData data = testCase.getData("data");
		for (String memberName : data.getMemberNames()) {
			String desc = data.getString(memberName);
			assertEquals(JsonUtil.qryString(data, memberName), desc, "\"" + memberName + "\": " + desc);
		}

	}

	private void testValidQueries() {
		InputData testCase = this.tests[2];
		String testName = testCase.getString("name");
		logger.info("Running Test Case : {}", testName);
		InputArray arr = testCase.getArray("data");

		for (InputData aTest : arr.toDataArray()) {
			logger.info(aTest.toString());
			String qry = aTest.getString("qry");
			String typ = aTest.getString("type");
			this.assertMatch(qry, typ, aTest);
		}
	}

	private void testInvalidQueries() {
		InputData testCase = this.tests[3];
		String testName = testCase.getString("name");
		logger.info("Running Test Case : {}", testName);
		InputArray arr = testCase.getArray("data");
		for (InputData aTest : arr.toDataArray()) {
			String qry = aTest.getString("qry");
			String typ = aTest.getString("type");
			String desc = aTest.getString("desc");
			Object qryResult = this.qryPrimitiveByType(qry, typ);
			assertNull(qryResult, "Qry: \"" + qry + "\" reason: " + desc);
		}
	}

	@SuppressWarnings("boxing")
	private Object qryPrimitiveByType(String qry, String typ) {
		String s = JsonUtil.qryString(this.testData, qry);
		if (s == null) {
			return null;
		}
		switch (typ) {
		case "text":
			return s;

		case "integer":
			try {
				return Long.parseLong(s);
			} catch (NumberFormatException e) {
				return null;
			}

		case "decimal":
			try {
				return Double.parseDouble(s);
			} catch (NumberFormatException e) {
				return null;
			}

		case "boolean":
			if (s.equals("true")) {
				return true;
			}

			if (s.equals("false")) {
				return false;
			}
			return null;
		default:
			break;
		}
		return null;
	}

	private static final String VALUE = "value";

	@SuppressWarnings("boxing")
	private void assertMatch(String qry, String typ, InputData data) {
		/**
		 * how do we prove that a primitive member does not exist?? because,
		 * queryBoolen() will return false!! We will use queryString() and then infer...
		 */

		String s = JsonUtil.qryString(this.testData, qry);
		String msg = "Qry: '" + qry + '\'';
		if (s == null || s.equals("null")) {
			assertNotNull(null, msg);
		}
		switch (typ) {
		case "text":
			String s1 = data.getString(VALUE);
			assertEquals(s1, s, msg);
			return;

		case "integer":
			long n1 = data.getInteger(VALUE);
			try {
				long n2 = (long) Float.parseFloat(s.trim());
				assertEquals(n1, n2, msg);
				return;
			} catch (NumberFormatException e) {
				assertEquals(n1, s, msg);
				return;
			}

		case "decimal":
			double d1 = data.getDecimal(VALUE);
			try {
				double d2 = Double.parseDouble(s.trim());
				assertEquals(d1, d2, msg);
				return;
			} catch (NumberFormatException e) {
				assertEquals(d1, s, msg);
				return;
			}

		case "boolean":
			boolean b1 = data.getBoolean(VALUE);
			assertEquals(b1 + "", s, msg);
			return;
		default:
			break;
		}

	}

}
