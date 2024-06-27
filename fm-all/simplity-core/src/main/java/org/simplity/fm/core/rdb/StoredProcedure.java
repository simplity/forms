package org.simplity.fm.core.rdb;

import org.simplity.fm.core.data.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class to create instances of Stored Procedure based on the JSONs
 *
 * @author org.simplity
 *
 */
public abstract class StoredProcedure {
	protected static final Logger logger = LoggerFactory
			.getLogger(StoredProcedure.class);

	protected String procedureName;

	/**
	 * null if this procedure has no input parameters
	 */
	protected Record inputData;

	/**
	 * null if this procedure has no output parameters
	 */
	protected Record outputData;

	/**
	 * null if the procedure does produce any result sets.
	 */
	protected Record[] returnedData;

}
