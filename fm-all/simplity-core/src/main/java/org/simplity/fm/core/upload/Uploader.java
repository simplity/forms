// SPDX-License-Identifier: MIT
package org.simplity.fm.core.upload;

import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;

import org.simplity.fm.core.app.AppManager;
import org.simplity.fm.core.db.TransactionHandle;
import org.simplity.fm.core.service.ServiceContext;

/**
 * @author simplity.org
 *
 */
public class Uploader {
	protected final FormLoader[] inserts;

	Uploader(final FormLoader[] inserts) {
		this.inserts = inserts;
	}

	/**
	 * @param client client for this uploader that supplies input rows
	 * @param ctx
	 * @return info about what happened
	 * @throws SQLException
	 */
	public UploadResult upload(final UploadClient client, final ServiceContext ctx) throws SQLException {
		final Worker worker = new Worker(client, ctx);
		AppManager.getApp().getDbDriver().doMultipleTransactions(handle -> {

			return worker.transact(handle);
		});
		return worker.getResult();
	}

	/**
	 * @param client client for this uploader that supplies input rows
	 * @param ctx
	 *
	 * @return info about what happened
	 */
	public UploadResult validate(final UploadClient client, final ServiceContext ctx) {
		final Worker worker = new Worker(client, ctx);
		worker.validate();
		return worker.getResult();
	}

	protected class Worker {
		private Instant startedAt;
		private final UploadClient client;
		private final ServiceContext ctx;
		private Instant doneAt;
		private int nbrRows = 0;
		private int nbrErrors = 0;

		protected Worker(final UploadClient client, final ServiceContext ctx) {
			this.client = client;
			this.ctx = ctx;
		}

		protected UploadResult getResult() {
			return new UploadResult(this.startedAt, this.doneAt, this.nbrRows, this.nbrErrors, this.ctx.getMessages());
		}

		protected boolean transact(final TransactionHandle handle) throws SQLException {
			this.startedAt = Instant.now();
			while (true) {
				final Map<String, String> input = this.client.nextRow(this.ctx);
				if (input == null) {
					this.doneAt = Instant.now();
					return true;
				}

				this.nbrRows++;
				boolean ok = true;
				for (final FormLoader loader : Uploader.this.inserts) {
					if (!loader.loadData(input, handle, this.ctx)) {
						ok = false;
						break;
					}
				}
				if (ok) {
					handle.commit();
				} else {
					handle.rollback();
					this.nbrErrors++;
				}
				return ok;
			}
		}

		protected void validate() {
			this.startedAt = Instant.now();
			while (true) {
				final Map<String, String> input = this.client.nextRow(this.ctx);
				if (input == null) {
					this.doneAt = Instant.now();
					return;
				}

				this.nbrRows++;
				boolean ok = true;
				for (final FormLoader loader : Uploader.this.inserts) {
					if (!loader.validate(input, this.ctx)) {
						ok = false;
					}
				}

				if (!ok) {
					this.nbrErrors++;
				}
			}
		}
	}
}
