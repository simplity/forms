// SPDX-License-Identifier: MIT
package org.simplity.fm.core.valueschema;

import java.util.regex.Pattern;

/**
 * validation parameters for a text value
 *
 * @author simplity.org
 *
 */
public class TextSchema extends ValueSchema {
	private final String regex;

	/**
	 *
	 * @param name
	 * @param messageId
	 * @param minLength
	 * @param maxLength
	 * @param regex
	 */
	public TextSchema(final String name, final String messageId,
			final int minLength, final int maxLength, final String regex) {
		this.name = name;
		this.valueType = ValueType.Text;
		this.minLength = minLength;
		this.maxLength = maxLength;
		this.messageId = messageId;
		if (regex == null || regex.isEmpty()) {
			this.regex = null;
		} else {
			this.regex = regex;
		}
	}

	@Override
	public String parse(final Object object) {
		return this.parse(object.toString());
	}

	@Override
	public String parse(final String value) {
		final int len = value.length();
		if (len < this.minLength
				|| (this.maxLength > 0 && len > this.maxLength)) {
			return null;
		}
		if (this.regex == null || Pattern.matches(this.regex, value)) {
			return value;
		}
		return null;
	}
}
