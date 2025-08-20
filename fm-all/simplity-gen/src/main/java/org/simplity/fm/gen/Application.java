// SPDX-License-Identifier: MIT
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
