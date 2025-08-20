package org.simplity.fm.core.json;

import java.io.Reader;
import java.io.StringWriter;

import org.simplity.fm.core.service.InputArray;
import org.simplity.fm.core.service.InputData;
import org.simplity.fm.core.service.OutputData;

/**
 * Provides JSON-related utilities for the framework, including methods to
 * create and query input/output data.
 * <p>
 * Implementations should handle JSON parsing and mapping for service input and
 * output.
 * </p>
 *
 * @see org.simplity.fm.core.service.InputData
 * @see org.simplity.fm.core.service.InputArray
 * @see org.simplity.fm.core.service.OutputData
 */
public interface JsonAdapter {

	/**
	 * Creates a new empty {@link InputData} instance.
	 *
	 * @return a new empty input data object
	 */
	InputData newInputData();

	/**
	 * Creates a new empty {@link InputArray} instance.
	 *
	 * @return a new empty input array object
	 */
	InputArray newInputArray();

	/**
	 * Parses JSON from a {@link java.io.Reader} into an {@link InputData}
	 * instance.
	 *
	 * @param reader the reader containing JSON data
	 * @return parsed input data
	 * @throws JsonException if parsing fails
	 * @see #newInputData()
	 */
	InputData newInputData(Reader reader) throws JsonException;

	/**
	 * Parses JSON from a {@link java.io.Reader} into an {@link InputArray}
	 * instance.
	 *
	 * @param reader the reader containing JSON array data
	 * @return parsed input array
	 * @throws JsonException if parsing fails
	 * @see #newInputArray()
	 */
	InputArray newInputArray(Reader reader) throws JsonException;

	/**
	 * Loads {@link InputData} from a resource or file.
	 *
	 * @param fileOrResource the resource or file name
	 * @return loaded input data
	 * @throws JsonException if loading or parsing fails
	 */
	InputData newInputDataFromResource(String fileOrResource) throws JsonException;

	/**
	 * Creates a new {@link OutputData} instance for writing JSON output.
	 *
	 * @param writer the writer to output JSON
	 * @return output data object
	 */
	OutputData newOutputData(StringWriter writer);

	// Query helpers

	/**
	 * Queries a string value from {@link InputData} at the specified path.
	 *
	 * @param inData input data to query
	 * @param path   JSON path to query
	 * @return string value at the path, or null if not found
	 */
	String qryString(InputData inData, String path);

	/**
	 * Queries a long value from {@link InputData} at the specified path.
	 *
	 * @param inData input data to query
	 * @param path   JSON path to query
	 * @return long value at the path, or 0 if not found
	 */
	long qryInteger(InputData inData, String path);

	/**
	 * Queries a double value from {@link InputData} at the specified path.
	 *
	 * @param inData input data to query
	 * @param path   JSON path to query
	 * @return decimal value at the path, or 0.0 if not found
	 */
	double qryDecimal(InputData inData, String path);

	/**
	 * Queries a boolean value from {@link InputData} at the specified path.
	 *
	 * @param inData input data to query
	 * @param path   JSON path to query
	 * @return boolean value at the path, or false if not found
	 */
	boolean qryBoolean(InputData inData, String path);

	/**
	 * Queries a nested {@link InputData} from the specified path.
	 *
	 * @param inData input data to query
	 * @param path   JSON path to query
	 * @return nested input data, or null if not found
	 */
	InputData qryInputData(InputData inData, String path);

	/**
	 * Queries an {@link InputArray} from the specified path.
	 *
	 * @param inData input data to query
	 * @param path   JSON path to query
	 * @return input array, or null if not found
	 */
	InputArray getInputArray(InputData inData, String path);

	// Object mapping

	/**
	 * Loads an object of type {@code T} from a resource or file.
	 *
	 * @param <T>                the type of object to load
	 * @param resourceOrFileName the resource or file name
	 * @param cls                the class of the object
	 * @return loaded object
	 */
	<T> T load(String resourceOrFileName, Class<T> cls);

	/**
	 * Loads an object of type {@code T} from {@link InputData}.
	 *
	 * @param <T>       the type of object to load
	 * @param inputData input data to map
	 * @param cls       the class of the object
	 * @return loaded object
	 */
	<T> T load(InputData inputData, Class<T> cls);
}
