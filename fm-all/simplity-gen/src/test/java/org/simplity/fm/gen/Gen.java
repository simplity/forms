// SPDX-License-Identifier: MIT
package org.simplity.fm.gen;

/**
 * @author simplity.org
 *
 */
public class Gen {
	private static final String SPEC_ROOT = "c:/repos/forms/fm-all/gen/src/test/resources/spec/";
	private static final String JAVA_ROOT = "c:/repos/forms/fm-all/gen/src/test/java/";
	private static final String JAVA_PACKAGE = "org.simplity.fm.test.gen";

	/**
	 *
	 * @param args
	 */
	public static void main(final String[] args) {

		Generator.generate(SPEC_ROOT, JAVA_ROOT, JAVA_PACKAGE);
	}
}
