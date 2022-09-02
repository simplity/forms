package org.simplity.fm.core.html;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


import org.junit.jupiter.api.Test;
import org.simplity.fm.core.IoUtil;
import org.simplity.fm.core.http.RestAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * 
 * @author simplity.org
 *
 */
public class PathsTest {
	private static final Logger logger = LoggerFactory.getLogger(PathsTest.class);
	private static final String RES_NAME = "html/paths.json";
	private JsonObject invalidPaths;
	private JsonObject validPathsWithErrors;
	private JsonObject pathsWithPrefixes;
	private JsonObject testPaths;
	private JsonArray testData;
	PathsTest(){
		JsonObject json = IoUtil.readJsonResource(RES_NAME);
		this.invalidPaths = json.get("invalidPaths").getAsJsonObject();
		this.validPathsWithErrors = json.get("validPathsWithErrors").getAsJsonObject();
		this.pathsWithPrefixes = json.get("pathsWithPrefixes").getAsJsonObject();
		this.testPaths = json.get("testPaths").getAsJsonObject();
		this.testData = json.get("testData").getAsJsonArray();
	}
	
	@Test
	void testAll() {
		this.shouldNotParseInvalidJsons();
		this.shouldParseWithErrors();
		this.shouldApplyPrefixes();
		this.shouldExtractFieldsAndMapService();
	}
	private void shouldNotParseInvalidJsons() {
		this.invalidPaths.entrySet().forEach(entry -> {
			logger.info("checking invalid paths: {}", entry.getKey());
			assertNull(RestAdapter.fromJson((JsonObject)entry.getValue()));
		});
	}
	

	private void shouldParseWithErrors() {
		this.validPathsWithErrors.entrySet().forEach(entry -> {
			logger.info("checking paths with error : {}", entry.getKey());
			assertNull(RestAdapter.fromJson((JsonObject)entry.getValue()));
		});
	}
	

	private void shouldApplyPrefixes() {
		RestAdapter paths = RestAdapter.fromJson(this.pathsWithPrefixes);
		String serviceName = paths.parsePath("app/module/p1/p2", "get", new JsonObject());
		assertEquals("a.b.s", serviceName);
		
		serviceName = paths.parsePath("p1/p2", "get", new JsonObject());
		assertNull(serviceName);
	}
	

	private void shouldExtractFieldsAndMapService() {
		RestAdapter paths = RestAdapter.fromJson(this.testPaths);
		this.testData.forEach(entry -> {
			JsonObject data = new JsonObject();
			JsonObject attrs = entry.getAsJsonObject();
			String path = attrs.get("path").getAsString();
			logger.info("testing path: {}", path);
			String method = attrs.get("method").getAsString();
			String service = attrs.get("service").getAsString();
			JsonObject expectedData =  attrs.get("data").getAsJsonObject();
			
			String result = paths.parsePath(path, method, data);
			if(service.isEmpty()) {
				assertNull(result);
			}else {
				assertEquals(service, result);
			}
			assertTrue(objectsAreSame(data, expectedData));
		});
	}
	
	private static boolean objectsAreSame(JsonObject j1, JsonObject j2) {
		if(j1.size() != j2.size()) {
			return false;
		}
		boolean[] ok = {true};
		j1.entrySet().forEach(entry -> {
			JsonElement ele = j2.getAsJsonPrimitive(entry.getKey());
			if(ele == null || ele.getAsString().equals(entry.getValue().getAsString()) == false) {
				ok[0] = false;
				return;
			}
		});
		return ok[0];
	}
}
