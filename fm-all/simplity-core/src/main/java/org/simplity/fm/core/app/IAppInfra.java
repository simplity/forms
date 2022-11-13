package org.simplity.fm.core.app;

import org.simplity.fm.core.data.IDbDriver;
import org.simplity.fm.core.infra.ICompProvider;
import org.simplity.fm.core.infra.IEmailer;
import org.simplity.fm.core.infra.ITexter;

/**
 * How an app is configured to run
 *
 */
public interface IAppInfra {

	/**
	 *
	 * @return run-time name of this app. it may be different from its
	 *         design-time name based on multi-app deployment design
	 */
	String getName();

	/**
	 * safety against large db operation.
	 *
	 * @return number of max rows to be extracted in a db fetch/read operation
	 */
	int getMaxRowsToExtractFromDb();

	/**
	 * nullable db fields are generally bug-prone. We recommend that you avoid
	 * them by using empty string. However, Oracle creates bigger mess by
	 * treating empty-string as null, but not quite that way!!
	 *
	 * @return true if any null text field from db is extracted as empty string
	 */

	boolean treatNullAsEmptyString();

	/**
	 *
	 * @return non-null
	 */

	ICompProvider getCompProvider();

	/**
	 *
	 * @return non-null
	 */
	IDbDriver getDbDriver();

	/**
	 *
	 * @return non-null
	 */
	ITexter getTexter();

	/**
	 *
	 * @return non-null
	 */
	IEmailer getEmailer();
}
