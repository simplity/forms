// SPDX-License-Identifier: MIT
package org.simplity.fm.core.service;

import java.util.Map;

import org.simplity.fm.core.Conventions;
import org.simplity.fm.core.Message;
import org.simplity.fm.core.app.AppManager;
import org.simplity.fm.core.validn.ValueList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * handles request to get drop-down values for a field, typically from a client
 * "list" is the mandatory parameter for name of the list.
 *
 * @author simplity.org
 *
 */
public class ListService extends AbstractService {
	private static final ListService instance = new ListService();
	protected static final Logger logger = LoggerFactory.getLogger(ListService.class);

	private static final String INPUT_LIST = Conventions.Request.TAG_LIST;
	private static final String INPUT_KEY = Conventions.Request.TAG_KEY;
	private static final String INPUT_ALL_KEYS = Conventions.Request.TAG_ALL_KEYS;

	private static final String OUTPUT_LIST = Conventions.Request.TAG_LIST;
	private static final String OUTPUT_LISTS = Conventions.Request.TAG_LISTS;
	private static final String OUTPUT_VALUE = Conventions.Request.TAG_LIST_ENTRY_VALUE;
	private static final String OUTPUT_LABEL = Conventions.Request.TAG_LIST_ENTRY_LABEL;

	/**
	 *
	 * @return non-null instance
	 */
	public static ListService getInstance() {
		return instance;
	}

	private ListService() {
		super(Conventions.App.SERVICE_LIST);
	}

	private static void reportError(final ServiceContext ctx, String msg) {
		ctx.getOutputData().addName(OUTPUT_LIST).beginArray().endArray();
		ctx.addMessage(Message.newError(msg));
	}

	@Override
	public void serve(final ServiceContext ctx, final InputData payload) throws Exception {
		final String listName = payload.getString(INPUT_LIST);
		if (listName == null || listName.isEmpty()) {
			reportError(ctx, Conventions.MessageId.LIST_NAME_REQUIRED);
			return;
		}

		final ValueList list = AppManager.getApp().getCompProvider().getValueList(listName);
		if (list == null) {
			reportError(ctx, Conventions.MessageId.LIST_NOT_CONFIGURED);
			return;
		}

		if (list.authenticationRequired()) {
			Object uid = ctx.getUserId();
			if (uid == null || uid.toString().isEmpty()) {
				reportError(ctx, Conventions.MessageId.NOT_AUTHORIZED);
				return;
			}
		}

		String key = null;
		if (list.isKeyBased()) {
			boolean forAllKeys = payload.getBoolean(INPUT_ALL_KEYS);
			if (forAllKeys) {
				Map<String, Object[][]> allLists = list.getAllLists(ctx);
				writeOut(ctx.getOutputData(), allLists);
				return;
			}

			key = payload.getString(INPUT_KEY);
			if (key == null || key.isEmpty()) {
				reportError(ctx, Conventions.MessageId.LIST_KEY_REQUIRED);
				return;
			}
		}

		Object[][] result = list.getList(key, ctx);
		if (result == null) {
			reportError(ctx, Conventions.MessageId.INTERNAL_ERROR);
			result = new Object[0][];
		} else if (result.length == 0) {
			logger.warn("List {} has no values for key {}. sending an empty response", listName, key);
		}
		OutputData data = ctx.getOutputData();
		data.addName(OUTPUT_LIST);
		emitRows(data, result);
	}

	private static void writeOut(OutputData data, Map<String, Object[][]> allLists) {
		data.addName(OUTPUT_LISTS);
		data.beginObject();
		if (allLists != null) {
			for (Map.Entry<String, Object[][]> entry : allLists.entrySet()) {
				data.addName(entry.getKey());
				emitRows(data, entry.getValue());
			}
		}
		data.endObject();
	}

	private static void emitRows(final OutputData data, final Object[][] rows) {
		data.beginArray();
		for (final Object[] row : rows) {
			data.beginObject();

			data.addName(OUTPUT_VALUE);
			data.addPrimitive(row[0]);

			data.addName(OUTPUT_LABEL);
			data.addValue(row[1].toString());

			data.endObject();
		}
		data.endArray();
	}

	@Override
	public boolean serveGuests() {
		return true;
	}
}
