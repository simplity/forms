// SPDX-License-Identifier: MIT
package org.simplity.fm.core.db;

import java.sql.SQLException;

/**
 * interface for a class that wants to do db operations in batch. That is, more
 * than one transaction. In this case, the client manages transactions
 * (begin-trans, commit and roll-back)
 *
 * NOTE: This interface is created because the java.util.functions can not
 * declare exceptions. Our function needs to declare a throws clause
 *
 * @author simplity.org
 *
 */
@FunctionalInterface
public interface DbTransacter {

	/**
	 * function that manages its own transactions, like commit and roll-back. It is
	 * also possible to do the read-writes with auto-commits
	 *
	 * @param handle
	 * @return true if all ok, false otherwise. Exact meaning of what is allOK is
	 *         left to the individual implementation
	 * @throws SQLException
	 */
	boolean transact(TransactionHandle handle) throws SQLException;
}
