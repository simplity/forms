// SPDX-License-Identifier: MIT
package org.simplity.fm.core.upload;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.simplity.fm.core.Conventions;
import org.simplity.fm.core.app.AppManager;
import org.simplity.fm.core.fn.FunctionDefinition;
import org.simplity.fm.core.infra.CompProvider;
import org.simplity.fm.core.service.ServiceContext;
import org.simplity.fm.core.validn.ValueList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author simplity.org
 *
 */
public class UploaderParser {
	protected static final Logger logger = LoggerFactory
			.getLogger(UploaderParser.class);

	private final CompProvider compProvider = AppManager.getApp()
			.getCompProvider();
	private final Map<String, String> params = new HashMap<>();
	private final Map<String, Map<String, String>> valueLists = new HashMap<>();
	private final Set<String> listsRequiringKey = new HashSet<>();
	private final Map<String, FunctionDefinition> functions = new HashMap<>();

	FormLoader[] inserts;

	/**
	 * parse a json of meta-data into an instance of uploader
	 *
	 * @param json
	 * @param ctx
	 * @return null in case of any error. Error messages, if any are logged and
	 *         not returned in any form to the caller
	 */
	public Uploader parse(final JsonObject json, final ServiceContext ctx) {

		JsonElement ele = json.get(Conventions.Upload.TAG_PARAMS);
		if (ele != null) {
			if (!ele.isJsonObject()) {
				missingTag(Conventions.Upload.TAG_PARAMS);
				return null;
			}

			if (!this.parseParams((JsonObject) ele)) {
				return null;
			}
		}

		ele = json.get(Conventions.Upload.TAG_LOOKUPS);
		if (ele != null) {
			if (!ele.isJsonObject()) {
				missingTag(Conventions.Upload.TAG_LOOKUPS);
				return null;
			}
			if (!this.parseLookups((JsonObject) ele, ctx)) {
				return null;
			}
		}

		ele = json.get(Conventions.Upload.TAG_FUNCTIONS);
		if (ele != null) {
			if (!ele.isJsonObject()) {
				missingTag(Conventions.Upload.TAG_FUNCTIONS);
				return null;
			}
			if (!this.parseFunctions((JsonObject) ele)) {
				return null;
			}
		}

		ele = json.get(Conventions.Upload.TAG_INSERTS);
		if (ele == null || !ele.isJsonArray()) {
			missingTag(Conventions.Upload.TAG_INSERTS);
			return null;
		}

		if (this.parseInsert((JsonArray) ele) == false) {
			return null;
		}

		return new Uploader(this.inserts);
	}

	private boolean parseParams(final JsonObject json) {
		for (final Map.Entry<String, JsonElement> entry : json.entrySet()) {
			final String name = entry.getKey();
			final JsonElement ele = entry.getValue();
			if (!ele.isJsonPrimitive()) {
				logger.error("parameter {} has an invalid value", name);
			}
			this.params.put(name, ele.getAsString());
		}
		return true;
	}

	private boolean parseFunctions(final JsonObject json) {
		for (final Map.Entry<String, JsonElement> entry : json.entrySet()) {
			final String attr = entry.getKey().trim();
			final JsonElement ele = entry.getValue();
			if (!ele.isJsonPrimitive()) {
				logger.error("function name {} should have a string value",
						attr);
				return false;
			}
			final String nam = ele.getAsString().trim();
			final FunctionDefinition fn = this.compProvider.getFunction(nam);
			if (fn == null) {
				logger.error(
						"{} not defiined as a function in this application",
						nam);
				return false;
			}
			this.functions.put(attr, fn);
		}
		return true;
	}

