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

package org.simplity.fm.core.service;

/**
 * represents an array. In our usage, array ALWAYS contains objects as elements.
 * We DO NOT use array of primitives or array of arrays
 *
 * @author simplity.org
 *
 */
public interface IInputArray {

	/**
	 *
	 * @return size/length. could be zero
	 */
	int length();

	/**
	 * To be used to explore data with unknown/flexible schema. Useful in case
	 * nulls are allowed by design. Simplity recommends using additional data
	 * elements rather than resorting to nullable fields.
	 *
	 * @param idx
	 *            0-based index
	 * @return non-null value. null-value in case idx is out of range
	 */
	INullableValue getValueAt(int idx);
	/**
	 *
	 * @param idx
	 *            0-based index
	 * @return inputArray at the specified index. null if the index is out of
	 *         range
	 */
	IInputArray getArrayAt(int idx);

	/**
	 *
	 * @param idx
	 *            0-based index
	 * @return data at the specified index. null if the index is out of range
	 */
	IInputData getDataAt(int idx);

	/**
	 *
	 * @param idx
	 *            0-based index
	 * @return string at the specified index. null if the index is out of range
	 */
	String getStringAt(int idx);

	/**
	 *
	 * @param idx
	 *            0-based index
	 * @return value at the specified index. 0 if the index is out of range
	 */
	long getIntegerAt(int idx);

	/**
	 *
	 * @param idx
	 *            0-based index
	 * @return value at the specified index. 0 if the index is out of range
	 */
	double getDecimalAt(int idx);

	/**
	 *
	 * @param idx
	 *            0-based index
	 * @return vale at the specified index. false if the index is out of range
	 */
	boolean getBooleanAt(int idx);

	/**
	 *
	 * @return elements as array of data
	 */
	IInputData[] toDataArray();

	/**
	 *
	 * @return elements as array of arrays
	 */
	IInputArray[] toArrayArray();

	/**
	 *
	 * @return elements as array of string
	 */
	String[] toStringArray();

	/**
	 *
	 * @return elements as array of long
	 */
	long[] toIntegerArray();

	/**
	 *
	 * @return elements as array of boolean
	 */
	boolean[] toBooleanArray();

	/**
	 *
	 * @return elements as array of double
	 */
	double[] toDecimalArray();

}
