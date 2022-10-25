package org.simplity.fm.core.data;

import java.sql.SQLException;

import org.simplity.fm.core.rdb.DbReader;
import org.simplity.fm.core.rdb.DbWriter;
import org.simplity.fm.core.rdb.IDbTransacter;

/**
 * APIs to be implemented by a Data-persistence infrastructure
 * @author simplity.org
 *
 */
public interface IDbDriver {
	/**
	 * do read-only operation on the rdbms
	 *
	 * @param reader
	 *            function that reds from the db
	 * @throws SQLException
	 *
	 */
	public void read(final DbReader reader) throws SQLException;

	/**
	 * do read-only operations using a specific schema name
	 *
	 * @param reader
	 *            function that reds from the db
	 * @param schemaName
	 *            non-null schema name that is different from the default schema
	 * @throws SQLException
	 *
	 */
	public void read(final String schemaName, final DbReader reader) throws SQLException; 
	/**
	 * do read-write operations on the rdbms within a transaction boundary. The
	 * transaction is managed by the driver.
	 *
	 * @param updater
	 *            function that reads from db and writes to it within a
	 *            transaction boundary. returns true to commit the transaction,
	 *            or false to signal a roll-back. The transaction is rolled back
	 *            on exceptions as well.
	 * @throws SQLException
	 */
	public void readWrite(final DbWriter updater) throws SQLException;
	/**
	 * do read-write-operations in a transaction using a specific schema name
	 *
	 * @param schemaName
	 *            non-null schema name that is different from the default schema
	 *            do read-write operations on the rdbms within a transaction
	 *            boundary. The
	 *            transaction is managed by the driver.
	 *
	 * @param updater
	 *            function that reads from db and writes to it within a
	 *            transaction boundary. returns true to commit the transaction,
	 *            or false to signal a roll-back. The transaction is rolled back
	 *            on exceptions as well.
	 * @throws SQLException
	 *
	 */
	public void readWrite(final String schemaName, final DbWriter updater) throws SQLException;
	/**
	 * Meant for db operations that are to be committed/rolled-back possibly
	 * more than once. Of course, it is rolled-back if the caller throws any
	 * exception
	 *
	 * @param transacter
	 *            function that accesses the db with transactions managed with
	 *            commit/roll-back or with auto-commit mode. Driver does not do
	 *            any transaction management.
	 * @throws SQLException
	 *             if update is attempted after setting readOnly=true, or any
	 *             other SqlException
	 *
	 */
	public void transact(final IDbTransacter transacter) throws SQLException;
	/**
	 * Meant for db operations that are to be committed/rolled-back possibly
	 * more than once. Of course, it is rolled-back if the caller throws any
	 * exception
	 *
	 * @param schemaName
	 *            name of the non-default schema to be used in the db
	 * @param transacter
	 *            function that accesses the db with transactions managed with
	 *            commit/roll-back or with auto-commit mode. Driver does not do
	 *            any transaction management.
	 * @throws SQLException
	 *             if update is attempted after setting readOnly=true, or any
	 *             other SqlException
	 *
	 */
	public void transact(final String schemaName, final IDbTransacter transacter) throws SQLException;
}
