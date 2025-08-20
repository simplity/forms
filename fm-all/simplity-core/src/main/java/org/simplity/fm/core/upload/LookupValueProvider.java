// SPDX-License-Identifier: MIT
package org.simplity.fm.core.upload;

import java.util.Map;

import org.simplity.fm.core.Conventions;
import org.simplity.fm.core.service.ServiceContext;

/**
 * specifies how a field in the form maps to columns in the input row
 * @author simplity.org
 *
 */
public class LookupValueProvider implements InputValueProvider{
	private final Map<String, String> lookup;
	private final InputValueProvider textValue;
	private final InputValueProvider keyValue;

	/**
	 * 
	 * @param lookup used for looking up internal value for the input text
	 * @param textValue non-null value provider for text to look up.
	 * @param keyValue must be null if this is simple lookup, and non-null if this is keyed lookup
	 */
	public LookupValueProvider(Map<String, String> lookup, InputValueProvider textValue, InputValueProvider keyValue ) {
		this.lookup = lookup;
		this.textValue = textValue;
		this.keyValue = keyValue;
	}
	
	@Override
	public String getValue(Map<String, String> input, ServiceContext ctx) {
		String text = this.textValue.getValue(input, ctx);
		if(text == null) {
			return null;
		}
		
		if(this.keyValue != null) {
			String key = this.keyValue.getValue(input, ctx);
			if(key == null) {
				return null;
			}
			text = key +Conventions.Upload.KEY_TEXT_SEPARATOR + text;
		}
		return this.lookup.get(text);
	}
}
