package org.simplity.fm.core.filter;

/**
 * Data structure with details that are received from the client for a filter
 * operations Also, this is the data structure used for configuring a report.
 * Not surprising because because the core of reporting is filtering data from a
 * a data source
 */
public class FilterParams {
	/**
	 * optional. maximum number of rows to be filtered.
	 */
	public int maxRows;
	/**
	 * optional. default is to get all the fields
	 */
	public String[] fields;
	/**
	 * Generally, should have at least one condition. However, if this is empty or
	 * null, then all the rows will be retrieved
	 */
	public FilterCondition[] filters;
	/**
	 * optional. How the rows are to be sorted
	 */
	public SortBy[] sorts;
}
