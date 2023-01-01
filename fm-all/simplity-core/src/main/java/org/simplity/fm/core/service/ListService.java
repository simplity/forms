/*
 * Copyright (c) 2019 simplity.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.simplity.fm.core.service;

import java.util.Map;

import org.simplity.fm.core.Conventions;
import org.simplity.fm.core.Message;
import org.simplity.fm.core.app.AppManager;
import org.simplity.fm.core.validn.IValueList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * handles request to get drop-down values for a field, typically from a client
 * "list" is the mandatory parameter for name of the list.
 *
 * @author simplity.org
 *
 */
public class ListService implements IService {
	private static final ListService instance = new ListService();
	protected static final Logger logger = LoggerFactory
			.getLogger(ListService.class);

	private static final String INPUT_LIST = Conventions.Request.TAG_LIST;
	private static final String INPUT_KEY = Conventions.Request.TAG_KEY;
	private static final String INPUT_ALL_KEYS = Conventions.Request.TAG_ALL_KEYS;

	private static final String OUTPUT_LIST = Conventions.Request.TAG_LIST;
	private static final String OUTPUT_LISTS = Conventions.Request.TAG_LISTS;
	private static final String OUTPUT_VALUE = Conventions.Request.TAG_LIST_ENTRY_VALUE;
	private static final String OUTPUT_TEXT = Conventions.Request.TAG_LIST_ENTRY_TEXT;

	/**
	 *
	 * @return non-null instance
	 */
	public static ListService getInstance() {
		return instance;
	}

	private ListService() {
		// privatised for a singleton pattern
	}

	@Override
	public String getId() {
		return Conventions.App.SERVICE_LIST;
	}

	private static void reportError(final IServiceContext ctx, String msg) {
		ctx.getOutputData().addName(OUTPUT_LIST).beginArray().endArray();
		ctx.addMessage(Message.newError(msg));
	}
	@Override
	public void serve(final IServiceContext ctx, final IInputData payload)
			throws Exception {
		final String listName = payload.getString(INPUT_LIST);
		if (listName == null || listName.isEmpty()) {
			reportError(ctx, "list is required for listService");
			return;
		}

		final IValueList list = AppManager.getAppInfra().getCompProvider()
				.getValueList(listName);
		if (list == null) {
			reportError(ctx, "list " + listName + " is not configured");
			return;
		}

		if (list.authenticationRequired()) {
			Object uid = ctx.getUserId();
			if (uid == null || uid.toString().isEmpty()) {
				reportError(ctx, "list " + listName + " is not configured");
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
				reportError(ctx, "list " + listName
						+ " requires value for key. But it is missing in the request");
				return;
			}
		}

		Object[][] result = list.getList(key, ctx);
		if (result == null) {
			reportError(ctx, "Error while getting values for list " + listName
					+ " for key " + key);
			result = new Object[0][];
		} else if (result.length == 0) {
			logger.warn(
					"List {} has no values for key {}. sending an empty response",
					listName, key);
		}
		IOutputData data = ctx.getOutputData();
		data.addName(OUTPUT_LIST);
		emitRows(data, result);
	}

	private static void writeOut(IOutputData data,
			Map<String, Object[][]> allLists) {
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

	private static void emitRows(final IOutputData data,
			final Object[][] rows) {
		data.beginArray();
		for (final Object[] row : rows) {
			data.beginObject();

			data.addName(OUTPUT_VALUE);
			data.addPrimitive(row[0]);

			data.addName(OUTPUT_TEXT);
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
