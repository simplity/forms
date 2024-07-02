package org.simplity.fm.core.db;

/**
 * immutable data structure to hold all the output fields from a stored
 * procedure
 *
 * @author simplity.org
 *
 */
public class StoredProcedureResult {
	/**
	 * if the stored procedure acts like a function by returning value null if
	 * no such value is returned, or the returned value is null
	 */
	public final Object returnedValue;
	/**
	 * relevant if the stored procedure has no associated outputData. it is the
	 * number of affected rows as reported by the first/only data manipulation
	 * SQL. -1 if this is not relevant
	 */
	public final int nbrRowsAffected;
	/**
	 * all the output data from the stored procedure. a procedure can return
	 * many result-sets or number-of-rows-affected. outputData is an array with
	 * one element for each such SQL executed in the stored procedure
	 *
	 * each element in the output data is an array of rows. If the sql executed
	 * is a select statement, then this array has all the rows returned by that
	 * sql. If the sql is non-select, then it has just one row with on element
	 * that represents the number of rows affected by this query.
	 *
	 * For example if the procedure first executes an update and then a select,
	 * then the outputData is like:
	 *
	 * [ [ [12] ], [ [first row of data], [second-row-of-data-]....] ] ]
	 */
	public final Object[][][] outputData;

	/**
	 * construct
	 *
	 * @param returnedValue
	 * @param nbrRowsAffected
	 * @param outputData
	 */
	public StoredProcedureResult(final Object returnedValue,
			final int nbrRowsAffected, final Object[][][] outputData) {
		this.nbrRowsAffected = nbrRowsAffected;
		this.outputData = outputData;
		this.returnedValue = returnedValue;
	}
}
