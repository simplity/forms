// SPDX-License-Identifier: MIT
package org.simplity.fm.core.json.gson;

import java.io.Reader;

import org.simplity.fm.core.ApplicationError;
import org.simplity.fm.core.json.JsonException;
import org.simplity.fm.core.service.InputArray;
import org.simplity.fm.core.service.InputData;
import org.simplity.fm.core.service.NullableValue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * InputArray implementation using Gson. To be used inside of this package only
 *
 */
public class GsonInputArray implements InputArray {
	private final JsonArray array;

	/**
	 * create an empty input-array
	 */
	public GsonInputArray() {
		this.array = new JsonArray();
	}

	/**
	 * create an input-array from a Reader. The reader should contain a JSON array.
	 * If the root element is not an array, then an exception is thrown
	 *
	 * @param reader must not be null
	 * @throws JsonException if the root element is not an array
	 */
	public GsonInputArray(final Reader reader) throws JsonException {
		JsonElement ele = JsonParser.parseReader(reader);
		if (ele.isJsonArray()) {
			this.array = ele.getAsJsonArray();
		}
		throw new JsonException(
				"JSON root is not an array. it is " + (ele.isJsonObject() ? "an Object" : "a primitive or null"));
	}

	/**
	 *
	 * @param array must be non-null
	 */
	public GsonInputArray(final JsonArray array) {
		if (array == null) {
			throw new ApplicationError("JsonInputArray requires non-null array.");
		}
		this.array = array;
	}

	/**
	 *
	 * @return underlying Json Object. This mutable, but as a rule it should not be
	 *         modified
	 */
	JsonArray getJsonArray() {
		return this.array;
	}

	@Override
	public int length() {
		return this.array.size();
	}

	@Override
	public String[] toStringArray() {
		String[] arr = new String[this.array.size()];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = this.array.get(i).getAsString();
		}
		return arr;
	}

	@Override
	public long[] toIntegerArray() {
		long[] arr = new long[this.array.size()];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = this.array.get(i).getAsLong();
		}
		return arr;
	}

	@Override
	public boolean[] toBooleanArray() {
		boolean[] arr = new boolean[this.array.size()];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = this.array.get(i).getAsBoolean();
		}
		return arr;
	}

	@Override
	public double[] toDecimalArray() {
		double[] arr = new double[this.array.size()];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = this.array.get(i).getAsDouble();
		}
		return arr;
	}

	@Override
	public String getStringAt(int idx) {
		JsonElement ele = this.array.get(idx);
		if (ele == null || ele.isJsonNull()) {
			return null;
		}
		return ele.getAsString();
	}

	@Override
	public long getIntegerAt(int idx) {
		JsonElement ele = this.array.get(idx);
		if (ele == null || ele.isJsonNull()) {
			return 0;
		}
		return ele.getAsLong();
	}

	@Override
	public double getDecimalAt(int idx) {
		JsonElement ele = this.array.get(idx);
		if (ele == null || ele.isJsonNull()) {
			return 0;
		}
		return ele.getAsDouble();
	}

	@Override
	public boolean getBooleanAt(int idx) {
		JsonElement ele = this.array.get(idx);
		if (ele == null || ele.isJsonNull()) {
			return false;
		}
		return ele.getAsBoolean();
	}

	@Override
	public InputArray getArrayAt(int idx) {
		JsonElement ele = this.array.get(idx);
		if (ele == null || ele.isJsonArray() == false) {
			return null;
		}
		return new GsonInputArray(ele.getAsJsonArray());
	}

	@Override
	public InputData getDataAt(int idx) {
		JsonElement ele = this.array.get(idx);
		if (ele == null || ele.isJsonObject() == false) {
			return null;
		}
		return new GsonInputData(ele.getAsJsonObject());
	}

	@Override
	public InputData[] toDataArray() {
		InputData[] arr = new InputData[this.array.size()];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = this.getDataAt(i);
		}
		return arr;
	}

	@Override
	public InputArray[] toArrayArray() {
		InputArray[] arr = new InputArray[this.array.size()];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = this.getArrayAt(i);
		}
		return arr;
	}

	@Override
	public NullableValue getValueAt(int idx) {
		JsonElement ele = this.array.get(idx);
		return GsonInputData.toValue(ele);
	}

	@Override
	public String toString() {
		return this.array.toString();
	}
}
