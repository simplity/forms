// SPDX-License-Identifier: MIT
package org.simplity.fm.core.service;

/**
 * represents an array. In our usage, array ALWAYS contains objects as elements.
 * We DO NOT use array of primitives or array of arrays
 *
 * @author simplity.org
 *
 */
public interface InputArray {

	/**
	 *
	 * @return size/length. could be zero
	 */
	int length();

	/**
	 * To be used to explore data with unknown/flexible schema. Useful in case nulls
	 * are allowed by design. Simplity recommends using additional data elements
	 * rather than resorting to nullable fields.
	 *
	 * @param idx 0-based index
	 * @return non-null value. null-value in case idx is out of range
	 */
	NullableValue getValueAt(int idx);

	/**
	 *
	 * @param idx 0-based index
	 * @return inputArray at the specified index. null if the index is out of range
	 */
	InputArray getArrayAt(int idx);

	/**
	 *
	 * @param idx 0-based index
	 * @return data at the specified index. null if the index is out of range
	 */
	InputData getDataAt(int idx);

	/**
	 *
	 * @param idx 0-based index
	 * @return string at the specified index. null if the index is out of range
	 */
	String getStringAt(int idx);

	/**
	 *
	 * @param idx 0-based index
	 * @return value at the specified index. 0 if the index is out of range
	 */
	long getIntegerAt(int idx);

	/**
	 *
	 * @param idx 0-based index
	 * @return value at the specified index. 0 if the index is out of range
	 */
	double getDecimalAt(int idx);

	/**
	 *
	 * @param idx 0-based index
	 * @return vale at the specified index. false if the index is out of range
	 */
	boolean getBooleanAt(int idx);

	/**
	 *
	 * @return elements as array of data
	 */
	InputData[] toDataArray();

	/**
	 *
	 * @return elements as array of arrays
	 */
	InputArray[] toArrayArray();

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
