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

package org.simplity.fm.example;

import org.simplity.fm.gen.Generator;

/**
 * @author simplity.org
 *
 */
public class Generate {
	private static final String PROJECT_ROOT = "c:/gitHub/forms/fm-all/simplity-example/";
	private static final String SPEC_ROOT = PROJECT_ROOT + "resources/spec/";
	private static final String JAVA_ROOT = PROJECT_ROOT + "src/main/java/";
	private static final String JAVA_PACKAGE = "org.simplity.fm.example.gen";

	/**
	 *
	 * @param args
	 */
	public static void main(final String[] args) {
		final long start = System.currentTimeMillis();
		if (args.length == 0) {
			Generator.generate(SPEC_ROOT, JAVA_ROOT, JAVA_PACKAGE);
		} else if (args.length == 3) {
			Generator.generate(args[0], args[1], args[2]);
		} else {
			System.err.print("Usage: Gen spec_root java_root java_package_name");
		}
		System.out.println("generated sources in " + (System.currentTimeMillis() - start) + "ms");
	}
}
