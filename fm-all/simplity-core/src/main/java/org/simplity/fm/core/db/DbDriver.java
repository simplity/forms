package org.simplity.fm.core.db;

import java.sql.SQLException;

/**
 * APIs to be implemented by a Data-persistence infrastructure
 *
 * @author simplity.org
 *
 */
public interface DbDriver {
	/**
	 * do read-only operation on the rdbms
	 *
	 * @param reader function that reds from the db
	 * @return true if all ok, false otherwise. Exact meaning of what is allOK is
	 *         left to the individual implementation
	 * @throws SQLException
	 *
	 */
	boolean doReadonlyOperations(final DbReader reader) throws SQLException;

	/**
	 * do read-only operations using a specific schema name
	 *
	 * @param reader     function that reds from the db
	 * @param schemaName non-null schema name that is different from the default
	 *                   schema
	 * @return true if all ok, false otherwise. Exact meaning of what is allOK is
	 *         left to the individual implementation
	 * @throws SQLException
	 *
	 */
	boolean doReadonlyOperations(final String schemaName, final DbReader reader) throws SQLException;

	/**
	 * do read-write operations on the rdbms within a transaction boundary. The
	 * transaction is managed by the driver.
	 *
	 * @param updater function that reads from db and writes to it within a
	 *                transaction boundary. returns true to commit the transaction,
	 *                or false to signal a roll-back. The transaction is rolled back
	 *                on exceptions as well.
	 * @return true if all ok, false otherwise. Exact meaning of what is allOK is
	 *         left to the individual implementation
	 * @throws SQLException
	 */
	boolean doReadWriteOperations(final DbWriter updater) throws SQLException;

	/**
	 * do read-write-operations in a transaction using a specific schema name
	 *
	 * @param schemaName non-null schema name that is different from the default
	 *                   schema do read-write operations on the rdbms within a
	 *                   transaction boundary. The transaction is managed by the
	 *                   driver.
	 *
	 * @param updater    function that reads from db and writes to it within a
	 *                   transaction boundary. returns true to commit the
	 *                   transaction, or false to signal a roll-back. The
	 *                   transaction is rolled back on exceptions as well.
	 * @return true if all ok, false otherwise. Exact meaning of what is allOK is
	 *         left to the individual implementation
	 * @throws SQLException
	 *
	 */
	boolean doReadWriteOperations(final String schemaName, final DbWriter updater) throws SQLException;

	/**
	 * Meant for db operations that are to be committed/rolled-back possibly more
	 * than once. Of course, it is rolled-back if the caller throws any exception
	 *
	 * @param transacter function that accesses the db with transactions managed
	 *                   with commit/roll-back or with auto-commit mode. Driver does
	 *                   not do any transaction management.
	 * @return true if all ok, false otherwise. Exact meaning of what is allOK is
	 *         left to the individual implementation
	 * @throws SQLException if update is attempted after setting readOnly=true, or
	 *                      any other SqlException
	 *
	 */
	boolean doMultipleTransactions(final DbTransacter transacter) throws SQLException;

	/**
	 * Meant for db operations that are to be committed/rolled-back possibly more
	 * than once. Of course, it is rolled-back if the caller throws any exception
	 *
	 * @param schemaName name of the non-default schema to be used in the db
	 * @param transacter function that accesses the db with transactions managed
	 *                   with commit/roll-back or with auto-commit mode. Driver does
	 *                   not do any transaction management.
	 * @return true if all ok, false otherwise. Exact meaning of what is allOK is
	 *         left to the individual implementation
	 * @throws SQLException if update is attempted after setting readOnly=true, or
	 *                      any other SqlException
	 *
	 */
	boolean doMultipleTransactions(final String schemaName, final DbTransacter transacter) throws SQLException;

	/**
	 * an instance of the database meta data is made available for the specified
	 * schema
	 *
	 * @param schemaName
	 * @param reader
	 * @return true if all ok, false otherwise. Exact meaning of what is allOK is
	 *         left to the individual implementation
	 * @throws SQLException
	 */
	boolean doReadMetaData(final String schemaName, final DbMetaDataReader reader) throws SQLException;

	/**
	 * an instance of the database meta data is made available
	 *
	 * @param reader
	 * @return true if all ok, false otherwise. Exact meaning of what is allOK is
	 *         left to the individual implementation
	 * @throws SQLException
	 */
	boolean doReadMetaData(final DbMetaDataReader reader) throws SQLException;
}
