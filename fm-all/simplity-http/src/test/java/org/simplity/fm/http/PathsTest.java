package org.simplity.fm.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.io.Reader;

import org.simplity.fm.core.IoUtil;
import org.simplity.fm.core.json.JsonUtil;
import org.simplity.fm.core.service.IInputData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author simplity.org
 *
 */
public class PathsTest {
	private static final Logger logger = LoggerFactory.getLogger(PathsTest.class);
	private static final String RES_NAME = "paths.json";
	private IInputData invalidPaths;
	private IInputData validPathsWithErrors;
	private IInputData pathsWithPrefixes;
	private IInputData testPaths;
	private IInputData testData;

	PathsTest() {
		try (Reader reader = IoUtil.getReader(RES_NAME)) {
			IInputData data = JsonUtil.newInputData(reader);
			this.invalidPaths = data.getData("invalidPaths");
			this.validPathsWithErrors = data.getData("validPathsWithErrors");
			this.pathsWithPrefixes = data.getData("pathsWithPrefixes");
			this.testPaths = data.getData("testPaths");
			this.testData = data.getData("testData");
		} catch (IOException e) {
			//
		}
	}

	// @Test
	void testAll() {
		this.shouldNotParseInvalidJsons();
		this.shouldParseWithErrors();
		// this.shouldApplyPrefixes();
		// this.shouldExtractFieldsAndMapService();
	}

	private void shouldNotParseInvalidJsons() {
		this.invalidPaths.getMemberNames().forEach(key -> {
			logger.info("checking invalid paths: {}", key);
			assertNull(RestAdapter.fromInputData(this.invalidPaths));
		});
	}

	private void shouldParseWithErrors() {
		this.validPathsWithErrors.getMemberNames().forEach(key -> {
			logger.info("checking paths with error : {}", key);
			assertNull(RestAdapter.fromInputData(this.invalidPaths));
		});
	}

	@SuppressWarnings("unused")
	private void shouldApplyPrefixes() {
		RestAdapter paths = RestAdapter.fromInputData(this.pathsWithPrefixes);
		String serviceName = paths.parsePath("app/module/p1/p2", "get", JsonUtil.newInputData());
		assertEquals("a.b.s", serviceName);

		serviceName = paths.parsePath("p1/p2", "get", JsonUtil.newInputData());
		assertNull(serviceName);
	}

	@SuppressWarnings("unused")
	private void shouldExtractFieldsAndMapService() {
		RestAdapter paths = RestAdapter.fromInputData(this.testPaths);
		this.testData.getMemberNames().forEach(key -> {
			IInputData data = JsonUtil.newInputData();
			IInputData attrs = this.testData.getData(key);
			String path = attrs.getString("path");
			logger.info("testing path: {}", path);
			String method = attrs.getString("method");
			String service = attrs.getString("service");
			IInputData expectedData = attrs.getData("data");

			String result = paths.parsePath(path, method, data);
			if (service.isEmpty()) {
				assertNull(result);
			} else {
				assertEquals(service, result);
			}
			assertEquals(data, expectedData);
		});
	}
}
