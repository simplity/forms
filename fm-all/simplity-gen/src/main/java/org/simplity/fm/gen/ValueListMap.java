package org.simplity.fm.gen;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author simplity.org
 *
 */
public class ValueListMap {
	protected static final Logger logger = LoggerFactory.getLogger(ValueListMap.class);
	private Map<String, ValueList> valueLists = new HashMap<>();

	public void init() {
		if (this.valueLists.size() == 0) {
			logger.warn("No Value Lists are defined for this project");
		} else {
			Util.initializeMapEntries(this.valueLists);
		}
	}

	/**
	 *
	 * @param valueLists
	 */
	public void setMap(Map<String, ValueList> valueLists) {
		this.valueLists = valueLists;
	}

	public boolean generateJava(final String rootFolder, final String packageName) {
		final String pck = packageName + ".list";
		final String folder = rootFolder + "list/";

		/**
		 * lists are created under list sub-package
		 */
		if (this.valueLists == null || this.valueLists.size() == 0) {
			logger.warn("No value lists created for this project");
			return true;
		}
		for (final ValueList list : this.valueLists.values()) {
			list.generateJava(folder, pck);
		}
		return true;
	}
}
