// SPDX-License-Identifier: MIT
package org.simplity.fm.core.data;

/**
 * db operation
 *
 * @author simplity.org
 *
 */
public enum IoType {
	/**
	 * read/fetch/get one row for the primary key
	 */
	Get,
	/**
	 * insert/new/create one row
	 */
	Create,
	/**
	 * edit/update/save/submit one row based identified by primary key
	 */
	Update,
	/**
	 * delete/remove/archive one row based on primary key
	 */
	Delete,
	/**
	 * filter rows based on filter criterion
	 */
	Filter
}
