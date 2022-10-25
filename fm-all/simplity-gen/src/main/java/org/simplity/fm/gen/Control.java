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

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * represents a Control on a client page
 *
 * @author simplity.org
 *
 */
class Control implements Util.IInitializer {
	protected static final Logger logger = LoggerFactory
			.getLogger(Control.class);
	protected static final String C = ", ";

	String controlType;
	/**
	 * required if the type of control requires data
	 */
	String name;
	String label;
	String placeHolder;
	int width;
	int columnUnits;
	int idx;

	void emitTs(final StringBuilder def, final Map<String, Field> fields) {
		def.append("\n\t").append(this.name).append(":Field = {\n\t\tname:'")
				.append(this.name).append("'");

		final String b = "\n\t\t,";
		def.append(b).append("controlType: '").append(this.controlType)
				.append('\'');

		def.append(b).append("label: ");
		if (this.label == null) {
			def.append(Util.singleQuotedString(this.name));
		} else {
			def.append(Util.singleQuotedString(this.label));
		}

		if (this.placeHolder != null) {
			def.append(b).append("placeHolder: ")
					.append(Util.singleQuotedString(this.placeHolder));
		}

		if (this.width != 0) {
			def.append(b).append("width: ").append(this.width);
		}

		if (this.columnUnits != 0) {
			def.append(b).append("columnUnits: ").append(this.columnUnits);
		}

		if (fields != null && this.name != null) {
			final Field field = fields.get(this.name);
			if (field == null) {
				final String msg = "Control " + this.name
						+ "is not defined as a field. Control generated to result in compilation error";
				def.append(msg);
				logger.error(msg);
			} else {
				field.emitTs(def, b);
			}
		}

		def.append("\n\t};");
	}

	@Override
	public void initialize(String nam, int controlIdx) {
		this.name = nam;
		this.idx = controlIdx;
	}

}
