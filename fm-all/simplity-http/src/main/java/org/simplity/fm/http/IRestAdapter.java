package org.simplity.fm.http;

import com.google.gson.JsonObject;


/**
 * Adapter that handles RESTful HTTP clients. 
 * @author simplity.org
 *
 */

public interface IRestAdapter {
	/**
	 * parse a path string to get the service name, and data (name-value pairs)
	 * @param path path part of a URL to be parsed
	 * @param method (http) method being requested
	 * @param inputData to which the extracted data is to be added 
	 * @return service name. null if no service name is mapped to this path
	 */
	
	String parsePath(String path, String method, JsonObject inputData);

}
