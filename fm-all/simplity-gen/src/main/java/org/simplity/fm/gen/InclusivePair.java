// SPDX-License-Identifier: MIT
package org.simplity.fm.gen;

/**
 * represents form level validation (across fields) in a form.
 *
 * @author simplity.org
 *
 */
class InclusivePair {
	private static final String C = ", ";

	String field1;
	String field2;
	String value1;
	String errorId;

	String fieldName;
	int index1;
	int index2;

	void emitJavaCode(final StringBuilder sbf) {
		sbf.append("new InclusiveValidation(").append(this.index1);
		sbf.append(C).append(this.index2);
		sbf.append(C).append(Util.quotedString(this.value1));
		sbf.append(C).append(Util.quotedString(this.fieldName));
		sbf.append(C).append(Util.quotedString(this.errorId));
		sbf.append(")");
	}
}
