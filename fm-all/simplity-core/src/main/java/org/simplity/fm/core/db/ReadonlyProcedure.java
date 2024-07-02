package org.simplity.fm.core.db;

import java.sql.SQLException;

/**
 * Represents a Stored Procedure of an RDBMS.
 *
 * 1. All the parameters for the procedure are to be Input. That is no Output or
 * In-out parameters. (this feature will be developed on a need basis)
 *
 * 2. return value, if any, can only be a simple value. complex structures like
 * arrays and tables are not handled.
 *
 * 3. procedure can output one or more result sets
 *
 *
 * @author org.simplity
 *
 */
public abstract class ReadonlyProcedure extends StoredProcedure {

	/**
	 * call the stored procedure
	 *
	 * @param handle
	 * @return result of this call process
	 * @throws SQLException
	 */
	public StoredProcedureResult readIt(IReadonlyHandle handle)
			throws SQLException {

		return super.callSp(handle);
	}

}
