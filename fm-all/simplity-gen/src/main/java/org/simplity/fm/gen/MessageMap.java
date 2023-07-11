package org.simplity.fm.gen;

import java.util.Map;

/**
 *
 * @author simplity.org
 *
 */
public class MessageMap {
	private Map<String, String> messages;

	/**
	 *
	 * @param msgs
	 *            messages
	 */
	public void setMap(Map<String, String> msgs) {
		this.messages = msgs;
	}
	/**
	 *
	 * @param folder
	 *            folder name ending with folder character in which this file is
	 *            to be generated
	 * @return true if all OK. false otherwise
	 */
	public boolean generateTs(final String folder) {
		final StringBuilder sbf = new StringBuilder();
		sbf.append('{');
		int n = 0;
		if (this.messages != null) {
			for (final Map.Entry<String, String> entry : this.messages
					.entrySet()) {
				sbf.append("\n\t\"").append(entry.getKey()).append("\": ")
						.append(Util.quotedString(entry.getValue()))
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
