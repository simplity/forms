package org.simplity.fm.core.json;

import java.io.Reader;
import java.io.StringWriter;

import org.simplity.fm.core.IoUtil;
import org.simplity.fm.core.service.IInputArray;
import org.simplity.fm.core.service.IInputData;
import org.simplity.fm.core.service.IOutputData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * All Json based tasks provided as utility methods. This is to ensure that we
 * have the flexibility to switch to any JSON library with ease. No other class
 * should use external JSOn libraries
 *
 *
 */
public class JsonUtil {
	private static final Logger logger = LoggerFactory
			.getLogger(JsonUtil.class);
	/**
	 *
	 * @return non-null empty instance
	 */
	public static IInputData newInputObject() {
		return new GsonInputData();
	}

	/**
	 *
	 * @return non-null empty instance
	 */
	public static IInputArray newInputArray() {
		return new GsonInputArray();
	}

	/**
	 *
	 * @param reader
	 *            from which the input data is to be created
	 * @return null if the input is not a valid json.
	 */
	public static IInputData newInputObject(Reader reader) {
		try {
			return new GsonInputData(reader);
		} catch (JsonException e) {
			logger.error(e.getMessage());
			return null;
		}

	}

	/**
	 * Create an OutputData on this stream
	 *
	 * @param writer
	 * @return non-null IOutputData instance
	 */
	public static IOutputData newOutputData(StringWriter writer) {
		return new GsonOutputData(writer);

	}
	/**
	 * read a json resource into a JSONObject
	 *
	 * @param fileOrResourceName
	 * @return json object, or null in case of any error
	 */
	static JsonObject readJsonResource(String fileOrResourceName) {
		try (Reader r = IoUtil.getReader(fileOrResourceName)) {
			if (r == null) {
				logger.error("Unable to get a handle to resource {}",
						fileOrResourceName);
				return null;
			}
			JsonElement ele = JsonParser.parseReader(r);
			if (ele.isJsonObject()) {
				return ele.getAsJsonObject();
			}
		} catch (Exception e) {
		}
		logger.error("resource {} has an invalid json object",
				fileOrResourceName);
		return null;
	}

}
