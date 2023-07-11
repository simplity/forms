package org.simplity.fm.core.app;

/**
 * status when the server responds to a service request
 *
 */
public enum RequestStatus {
	/**
	 * Request has not specified a service name
	 */
	ServiceNameRequired {
		@Override
		public String getDescription() {
			return "Request does not specify a service name.";
		}
	},
	/**
	 * this server does not serve this service. It may be because the service
	 * name is invalid, or it is not accessible to this requester
	 */
	NoSuchService {
		@Override
		public String getDescription() {
			return "No such service is served by this app, or the service is not accessible to this user.";
		}
	},
	/**
	 * service was requested with no session, but the server requires a session
	 * to look at any service request.
	 */
	SessionRequired {
		@Override
		public String getDescription() {
			return "Request does not specify a session id";
		}
	},

	/**
	 * the sessionId with which the service was requested is not valid. Either
	 * the session has expired, or the session id is invalid
	 */
	NoSuchSession {
		@Override
		public String getDescription() {
			return "No session is active with this session id. Either the session has expired, or the id is invalid";
		}
	},
	/**
	 * Input data stream did not yield a valid data object. like invalid
	 * Json/XML
	 */
	InvalidDataFormat {
		@Override
		public String getDescription() {
			return "Invalid input data format.";
		}
	},
	/**
	 * Request is serviced successfully
	 */
	Completed {
		@Override
		public String getDescription() {
			return "Successfully completed.";
		}
	},
	/**
	 * Service execution found errors with input data, or the intended action
	 * could not be taken for other reasons payload
	 */
	CompletedWithErrors {
		@Override
		public String getDescription() {
			return "Service completed but with errors. Either the input data was invalid, or the intended action could not be taken for other reasons.";
		}
	},
	/**
	 * Internal Error. Service was unable to run properly. OutputData contains
	 * an error message to this effect
	 */
	ServerError {
		@Override
		public String getDescription() {
			return "There was an internal error on the server. It is being looked into. ";
		}
	};

	/**
	 *
	 * @return Description of this status in English
	 */
	public abstract String getDescription();

	/**
	 *
	 * @return messageId associated with this status
	 */
	public String getMessageId() {
		String s = this.name();
		return (s.substring(0, 1)).toLowerCase() + s.substring(1);
	}
}
