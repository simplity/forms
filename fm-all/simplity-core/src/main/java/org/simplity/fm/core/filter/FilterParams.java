package org.simplity.fm.core.filter;

import org.simplity.fm.core.Conventions;
import org.simplity.fm.core.Message;
import org.simplity.fm.core.json.JsonUtil;
import org.simplity.fm.core.service.IInputData;
import org.simplity.fm.core.service.IServiceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data structure with details that are received from the client for a filter
 * operations Also, this is the data structure used for configuring a report.
 * Not surprising because because the core of reporting is filtering data from a
 * a data source
 */
public class FilterParams {
	private static final Logger logger = LoggerFactory.getLogger(FilterParams.class);

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

	/**
	 * parse filter parameters from a payload
	 * 
	 * @param input
	 * @param ctx   optional serviceCOntext. If present, an error message is added
	 *              in case of parse error;
	 * @return
	 */
	public static FilterParams parse(IInputData input, IServiceContext ctx) {
		try {
			return JsonUtil.load(input, FilterParams.class);
		} catch (Exception e) {
			logger.error(e.getMessage());
			if (ctx != null) {
				ctx.addMessage(Message.newError(Conventions.MessageId.INVALID_DATA));
			}

			return null;
		}
	}
}
