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
public class MessageMap {
	private static final Logger logger = LoggerFactory.getLogger(MessageMap.class);
	private Map<String, String> messages = new HashMap<>();

	public void init() {
		if (this.messages.size() == 0) {
			logger.warn("No messages defined for this app");
		}
	}

	/**
	 *
	 * @param msgs messages
	 */
	public void setMap(Map<String, String> msgs) {
		this.messages = msgs;
	}

	/**
	 *
	 * @param folder folder name ending with folder character in which this file is
	 *               to be generated
	 * @return true if all OK. false otherwise
	 */
	public boolean generateTs(final String folder) {
		final StringBuilder sbf = new StringBuilder();
		sbf.append('{');
		int n = 0;
		if (this.messages != null) {
			for (final Map.Entry<String, String> entry : this.messages.entrySet()) {
				sbf.append("\n\t\"").append(entry.getKey()).append("\": ").append(Util.quotedString(entry.getValue()))
						.append(',');
				n++;
			}
		}
		if (n != 0) {
			sbf.setLength(sbf.length() - 1);
		}
		sbf.append("\n}\n");
		Util.writeOut(folder + "allMessages.json", sbf.toString());
		return true;

	}
}
