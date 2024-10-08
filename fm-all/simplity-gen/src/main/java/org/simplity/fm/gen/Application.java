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

package org.simplity.fm.gen;

/**
 * Design Considerations:
 * Problem on hand:
 * Generate desired Java classes and TypeScript files for the meta data specified for this app.
 *
 * Approach:
 * Meta data is in Json files. We can either read them as Json Objects, or create data structures/classes.
 * We decided to use classes with matching data-structures for ease of reading/loading.
 *
 * Source code generation is lengthy, but not complex. This is probably reflected in our design as well.
 * Lengthy methods with heavily hard-coded strings.
 *
 * Since this is a fairly focused, non-generic code, we have used package-private attributes and avoided setters/getters
 */
/**
 *
 * @author simplity.org
 *
 */
public class Application {

	/**
	 * if a text field's length is less than this, it is rendered as text-field,
	 * else as text-area
	 */
	public static int TEXT_AREA_CUTOFF = 199;

	String appName;
	int maxLengthForTextField = TEXT_AREA_CUTOFF;
	String tenantFieldName;
	String tenantNameInDb;
	DbTypes dbTypes;

	/**
	 * to be called after loading it, before using it
	 */
	public void initialize() {
		if (this.maxLengthForTextField > 0) {
			TEXT_AREA_CUTOFF = this.maxLengthForTextField;
		}
	}

}
