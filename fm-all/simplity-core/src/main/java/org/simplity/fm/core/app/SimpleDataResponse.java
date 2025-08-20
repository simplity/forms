package org.simplity.fm.core.app;

import org.simplity.fm.core.service.ServiceResponse;

/**
 *
 * simple data structure to be used as a response to service request
 *
 */
class SimpleDataResponse implements ServiceResponse {

	SimpleDataResponse(RequestStatus status, String resp) {
		this.status = status;
		this.resp = resp;
	}
	private final RequestStatus status;
	private final String resp;
	@Override
	public RequestStatus getStatus() {
		return this.status;
	}

	@Override
	public String getResponseData() {
		return this.resp;
	}

}
