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
	 * @param importLibrary
	 *            name of the library to import the Types from
	 * @return true if all OK. false otherwise
	 */
	public boolean generateTs(final String folder, final String importLibrary) {
		final StringBuilder sbf = new StringBuilder();
		sbf.append("import { Messages } from '").append(importLibrary)
				.append("';");
		sbf.append("\n\nexport const allMessages: Messages = {");
		if (this.messages != null) {
			for (final Map.Entry<String, String> entry : this.messages
					.entrySet()) {
				sbf.append("\n\t").append(entry.getKey()).append(": ")
						.append(Util.qoutedString(entry.getValue()))
						.append(',');
			}
		}
		sbf.append("\n}\n\n");
		Util.writeOut(folder + "allMessages.ts", sbf.toString());
		return true;

	}
}
