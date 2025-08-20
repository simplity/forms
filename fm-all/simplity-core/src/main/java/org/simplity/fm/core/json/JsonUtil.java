package org.simplity.fm.core.json;

import java.io.Reader;
import java.io.StringWriter;

import org.simplity.fm.core.IoUtil;
import org.simplity.fm.core.json.gson.GsonAdapter;
import org.simplity.fm.core.json.gson.GsonInputArray;
import org.simplity.fm.core.json.gson.GsonInputData;
import org.simplity.fm.core.json.gson.GsonOutputData;
import org.simplity.fm.core.service.InputArray;
import org.simplity.fm.core.service.InputData;
import org.simplity.fm.core.service.OutputData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * All Json based tasks provided as utility methods. This is to ensure that we
 * have the flexibility to switch to any JSON library with ease. No other class
 * should use external JSOn libraries
 *
 *
 */
public class JsonUtil {
	static final Logger logger = LoggerFactory.getLogger(JsonUtil.class);
	private static final JsonAdapter adapter = new GsonAdapter();

	/**
	 *
	 * @return non-null empty instance
	 */
	public static InputData newInputData() {
		return new GsonInputData();
	}

	/**
	 *
	 * @return non-null empty instance
	 */
	public static InputArray newInputArray() {
		return new GsonInputArray();
	}

	/**
	 *
	 * @param reader from which the input data is to be created
	 * @return null if the input is not a valid json.
	 * @throws JsonException
	 */
	public static InputData newInputData(Reader reader) throws JsonException {
		return adapter.newInputData(reader);
	}

	/**
	 *
	 * @param reader from which the input data is to be created
	 * @return null if the input is not a valid json.
	 * @throws JsonException
	 */
	public static InputArray newInputArrayt(Reader reader) throws JsonException {
		return adapter.newInputArray(reader);
	}

	/**
	 *
	 * @param fileOrResource from which the input data is to be created
	 * @return null if the input is not a valid json.
	 * @throws JsonException
	 */
	public static InputData newInputData(String fileOrResource) throws JsonException {

		return adapter.newInputDataFromResource(fileOrResource);
	}

	/**
	 * Create an OutputData on this stream
	 *
	 * @param writer
	 * @return non-null IOutputData instance
	 */
	public static OutputData newOutputData(StringWriter writer) {
		return new GsonOutputData(writer);

	}

	/**
	 * get the String value at the specified location.
	 *
	 * @param inData from which to get the child data object
	 * @param path   conforms to query string format as follows:
	 *
	 *               Should end with the leaf-node-member name that is being
	 *               queried.
	 *
	 *               should start with a member name of the data-object. If this is
	 *               an array, it must be followed with [n] to point to the nth
	 *               element.
	 *
	 *               .memberNamr points to the member of the current child member.
	 *               Of course this is valid only if the current member is a
	 *               data-object
	 * @return null if in case of any error in pointing to the desired member, or
	 *         the pointed member is not a primitive
	 */
	public static String qryString(InputData inData, String path) {
		return adapter.qryString(inData, path);
	}

	/**
	 * get the String value at the specified location.
	 *
	 * @param inData from which to get the child data object
	 * @param path   conforms to query string format as follows:
	 *
	 *               Should end with the leaf-node-member name that is being
	 *               queried.
	 *
	 *               should start with a member name of the data-object. If this is
	 *               an array, it must be followed with [n] to point to the nth
	 *               element.
	 *
	 *               .memberNamr points to the member of the current child member.
	 *               Of course this is valid only if the current member is a
	 *               data-object
	 * @return 0 in case of any error in pointing to the desired member, or the
	 *         pointed member is not a number
	 */
	public static long qryInteger(InputData inData, String path) {
		return adapter.qryInteger(inData, path);
	}

	/**
	 * get the numeric value at the specified location.
	 *
	 * @param inData from which to get the child data object
	 * @param path   conforms to query string format as follows:
	 *
	 *               Should end with the leaf-node-member name that is being
	 *               queried.
	 *
	 *               should start with a member name of the data-object. If this is
	 *               an array, it must be followed with [n] to point to the nth
	 *               element.
	 *
	 *               .memberNamr points to the member of the current child member.
	 *               Of course this is valid only if the current member is a
	 *               data-object
	 * @return 0 in case of any error in pointing to the desired member, or the
	 *         pointed member is not a number
	 */
	public static double qryDecimal(InputData inData, String path) {
		return adapter.qryDecimal(inData, path);
	}

	/**
	 * get the boolean value at the specified location.
	 *
	 * @param inData from which to get the child data object
	 * @param path   conforms to query string format as follows:
	 *
	 *               Should end with the leaf-node-member name that is being
	 *               queried.
	 *
	 *               should start with a member name of the data-object. If this is
	 *               an array, it must be followed with [n] to point to the nth
	 *               element.
	 *
	 *               .memberNamr points to the member of the current child member.
	 *               Of course this is valid only if the current member is a
	 *               data-object
	 * @return false in case of any error in pointing to the desired member, or the
	 *         pointed member is not a boolean
	 */
	public static boolean qryBoolean(InputData inData, String path) {
		return adapter.qryBoolean(inData, path);
	}

	/**
	 * get the Data Object at the specified location.
	 *
	 * @param inData from which to get the child data object
	 * @param path   conforms to query string format as follows:
	 *
	 *               Should end with the leaf-node-member name that is being
	 *               queried.
	 *
	 *               should start with a member name of the data-object. If this is
	 *               an array, it must be followed with [n] to point to the nth
	 *               element.
	 *
	 *               .memberNamr points to the member of the current child member.
	 *               Of course this is valid only if the current member is a
	 *               data-object
	 * @return null if in case of any error in pointing to the desired member, or
	 *         the pointed member is not a data-object
	 */
	public static InputData qryInputData(InputData inData, String path) {
		return adapter.qryInputData(inData, path);
	}

	/**
	 * get the Data Array at the specified location.
	 *
	 * @param inData from which to get the child data object
	 * @param path   conforms to query string format as follows:
	 *
	 *               Should end with the leaf-node-member name that is being
	 *               queried.
	 *
	 *               should start with a member name of the data-object. If this is
	 *               an array, it must be followed with [n] to point to the nth
	 *               element.
	 *
	 *               .memberNamr points to the member of the current child member.
	 *               Of course this is valid only if the current member is a
	 *               data-object
	 * @return null if in case of any error in pointing to the desired member, or
	 *         the pointed member is not an array
	 */
	public static InputArray getInputArray(InputData inData, String path) {
		return adapter.getInputArray(inData, path);
	}

	/**
	 *
	 * @param <T>
	 *
	 * @param resourceOrFileName from which the object instance is to be loaded
	 * @param cls                class to be used to instantiate an object and load
	 *                           attributes from the json
	 * @return an instance of T with its attributes loaded from the resource. null
	 *         in case of any issue
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
	 * @param inputData from which the object instance is to be loaded
	 * @param cls       class to be used to instantiate an object and load
	 *                  attributes from the json
	 * @return an instance of T with its attributes loaded from the resource. null
	 *         in case of any issue
	 */
	public static <T> T load(InputData inputData, Class<T> cls) {
		return adapter.load(inputData, cls);
	}

}
