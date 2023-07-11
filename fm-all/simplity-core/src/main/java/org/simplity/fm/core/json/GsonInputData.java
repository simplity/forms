package org.simplity.fm.core.json;

import java.io.Reader;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

import org.simplity.fm.core.ApplicationError;
import org.simplity.fm.core.service.IInputArray;
import org.simplity.fm.core.service.IInputData;
import org.simplity.fm.core.service.INullableValue;

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
	private static final int DATE_LENGTH = 10;
	private static final int TS_LENGTH1 = 19; // without milli seconds
	private static final int TS_LENGTH2 = 23; // with milli seconds

	/**
	 * utility method to convert a JsonElement to a NullableElement
	 *
	 * @param ele
	 * @return
	 */
	static INullableValue toValue(JsonElement ele) {
		if (ele == null || ele.isJsonNull()) {
			return new NullableValue();
		}

		if (ele.isJsonObject()) {
			return new NullableValue(new GsonInputData(ele.getAsJsonObject()));
		}

		if (ele.isJsonArray()) {
			return new NullableValue(new GsonInputArray(ele.getAsJsonArray()));
		}
		if (ele.isJsonPrimitive() == false) {
			/**
			 * safe code.
			 */
			throw new ApplicationError(
					"GSON element type " + ele.getClass().getName()
							+ " is not handled in this code");
		}

		JsonPrimitive p = ele.getAsJsonPrimitive();
		if (p.isBoolean()) {
			return new NullableValue(p.getAsBoolean());
		}

		String s = p.toString();
		if (p.isNumber()) {
			if (s.indexOf('.') == -1) {
				return new NullableValue(p.getAsLong());
			}
			return new NullableValue(p.getAsDouble());
		}

		if (s.indexOf('-') != -1) {
			int n = s.length();
			if (n == DATE_LENGTH) {
				try {
					return new NullableValue(LocalDate.parse(s));
				} catch (final Exception e) {
					//
				}

			} else if (n == TS_LENGTH1 || n == TS_LENGTH2) {
				try {
					return new NullableValue(Instant.parse(s));
				} catch (final Exception e) {
					//
				}

			}
		}
		return new NullableValue(p.getAsString());
	}

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
			return;
		}
		throw new JsonException(
				"The root of this json resource is not an object. It is "
						+ (ele.isJsonArray()
								? "an Array"
								: "a primitive, null or empty"));
	}

	/**
	 *
	 * @return underlying Json Array. This mutable, but as a rule it should not
	 *         be modified
	 */
	JsonObject getJsonObject() {
		return this.json;
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
	public long getInteger(final String name) {
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
	public Set<String> getMemberNames() {
		return this.json.keySet();
	}

	@Override
	public INullableValue getValue(String memberName) {
		JsonElement ele = this.json.get(memberName);
		return toValue(ele);
	}

	@Override
	public void addValue(String memberName, String value) {
		this.json.addProperty(memberName, value);

	}

	@Override
	public int size() {
		return this.json.size();
	}

	@Override
	public String toString() {
		return this.json.toString();
	}

}
