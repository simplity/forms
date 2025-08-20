// SPDX-License-Identifier: MIT
package org.simplity.fm.core;

import org.simplity.fm.core.valueschema.ValueSchema;

/**
 * @author simplity.org
 *
 */
public interface ValueSchemas {
	/**
	 * 
	 * @param name
	 * @return data type instance, or null if there is no such data type
	 */
	public abstract ValueSchema getValueSchema(String name);

}
