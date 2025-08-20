package org.simplity.fm.core.json;

/**
 * exception to be thrown by all JSON Utilities whenever an exception is
 * encountered while parsing a JSON
 *
 */
public class JsonException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * default constructor
	 *
	 * @param errorMessage
	 */
	public JsonException(String errorMessage) {
		super(errorMessage);
	}

}
