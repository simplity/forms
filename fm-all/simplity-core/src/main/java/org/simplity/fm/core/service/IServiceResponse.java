package org.simplity.fm.core.service;

import org.simplity.fm.core.app.RequestStatus;

/**
 * response from the App for a service request
 *
 */
public interface IServiceResponse {
	/**
	 *
	 * @return status of the service request. This may be used by the server to
	 *         set response code in the transport protocol, like HTTP Status
	 */
	RequestStatus getStatus();
	/**
	 * payload/response to the service request that conforms to the ResponseData
	 * schema
	 *
	 * @return non-null response text
	 */
	String getResponseData();
}
