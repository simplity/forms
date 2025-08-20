// SPDX-License-Identifier: MIT
package org.simplity.fm.core;

/**
 * @author simplity.org
 *
 */
public enum MessageType {
	/**
	 * something positive to be conveyed
	 */
	Success,
	/**
	 * neutral information (neither positive, nor negative)
	 */
	Info,
	/**
	 * warning or calling attention
	 */
	Warning,
	/**
	 * something is not right. the service did not do the intended job
	 */
	Error
}
