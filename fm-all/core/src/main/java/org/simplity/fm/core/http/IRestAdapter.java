package org.simplity.fm.core.http;

import com.google.gson.JsonObject;


/**
 * utility that can map a URL path to service as well as extract data from the relevant parts of the path
 * @author simplity.org
 *
 */

public interface IUrlPathParser {
	/**
	 * parse a path string to get the service name, and data (name-value pairs)
	 * @param path path part of a URL to be parsed
	 * @param method (http) method being requested
	 * @param inputData to which the extracted data is to be added 
	 * @return service name. null if no service name is mapped to this path
	 */
	
	String parsePath(String path, String method, JsonObject inputData);

}
