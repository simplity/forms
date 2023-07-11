package org.simplity.fm.core.json;

import java.io.Reader;
import java.io.StringWriter;

import org.simplity.fm.core.IoUtil;
import org.simplity.fm.core.service.IInputArray;
import org.simplity.fm.core.service.IInputData;
import org.simplity.fm.core.service.IOutputData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
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
	public static IInputData newInputData() {
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
	public static IInputData newInputData(Reader reader) {
		try {
			return new GsonInputData(reader);
		} catch (JsonException e) {
			logger.error(e.getMessage());
			return null;
		}

	}

	/**
	 *
	 * @param reader
	 *            from which the input data is to be created
	 * @return null if the input is not a valid json.
	 */
	public static IInputArray newInputArrayt(Reader reader) {
		try {
			return new GsonInputArray(reader);
		} catch (JsonException e) {
			logger.error(e.getMessage());
			return null;
		}

	}

	/**
	 *
	 * @param fileOrResource
	 *            from which the input data is to be created
	 * @return null if the input is not a valid json.
	 */
	public static IInputData newInputData(String fileOrResource) {
		JsonObject json = readJsonResource(fileOrResource);
		if (json == null) {
			return null;
		}
		return new GsonInputData(json);
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
	/**
	 * get the String value at the specified location.
	 *
	 * @param inData
	 *            from which to get the child data object
	 * @param qryString
	 *            conforms to query string format as follows:
	 *
	 *            Should end with the leaf-node-member name that is being
	 *            queried.
	 *
	 *            should start with a member name of the data-object. If this is
	 *            an array, it must be followed with [n] to point to the nth
	 *            element.
	 *
	 *            .memberNamr points to the member of the current child member.
	 *            Of course this is valid only if the current member is a
	 *            data-object
	 * @return null if in case of any error in pointing to the desired member,
	 *         or the pointed member is not a primitive
	 */
	public static String qryString(IInputData inData, String qryString) {
		JsonObject obj = ((GsonInputData) inData).getJsonObject();
		JsonElement ele = queryAsEle(obj, qryString);
		if (ele == null || ele.isJsonPrimitive() == false) {
			return null;
		}
		return ele.getAsString();
	}

	/**
	 * get the String value at the specified location.
	 *
	 * @param inData
	 *            from which to get the child data object
	 * @param qryString
	 *            conforms to query string format as follows:
	 *
	 *            Should end with the leaf-node-member name that is being
	 *            queried.
	 *
	 *            should start with a member name of the data-object. If this is
	 *            an array, it must be followed with [n] to point to the nth
	 *            element.
	 *
	 *            .memberNamr points to the member of the current child member.
	 *            Of course this is valid only if the current member is a
	 *            data-object
	 * @return 0 in case of any error in pointing to the desired member, or the
	 *         pointed member is not a number
	 */
	public static long qryInteger(IInputData inData, String qryString) {
		JsonObject obj = ((GsonInputData) inData).getJsonObject();
		JsonElement ele = queryAsEle(obj, qryString);
		if (ele == null || ele.isJsonPrimitive() == false) {
			return 0;
		}
		return ele.getAsLong();
	}

	/**
	 * get the numeric value at the specified location.
	 *
	 * @param inData
	 *            from which to get the child data object
	 * @param qryString
	 *            conforms to query string format as follows:
	 *
	 *            Should end with the leaf-node-member name that is being
	 *            queried.
	 *
	 *            should start with a member name of the data-object. If this is
	 *            an array, it must be followed with [n] to point to the nth
	 *            element.
	 *
	 *            .memberNamr points to the member of the current child member.
	 *            Of course this is valid only if the current member is a
	 *            data-object
	 * @return 0 in case of any error in pointing to the desired member, or the
	 *         pointed member is not a number
	 */
	public static double qryDecimal(IInputData inData, String qryString) {
		JsonObject obj = ((GsonInputData) inData).getJsonObject();
		JsonElement ele = queryAsEle(obj, qryString);
		if (ele == null || ele.isJsonPrimitive() == false) {
			return 0;
		}
		return ele.getAsDouble();
	}

	/**
	 * get the boolean value at the specified location.
	 *
	 * @param inData
	 *            from which to get the child data object
	 * @param qryString
	 *            conforms to query string format as follows:
	 *
	 *            Should end with the leaf-node-member name that is being
	 *            queried.
	 *
	 *            should start with a member name of the data-object. If this is
	 *            an array, it must be followed with [n] to point to the nth
	 *            element.
	 *
	 *            .memberNamr points to the member of the current child member.
	 *            Of course this is valid only if the current member is a
	 *            data-object
	 * @return false in case of any error in pointing to the desired member, or
	 *         the pointed member is not a boolean
	 */
	public static boolean qryBoolean(IInputData inData, String qryString) {
		JsonObject obj = ((GsonInputData) inData).getJsonObject();
		JsonElement ele = queryAsEle(obj, qryString);
		if (ele == null || ele.isJsonPrimitive() == false) {
			return false;
		}
		return ele.getAsBoolean();
	}
	/**
	 * get the Data Object at the specified location.
	 *
	 * @param inData
	 *            from which to get the child data object
	 * @param qryString
	 *            conforms to query string format as follows:
	 *
	 *            Should end with the leaf-node-member name that is being
	 *            queried.
	 *
	 *            should start with a member name of the data-object. If this is
	 *            an array, it must be followed with [n] to point to the nth
	 *            element.
	 *
	 *            .memberNamr points to the member of the current child member.
	 *            Of course this is valid only if the current member is a
	 *            data-object
	 * @return null if in case of any error in pointing to the desired member,
	 *         or the pointed member is not a data-object
	 */
	public static IInputData qryInputData(IInputData inData, String qryString) {
		JsonObject obj = ((GsonInputData) inData).getJsonObject();
		JsonElement ele = queryAsEle(obj, qryString);
		if (ele == null || ele.isJsonObject() == false) {
			return null;
		}
		return new GsonInputData(ele.getAsJsonObject());
	}

	/**
	 * get the Data Array at the specified location.
	 *
	 * @param inData
	 *            from which to get the child data object
	 * @param qryString
	 *            conforms to query string format as follows:
	 *
	 *            Should end with the leaf-node-member name that is being
	 *            queried.
	 *
	 *            should start with a member name of the data-object. If this is
	 *            an array, it must be followed with [n] to point to the nth
	 *            element.
	 *
	 *            .memberNamr points to the member of the current child member.
	 *            Of course this is valid only if the current member is a
	 *            data-object
	 * @return null if in case of any error in pointing to the desired member,
	 *         or the pointed member is not an array
	 */
	public static IInputArray getInputArray(IInputData inData,
			String qryString) {
		JsonObject obj = ((GsonInputData) inData).getJsonObject();
		JsonElement ele = queryAsEle(obj, qryString);
		if (ele == null || ele.isJsonArray() == false) {
			return null;
		}
		return new GsonInputArray(ele.getAsJsonArray());
	}

	/**
	 *
	 * @param <T>
	 *
	 * @param resourceOrFileName
	 *            from which the object instance is to be loaded
	 * @param cls
	 *            class to be used to instantiate an object and load attributes
	 *            from the json
	 * @return an instance of T with its attributes loaded from the resource.
	 *         null in case of any issue
	 */
	public static <T> T load(String resourceOrFileName, Class<T> cls) {
		try (Reader reader = IoUtil.getReader(resourceOrFileName)) {
			JsonElement ele = JsonParser.parseReader(reader);
			return new Gson().fromJson(ele, cls);
		} catch (Exception e) {
			return null;
		}

	}

	/**
	 *
	 * @param <T>
	 *
	 * @param inputData
	 *            from which the object instance is to be loaded
	 * @param cls
	 *            class to be used to instantiate an object and load attributes
	 *            from the json
	 * @return an instance of T with its attributes loaded from the resource.
	 *         null in case of any issue
	 */
	public static <T> T load(IInputData inputData, Class<T> cls) {
		return new Gson().fromJson(((GsonInputData) inputData).getJsonObject(),
				cls);
	}
	/**
	 *
	 * @param inObj
	 *            non-null
	 * @param qryStr
	 *            non-null
	 * @return null in case of any error
	 */
	private static JsonObject queryAsObject(JsonObject inObj, String qryStr) {
		JsonElement ele = queryAsEle(inObj, qryStr);
		if (ele != null && ele.isJsonObject()) {
			return ele.getAsJsonObject();
		}
		return null;
	}

	/**
	 *
	 * @param inObj
	 *            non-null
	 * @param qryStr
	 *            non-null
	 * @return null in case of any error
	 */
	private static JsonArray queryAsArray(JsonObject inObj, String qryStr) {
		JsonElement ele = queryAsEle(inObj, qryStr);
		if (ele != null && ele.isJsonArray()) {
			return ele.getAsJsonArray();
		}
		return null;
	}
	/**
	 *
	 * @param inObj
	 *            non-null
	 * @param qryStr
	 *            non-null
	 * @return null in case of any error
	 */
	private static JsonElement queryAsEle(JsonObject inObj, String qryStr) {
		Qry qry = Qry.newQuery(qryStr);
		if (qry == null) {
			return null;
		}

		int idx = qry.idx;
		if (idx != -1) { // this is an array element
			JsonArray arr = queryAsArray(inObj, qry.dataName);
			if (arr == null) {
				return null;
			}

			return arr.get(idx);
		}

		String dataName = qry.dataName;
		JsonObject obj = inObj;
		if (dataName != null) {
			obj = queryAsObject(inObj, dataName);
			if (obj == null) {
				return null;
			}
		}
		return obj.get(qry.memberName);
	}

	/**
	 * data structure with attributes required for querying
	 *
	 */
	private static class Qry {
		/**
		 *
		 * @param qry
		 * @return null in case of any syntax or semantic error
		 */
		protected static Qry newQuery(String qryStr) {
			String qry = qryStr.trim();
			int len = qry.length();
			String member = null;
			String data = null;
			int idx = -1;

			int closeAt = qry.lastIndexOf(']');
			int dotAt = qry.lastIndexOf('.');

			if (closeAt == len - 1) { // it's an array of the form a.....c[idx]
				int m = qry.lastIndexOf('[');
				if (m == -1) {
					logger.error(
							"qryString : {} ends with a ']' with no matching ']' ",
							qry);
					return null;
				}
				String idxText = qry.substring(m + 1, len - 1).trim();
				try {
					idx = Integer.parseInt(idxText);
				} catch (NumberFormatException e) {
					logger.error("qryString : {} has an invalid index '{}'",
							qry, idxText);
					return null;
				}
				data = qry.substring(0, m).trim();
			} else if (dotAt != -1) {
				data = qry.substring(0, dotAt).trim();
				member = qry.substring(dotAt + 1).trim();
			} else {
				member = qry;
			}
			if (member != null && member.indexOf(' ') != -1) {
				logger.error(
						"qryString : {} has spaces within the member name:'{}'",
						qry, member);
				return null;
			}
			return new Qry(member, data, idx);
		}
		/**
		 * name after the last dot, provided query ends as indexed. null
		 * otherwise
		 */
		protected final String memberName;
		/**
		 * object or array name whose member/element is to be extracted. null if
		 * neither indexed, nor a dotted member
		 */
		protected final String dataName;
		/**
		 * index query ends with [n]. -1 otherwise
		 */
		protected final int idx;

		private Qry(final String member, final String next, final int idx) {
			this.memberName = member;
			this.dataName = next;
			this.idx = idx;
		}
	}
}