	private boolean parseLookups(final JsonObject json,
			final ServiceContext ctx) {
		for (final Map.Entry<String, JsonElement> entry : json.entrySet()) {
			final String attr = entry.getKey().trim();
			final JsonElement ele = entry.getValue();
			boolean ok = true;
			if (ele.isJsonPrimitive()) {
				ok = this.parseSystemList(attr, ele.getAsString().trim(), ctx);
			} else if (ele.isJsonObject()) {
				ok = this.parseLocalList(attr, (JsonObject) ele);
			} else {
				logger.error("Element {} which is a child of {} is invalid",
						attr, Conventions.Upload.TAG_LOOKUPS);
				return false;
			}
			if (!ok) {
				return false;
			}
		}
		return true;
	}

	private boolean parseLocalList(final String attr, final JsonObject json) {
		final Map<String, String> map = new HashMap<>();
		this.valueLists.put(attr, map);

		for (final Map.Entry<String, JsonElement> entry : json.entrySet()) {
			if (entry.getValue().isJsonObject()) {
				this.listsRequiringKey.add(attr);
				return parseKeyedList(map, json);
			}
			return parseLocalSimpleList(map, json);
		}
		return true;
	}

	private static boolean parseLocalSimpleList(final Map<String, String> map,
			final JsonObject json) {
		for (final Map.Entry<String, JsonElement> entry : json.entrySet()) {
			final JsonElement ele = entry.getValue();
			if (ele.isJsonPrimitive()) {
				map.put(entry.getKey(), ele.getAsString());
			} else {
				logger.error(
						"Attribute {} has an invalid value in a local value list lookup",
						entry.getKey());
				return false;
			}
		}
		return true;
	}

	private static boolean parseKeyedList(final Map<String, String> map,
			final JsonObject json) {
		for (final Map.Entry<String, JsonElement> entry : json.entrySet()) {
			final JsonElement ele = entry.getValue();
			final String key = entry.getKey();
			if (!ele.isJsonObject()) {
				logger.error(
						"key {} in the keyed list shoudl have a json object value",
						key);
				return false;
			}
			for (final Map.Entry<String, JsonElement> keyedEntry : ((JsonObject) ele)
					.entrySet()) {
				final JsonElement keyedEle = keyedEntry.getValue();
				final String text = keyedEntry.getKey();
				if (!keyedEle.isJsonPrimitive()) {
					logger.error(
							"attribute {} which is inside a list of a keyed list should have a primitive value.",
							text);
					return false;
				}
				// use parentKey + this key as index
				map.put(key + Conventions.Upload.KEY_TEXT_SEPARATOR + text,
						keyedEle.getAsString());
			}
		}
		return true;
	}

	private boolean parseSystemList(final String lukupName,
			final String sysName, final ServiceContext ctx) {
		final ValueList vl = this.compProvider.getValueList(sysName);
		if (vl == null) {
			logger.error(
					"{} is not defined as ValueList in this app. Verify your application config file.",
					sysName);
			return false;
		}
		Map<String, String> map = vl.getAllEntries(ctx);
		if (map == null) {
			logger.error(
					"SYstem list {} has no valid values. lookup on this list will always fail");
			map = new HashMap<>();
		}
		this.valueLists.put(lukupName, map);
		if (vl.isKeyBased()) {
			this.listsRequiringKey.add(lukupName);
		}
		return true;
	}

	private static void missingTag(final String tagName) {
		logger.error("Tag/attribute {} is missing or is not valid", tagName);
	}

	private boolean parseInsert(final JsonArray arr) {
		final int nbr = arr.size();
		this.inserts = new FormLoader[nbr];
		int idx = -1;
		final FormParser fp = new FormParser(this.params, this.valueLists,
				this.listsRequiringKey, this.functions);
		for (final JsonElement t : arr) {
			idx++;
			if (t == null || t.isJsonObject() == false) {
				logger.error("{} has an invalid or missing form element",
						Conventions.Upload.TAG_INSERTS);
				return false;
			}
			final FormLoader insert = fp.parse((JsonObject) t);
			if (insert == null) {
				logger.error("insert specification has errors.");
				return false;
			}
			this.inserts[idx] = insert;
		}

		return true;
	}
}
