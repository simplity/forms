/*
 * Copyright (c) 2020 simplity.org
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
public interface IProcessSpOutput {
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
