package org.simplity.fm.core.json;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.simplity.fm.core.ApplicationError;
import org.simplity.fm.core.service.IInputArray;
import org.simplity.fm.core.service.IInputData;
import org.simplity.fm.core.service.InputDataType;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

/**
 * this class is package-private to ensure that it is not used outside of this
 * package in Simplity IInputObject implementation using Gson
 *
 */
class GsonInputData implements IInputData {
	private final JsonObject json;

	/**
	 * create an empty input object
	 */
	GsonInputData() {
		this.json = new JsonObject();
	}

	/**
	 * crate an input object based on this json object
	 *
	 * @param json
	 *            must be non-null
	 */
	GsonInputData(final JsonObject json) {
		this.json = json;
	}

	GsonInputData(final Reader reader) throws JsonException {
		JsonElement ele = JsonParser.parseReader(reader);
		if (ele.isJsonObject()) {
			this.json = ele.getAsJsonObject();
		}
		throw new JsonException("JSON root is not an object");
	}

	@Override
	public IInputData getData(final String name) {
		final JsonElement ele = this.json.get(name);
		if (ele != null && ele.isJsonObject()) {
			return (new GsonInputData((JsonObject) ele));
		}
		return null;
	}

	@Override
	public IInputArray getArray(final String name) {
		final JsonElement ele = this.json.get(name);
		if (ele != null && ele.isJsonArray()) {
			return (new GsonInputArray((JsonArray) ele));
		}
		return null;
	}

	private JsonPrimitive getPrimitive(final String name) {
		final JsonElement ele = this.json.get(name);
		if (ele != null && ele.isJsonPrimitive()) {
			return (JsonPrimitive) ele;
		}
		return new JsonPrimitive("");
	}

	@Override
	public long getLong(final String name) {
		try {
			return this.getPrimitive(name).getAsLong();
		} catch (final NumberFormatException e) {
			return 0;
		}
	}

	@Override
	public String getString(final String name) {
		return this.getPrimitive(name).getAsString();
	}

	@Override
	public boolean getBoolean(final String name) {
		return this.getPrimitive(name).getAsBoolean();
	}

	@Override
	public double getDecimal(final String name) {
		try {
			return this.getPrimitive(name).getAsDouble();
		} catch (final NumberFormatException e) {
			return 0;
		}
	}

	@Override
	public boolean isEmpty() {
		return this.json.size() == 0;
	}

	@Override
	public Map<String, InputDataType> getMemberTypes() {

		HashMap<String, InputDataType> map = new HashMap<>(this.json.size());
		for (Map.Entry<String, JsonElement> entry : this.json.entrySet()) {
			map.put(entry.getKey(), getEleType(entry.getValue()));
		}
		return map;
	}

	@Override
	public Set<String> getMemberNames() {
		return this.json.keySet();
	}

	@Override
	public InputDataType getDataType(String memberName) {
		return getEleType(this.json.get(memberName));
	}

	private static InputDataType getEleType(JsonElement ele) {
		if (ele == null) {
			return InputDataType.NoData;
		}

		if (ele.isJsonPrimitive()) {
			return InputDataType.Value;
		}

		if (ele.isJsonObject()) {
			return InputDataType.DataStructure;
		}

		if (ele.isJsonArray()) {
			return InputDataType.Array;
		}
		if (ele.isJsonNull()) {
			return InputDataType.Null;
		}
		/**
		 * safe code.
		 */
		throw new ApplicationError("GSON element type "
				+ ele.getClass().getName() + " is no thandled in this code");
	}

	@Override
	public void addValue(String memberName, String value) {
		this.json.addProperty(memberName, value);

	}

	@Override
	public int size() {
		return this.json.size();
	}

}
