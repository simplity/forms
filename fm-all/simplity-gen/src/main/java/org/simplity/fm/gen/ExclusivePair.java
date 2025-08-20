// SPDX-License-Identifier: MIT
package org.simplity.fm.gen;

/**
 * represents form level validation (across fields) in a form.
 * 
 * @author simplity.org
 *
 */
class ExclusivePair {
	private static final String C = ", ";

	String field1;
	String field2;
	int index1;
	int index2;
	String fieldName;
	boolean isRequired;
	String errorId;

	void emitJavaCode(StringBuilder sbf) {
		sbf.append("new ExclusiveValidation(").append(this.index1);
		sbf.append(C).append(this.index2);
		sbf.append(C).append(this.isRequired);
		sbf.append(C).append(Util.quotedString(this.fieldName));
		sbf.append(C).append(Util.quotedString(this.errorId));
		sbf.append(")");
	}
}
