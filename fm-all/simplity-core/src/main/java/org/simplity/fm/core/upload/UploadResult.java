// SPDX-License-Identifier: MIT
package org.simplity.fm.core.upload;

import java.time.Instant;

import org.simplity.fm.core.Message;

/**
 * result of an upload process
 * 
 * @author simplity.org
 *
 */
public class UploadResult {
	/**
	 * instance a which first row was started(after any set-up)
	 */
	public final Instant startedAt;
	/**
	 * instance at which last row is processed
	 */
	public final Instant doneAt;
	/**
	 * total rows processed
	 */
	public final int nbrRowsProcessed;
	/**
	 * number of rows in error.
	 */
	public final int nbrRowsInError;
	/**
	 * error messages if any. Could be empty, but not null
	 */
	public Message[] errors;

	/**
	 * 
	 * @param startedAt
	 *            instance a which first row was started(after any set-up)
	 * @param doneAt
	 *            instance at which last row is processed
	 * @param nbrRowsProcessed
	 *            total rows processed
	 * @param nbrRowsInError
	 *            number of rows in error
	 * @param errors
	 *            error messages if any. Could be empty, but not null
	 */
	public UploadResult(Instant startedAt, Instant doneAt, int nbrRowsProcessed, int nbrRowsInError,
			Message[] errors) {
		this.startedAt = startedAt;
		this.doneAt = doneAt;
		this.nbrRowsProcessed = nbrRowsProcessed;
		this.nbrRowsInError = nbrRowsInError;
	}
}
