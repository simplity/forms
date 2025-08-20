// SPDX-License-Identifier: MIT
package org.simplity.fm.core.db;

import java.sql.SQLException;

/**
 * @author simplity.org
 *
 */
@FunctionalInterface
public interface RowProcessor {
	/**
	 * lambda function to process a a row of data
	 *
	 * @param row
	 *            non-null array of objects
	 * @return true to continue with the next. false to stop retrieving records
	 * @throws SQLException
	 */
	boolean process(Object[] row) throws SQLException;
}
