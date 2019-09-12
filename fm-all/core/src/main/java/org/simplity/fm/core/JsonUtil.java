/*
 * Copyright (c) 2019 simplity.org
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

package org.simplity.fm.core;

import java.io.IOException;
import java.io.Writer;

/**
 * @author simplity.org
 *
 */
public class JsonUtil {
	private static final char OPEN_ARR = '[';
	private static final char CLOS_ARR = ']';
	private static final char COMA = ',';
	private static final char Q = '"';
	private static final String QS = "\"";
	private static final String QQS = "\"\"";
	private static final String NULL = "null";
	
	/**
	 * write a 2d array of string
	 * @param writer
	 * @param arr
	 * @throws IOException 
	 */
	public static void write(Writer writer, Object[][] arr) throws IOException {
		if(arr == null) {
			writer.write("[[]]");
			return;
		}
		writer.write(OPEN_ARR);
		boolean first = true;
		for(Object[] row : arr) {
			if(first) {
				first = false;
			}else {
				writer.write(COMA);
			}
			write(writer, row);
		}
		writer.write(CLOS_ARR);
	}
	
	/**
	 * write an array of string
	 * @param writer
	 * @param arr
	 * @throws IOException 
	 */
	public static void write(Writer writer, Object[] arr) throws IOException {
		if(arr ==null) {
			writer.write("[]");
			return;
		}
		writer.write(OPEN_ARR);
		boolean first = true;
		for(Object s : arr) {
			if(first) {
				first =false;
			}else {
				writer.write(COMA);
			}
			write(writer, s);
		}
		writer.write(CLOS_ARR);
	}
	
	/**
	 * write a string
	 * @param writer
	 * @param primitive  value
	 * @throws IOException 
	 */
	public static void write(Writer writer, Object primitive) throws IOException {
		if(primitive == null) {
			writer.write(NULL);
			return;
		}
		String s = primitive.toString();
		if(primitive instanceof Number || primitive instanceof Boolean) {
			writer.write(primitive.toString());
			return;
		}
		writer.write(Q);
		writer.write(s.replaceAll(QS, QQS));
		writer.write(Q);
	}
	
	/**
	 * write a string
	 * @param sbf
	 * @param number
	 */
	public static void toJson(StringBuilder sbf, Number number) {
		if(number == null) {
			sbf.append(NULL);
			return;
		}
		sbf.append(""+number);
	}
}
