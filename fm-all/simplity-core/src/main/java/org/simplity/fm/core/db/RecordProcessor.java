// SPDX-License-Identifier: MIT
package org.simplity.fm.core.db;

import java.sql.SQLException;

import org.simplity.fm.core.data.Record;

/**
 * @author simplity.org
 * @param <T>
 *            Type of record that this lambda function is designed to process
 *
 */
@FunctionalInterface
public interface RecordProcessor<T extends Record> {
	/**
	 * process a record
	 *
	 * @param record
	 *            record that is coming from the db
	 * @return true to continue with the next. false to stop retrieving records
	 * @throws SQLException
	 */
	boolean process(T record) throws SQLException;
}
