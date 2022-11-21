/*
 * Copyright (c) 2020 simplity.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.simplity.fm.core.json;

import java.io.Reader;

import org.simplity.fm.core.ApplicationError;
import org.simplity.fm.core.service.IInputArray;
import org.simplity.fm.core.service.IInputData;
import org.simplity.fm.core.service.INullableValue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * InputArray implementation using Gson. To be used inside of this package only
 *
 */
class GsonInputArray implements IInputArray {
	private final JsonArray array;

	/**
	 * create an empty input-array
	 */
	GsonInputArray() {
		this.array = new JsonArray();
	}

	GsonInputArray(final Reader reader) throws JsonException {
		JsonElement ele = JsonParser.parseReader(reader);
		if (ele.isJsonArray()) {
			this.array = ele.getAsJsonArray();
		}
		throw new JsonException("JSON root is not an array. it is "
				+ (ele.isJsonObject() ? "an Object" : "a primitive or null"));
	}

	/**
	 *
	 * @param array
	 *            must be non-null
	 */
	GsonInputArray(final JsonArray array) {
		if (array == null) {
			throw new ApplicationError(
					"JsonInputArray requires non-null array.");
		}
		this.array = array;
	}

	/**
	 *
	 * @return underlying Json Object. This mutable, but as a rule it should not
	 *         be modified
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
	public IInputArray getArrayAt(int idx) {
		JsonElement ele = this.array.get(idx);
		if (ele == null || ele.isJsonArray() == false) {
			return null;
		}
		return new GsonInputArray(ele.getAsJsonArray());
	}

	@Override
	public IInputData getDataAt(int idx) {
		JsonElement ele = this.array.get(idx);
		if (ele == null || ele.isJsonObject() == false) {
			return null;
		}
		return new GsonInputData(ele.getAsJsonObject());
	}

	@Override
	public IInputData[] toDataArray() {
		IInputData[] arr = new IInputData[this.array.size()];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = this.getDataAt(i);
		}
		return arr;
	}

	@Override
	public IInputArray[] toArrayArray() {
		IInputArray[] arr = new IInputArray[this.array.size()];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = this.getArrayAt(i);
		}
		return arr;
	}

	@Override
	public INullableValue getValueAt(int idx) {
		JsonElement ele = this.array.get(idx);
		return GsonInputData.toValue(ele);
	}

	@Override
	public String toString() {
		return this.array.toString();
	}
}
