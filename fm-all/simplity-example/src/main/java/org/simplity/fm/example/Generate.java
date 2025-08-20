// SPDX-License-Identifier: MIT
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
