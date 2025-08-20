// SPDX-License-Identifier: MIT
package org.simplity.fm.core.db;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author simplity.org
 *
 *         call-back object that receives the output from a stored procedure
 */
@FunctionalInterface
public interface SpOutputProcessor {
	/**
	 * Stored procedures can output several results. Each result is either a
	 * ResultSet, or updateCOunt (the result of a update/insert/delete
	 * statement) This function/method is called for each of them, and then just
	 * once more to mark the end of output. The last call will have resultSet as
	 * null and updateCOunt as -1
	 * 
	 *
	 * @param rs
	 *            null if there are no more output, or if this output has no
	 *            resultSte, but an update count
	 * @param updateCount
	 *            -1 if no more outputs, or if this output has a result-set
	 * @return true to continue to get the next output, false to stop the
	 *         process of looking for more output.
	 * @throws SQLException
	 */
	public boolean nextResult(ResultSet rs, int updateCount)
			throws SQLException;
}
