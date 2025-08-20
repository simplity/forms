// SPDX-License-Identifier: MIT
package org.simplity.fm.gen;

/**
 * represents a pair of from-to fields in the form
 * 
 * @author simplity.org
 *
 */
class InterFieldValidation {
	private static final String C = ", ";

	String field1;
	String field2;
	String validationType;
	String onlyIfFieldValueEquals;
	String messageId;

	// calculated fields
	int index1;
	int index2;
	String fieldName;

	/**
	 * called from record.init();
	 * 
	 * @param record
	 */
	void init(Record record) {
		this.fieldName = this.field1;
		this.index1 = checkField(this.field1, record);
		this.index2 = checkField(this.field2, record);
	}

	private int checkField(String name, Record record) {
		Field f = record.fieldsMap.get(this.field1);
		if (f == null) {
			record.addError("Inter-field validation refers to field {} but that field is not defined", name);
			return -1;
		}
		return f.index;
	}

	void emitJavaCode(StringBuilder sbf) {
		sbf.append("new InterFieldValidation(").append(this.index1);
		sbf.append(C).append(this.index2);
		sbf.append(C).append(Util.quotedString(this.fieldName));
		sbf.append(C).append(Util.quotedString(this.messageId));
		sbf.append(C).append(Util.quotedString(this.onlyIfFieldValueEquals));
		sbf.append(C).append("InterFieldValidationType." + Util.toClassName(this.validationType));
		sbf.append(")");
	}
}
