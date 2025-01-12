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

import java.util.ArrayList;
import java.util.List;

import org.simplity.fm.core.Conventions;
import org.simplity.fm.core.Message;
import org.simplity.fm.core.app.AppManager;
import org.simplity.fm.core.valueschema.ValueType;

/**
 * returns all the configuration settings for a given record
 * 
 * @author simplity.org
 *
 */
public class GetReportSettings extends AbstractService {
	private static final String SERVICE_NAME = Conventions.App.SERVICE_GET_REPORT_SETTINGS;
	private static final String REPORT_NAME = Conventions.Request.TAG_REPORT_NAME;
	private static final String OUTPUT_LIST = Conventions.Request.TAG_LIST;
	private static final String VARIANT_NAME = "variantName";
	private static final String SETTINGS = "settings";
	private static final ValueType[] PARAM_TYPES = { ValueType.Text };
	private static final ValueType[] OUTPUT_TYPES = { ValueType.Text, ValueType.Text };
	private static final GetReportSettings instance = new GetReportSettings();

	private static final String SQL = "select variant_name, settings from _report_settings where report_name=?";

	/**
	 *
	 * @return non-null instance
	 */
	public static IService getInstance() {
		/**
		 * our instance is immutable. Hence a single instance will do.
		 */
		return instance;
	}

	private GetReportSettings() {
		super(SERVICE_NAME);
	}

	@Override
	public void serve(final IServiceContext ctx, final IInputData payload) throws Exception {
		final String reportName = payload.getString(REPORT_NAME);
		if (reportName == null || reportName.isEmpty()) {
			reportError(ctx, Conventions.MessageId.LIST_NAME_REQUIRED);
			return;
		}

		final Object[] paramaValues = { reportName };
		final List<Object[]> rows = new ArrayList<>();
		AppManager.getApp().getDbDriver().doReadonlyOperations(handle -> {
			int n = handle.readMany(SQL, paramaValues, PARAM_TYPES, OUTPUT_TYPES, rows);
			return n > 0;
		});

		/**
		 * our output is of the form {list: [{},{}....]}
		 * 
		 * each object is of the form {variantName: "", settings {...}}
		 */
		final IOutputData writer = ctx.getOutputData();
		writer.addName(OUTPUT_LIST).beginArray();
		for (Object[] row : rows) {
			writer.beginObject();
			writer.addNameValuePair(VARIANT_NAME, row[0]);
			writer.addName(SETTINGS).addStringAsJson((String) row[1]);
			writer.endObject();
		}
		writer.endArray();

	}

	private static void reportError(final IServiceContext ctx, String msg) {
		ctx.getOutputData().addName(OUTPUT_LIST).beginArray().endArray();
		ctx.addMessage(Message.newError(msg));
	}

	@Override
	public boolean serveGuests() {
		return true;
	}
}
