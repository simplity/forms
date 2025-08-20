package org.simplity.fm.core.json.gson;

import java.io.Reader;
import java.io.StringWriter;

import org.simplity.fm.core.IoUtil;
import org.simplity.fm.core.json.JsonAdapter;
import org.simplity.fm.core.json.JsonException;
import org.simplity.fm.core.service.InputArray;
import org.simplity.fm.core.service.InputData;
import org.simplity.fm.core.service.OutputData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * GsonAdapter is an implementation of the JsonAdapter interface that uses the
 * Gson library for JSON serialization and deserialization.
 */
public class GsonAdapter implements JsonAdapter {
	static final Logger logger = LoggerFactory.getLogger(GsonAdapter.class);

	@Override
	public InputData newInputData() {
		return new GsonInputData();
	}

	@Override
	public InputArray newInputArray() {
		return new GsonInputArray();
	}

	@Override
	public InputData newInputData(Reader reader) throws JsonException {
		try {
			return new GsonInputData(reader);
		} catch (JsonException e) {
			logger.error(e.getMessage());
			return null;
		}
	}

	@Override
	public InputArray newInputArray(Reader reader) throws JsonException {
		try {
			return new GsonInputArray(reader);
		} catch (JsonException e) {
			logger.error(e.getMessage());
			return null;
		}
	}

	@Override
	public InputData newInputDataFromResource(String fileOrResource) throws JsonException {
		JsonObject json = GsonAdapter.readJsonResource(fileOrResource);
		if (json == null) {
			return null;
		}
		return new GsonInputData(json);
	}

	@Override
	public OutputData newOutputData(StringWriter writer) {
		return new GsonOutputData(writer);
	}

	@Override
	public String qryString(InputData inData, String path) {
		JsonObject obj = ((GsonInputData) inData).getJsonObject();
		JsonElement ele = queryAsEle(obj, path);
		if (ele == null || ele.isJsonPrimitive() == false) {
			return null;
		}
		return ele.getAsString();
	}

	@Override
	public long qryInteger(InputData inData, String path) {
		JsonObject obj = ((GsonInputData) inData).getJsonObject();
		JsonElement ele = queryAsEle(obj, path);
		if (ele == null || ele.isJsonPrimitive() == false) {
			return 0;
		}
		return ele.getAsLong();
	}

	@Override
	public double qryDecimal(InputData inData, String path) {
		JsonObject obj = ((GsonInputData) inData).getJsonObject();
		JsonElement ele = queryAsEle(obj, path);
		if (ele == null || ele.isJsonPrimitive() == false) {
			return 0;
		}
		return ele.getAsDouble();
	}

	@Override
	public boolean qryBoolean(InputData inData, String path) {
		JsonObject obj = ((GsonInputData) inData).getJsonObject();
		JsonElement ele = queryAsEle(obj, path);
		if (ele == null || ele.isJsonPrimitive() == false) {
			return false;
		}
		return ele.getAsBoolean();
	}

	@Override
	public InputData qryInputData(InputData inData, String path) {
		JsonObject obj = ((GsonInputData) inData).getJsonObject();
		JsonElement ele = queryAsEle(obj, path);
		if (ele == null || ele.isJsonObject() == false) {
			return null;
		}
		return new GsonInputData(ele.getAsJsonObject());
	}

	@Override
	public InputArray getInputArray(InputData inData, String path) {
		JsonObject obj = ((GsonInputData) inData).getJsonObject();
		JsonElement ele = queryAsEle(obj, path);
		if (ele == null || ele.isJsonArray() == false) {
			return null;
		}
		return new GsonInputArray(ele.getAsJsonArray());
	}

	@Override
	public <T> T load(String resourceOrFileName, Class<T> cls) {
		try (Reader reader = IoUtil.getReader(resourceOrFileName)) {
			JsonElement ele = JsonParser.parseReader(reader);
			return new Gson().fromJson(ele, cls);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public <T> T load(InputData inputData, Class<T> cls) {
		return new Gson().fromJson(((GsonInputData) inputData).getJsonObject(), cls);
	}

	private static JsonObject readJsonResource(String fileOrResourceName) {
		try (Reader r = IoUtil.getReader(fileOrResourceName)) {
			if (r == null) {
				logger.error("Unable to get a handle to resource {}", fileOrResourceName);
				return null;
			}
			JsonElement ele = JsonParser.parseReader(r);
			if (ele.isJsonObject()) {
				return ele.getAsJsonObject();
			}
		} catch (Exception e) {//
		}
		logger.error("resource {} has an invalid json object", fileOrResourceName);
		return null;
	}

	/**
	 *
	 * @param inObj  non-null
	 * @param qryStr non-null
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
	 *
	 * @param inObj  non-null
	 * @param qryStr non-null
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
	 * @param inObj  non-null
	 * @param qryStr non-null
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
					logger.error("qryString : {} ends with a ']' with no matching ']' ", qry);
					return null;
				}
				String idxText = qry.substring(m + 1, len - 1).trim();
				try {
					idx = Integer.parseInt(idxText);
				} catch (NumberFormatException e) {
					logger.error("qryString : {} has an invalid index '{}'", qry, idxText);
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
				logger.error("qryString : {} has spaces within the member name:'{}'", qry, member);
				return null;
			}
			return new Qry(member, data, idx);
		}

		/**
		 * name after the last dot, provided query ends as indexed. null otherwise
		 */
		protected final String memberName;
		/**
		 * object or array name whose member/element is to be extracted. null if neither
		 * indexed, nor a dotted member
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
