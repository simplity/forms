// SPDX-License-Identifier: MIT
package org.simplity.fm.core.upload;

import java.util.Map;

import org.simplity.fm.core.service.ServiceContext;

/**
 * client for an upload process
 * 
 * @author simplity.org
 *
 */
@FunctionalInterface
public interface UploadClient {

	/**
	 * called by the server to get the next row. This is an unusual method that
	 * combines two methods into one.
	 * This is a call-back method called by the uploader to get the next row to
	 * be uploaded. while doing so, it also provides the result of uploading the
	 * last row
	 * 
	 * @param ctx
	 *            the service context in which this process is running
	 * 
	 * @return field/column values for this row. The caller may add some more
	 *         variables to this collection in the upload process for this row.
	 *         It is quite safe for you to clear it and re-use it for next
	 *         call-back though.
	 * 
	 *         null to imply end of data.
	 */
	public Map<String, String> nextRow(ServiceContext ctx);
}
