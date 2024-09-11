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

	/**
	 * @param sbf
	 */
	public void emitTs(StringBuilder sbf) {
		sbf.append("{type: '").append(this.validationType).append("', field1: '").append(this.field1)
				.append("', field2: '").append(this.field2);
		sbf.append("', messageId: '").append(this.messageId).append("', f1: '").append(this.field1);
		sbf.append("', f2: '").append(this.field2).append("'");
		if (this.onlyIfFieldValueEquals != null) {
			sbf.append(", onlyIfFieldValueEquals: ").append(Util.quotedString(this.onlyIfFieldValueEquals));
		}
		sbf.append("}");
	}
}
