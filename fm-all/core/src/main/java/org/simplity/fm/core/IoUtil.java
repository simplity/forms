package org.simplity.fm.core;

/* Copyright (c) 2018 simplity.org
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


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

/**
* @author simplity.org
*
*/
public class IoUtil {
	private static final Logger logger = LoggerFactory.getLogger(IoUtil.class);
	
	private static final String CHAR_ENCODING = "UTF-8";

	/**
	 * read from a reader
	 *
	 * @param reader
	 * @return non-null content of reader as string. could be empty if reader is
	 *         empty
	 */
	public static String readerToText(Reader reader) {
		StringBuilder sbf = new StringBuilder();
		try (BufferedReader br = new BufferedReader(reader)) {
			String str = null;
			while ((str = br.readLine()) != null) {
				sbf.append(str).append('\n');
			}
			return sbf.toString();
		} catch (Exception e) {
			logger.error("Error while reading from reader. {}", e.getMessage());
			return "";
		}
	}

	/**
	 * read input stream into a string
	 *
	 * @param stream
	 * @return content of reader as string
	 */
	public static String streamToText(InputStream stream) {
		try {
			return readerToText(new InputStreamReader(stream, CHAR_ENCODING));
		} catch (UnsupportedEncodingException e) {
			logger.error("Error while reading from reader. {}", e.getMessage());
			return "";
		}
	}

	/**
	 * read a resource into text
	 *
	 * @param fileOrResourceName
	 * @return text content of the resource. null in case of any error
	 */
	public static String readResource(String fileOrResourceName) {
		try (InputStream stream = getStream(fileOrResourceName)) {
			if (stream != null) {
				return readerToText(new InputStreamReader(stream, CHAR_ENCODING));
			}
		} catch (Exception e) {
			logger.error("Exception while reading resource {} using. Error: {}", fileOrResourceName,
					e.getMessage());
		}
		return null;
	}

	/**
	 * creates a stream for the resource from file system or using class loader
	 *
	 * @param fileOrResourceName
	 *            should be valid file-path, like c:/a/b/c.xxx, or a resource
	 *            path like /a/b/c.xxx
	 * @return stream, or null in case of any trouble creating one
	 */
	public static InputStream getStream(String fileOrResourceName) {
		logger.info("Loading resource {}", fileOrResourceName);
		/*
		 * in production, it is a resource, and hence we try that first
		 */
		ClassLoader loader = Thread.currentThread().getClass().getClassLoader();
		if(loader == null) {
			loader = IoUtil.class.getClassLoader();
			logger.info("Loader used is {}", loader);
		}
		if(loader == null) {
			logger.error("Loader is not found!!!!");
			return null;
		}
		InputStream stream = loader.getResourceAsStream(fileOrResourceName);
		//InputStream stream = Thread.currentThread().getClass().getClassLoader().getResourceAsStream(fileOrResourceName);
		if (stream != null) {
			return stream;
		}
		File file = new File(fileOrResourceName);
		if (file.exists()) {
			try {
				return new FileInputStream(file);
			} catch (Exception e) {
				logger.error(
						"Resource {} is intepreted as a file that was located on the file system, but error while creating stream from that file. Error: {}",
						fileOrResourceName, e.getMessage());
			}
		}
		return null;
	}
	
	/**
	 * read a json resource into a JSONObject
	 * @param fileOrResourceName
	 * @return json object, or null in case of any error
	 */
	public static JsonObject readJsonResource(String fileOrResourceName) {
		InputStream stream = getStream(fileOrResourceName);
		if(stream == null) {
			logger.error("Unable to get a handle to resource {}", fileOrResourceName);
			return null;
		}
		try(JsonReader reader = new JsonReader(new InputStreamReader(stream))){
			JsonElement ele = new JsonParser().parse(reader);
			if(ele.isJsonObject()) {
				return ele.getAsJsonObject();
			}
			logger.error("resource {} has an invalid json object", fileOrResourceName);
		}catch (IOException e) {
			logger.error("I/O error while reading resource {}", fileOrResourceName);
		}
		return null;
	}
}

