package org.simplity.fm.gen;

import java.util.HashMap;
import java.util.Map;

import org.simplity.fm.core.Conventions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author simplity.org
 *
 */
public class MessageMap {
	private static final Logger logger = LoggerFactory.getLogger(MessageMap.class);
	private static final String P = "\n\tpublic static final String ";
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

	/**
	 * generate static Constants for messageIds on the server
	 * 
	 * @param javaOutputRoot
	 * @param packageName
	 * @return true if all ok false otherwise
	 */
	public boolean generateJava(String javaOutputRoot, String packageName) {
		/*
		 * create ValueSchemas.java in the root folder.
		 */
		final StringBuilder sbf = new StringBuilder();
		sbf.append("package ").append(packageName).append(';');
		sbf.append("\n\n");

		final String clsName = Conventions.App.GENERATED_MESSAGES_CLASS_NAME;

		sbf.append(
				"\n\n/**\n * class that has static Constant definitions for all messageIds defined for this project.");
		sbf.append("\n */ ");
		sbf.append("\npublic class ").append(clsName).append(" {");
		for (String messageId : this.messages.keySet()) {
			sbf.append(P).append(messageId).append(" = \"").append(messageId).append("\";");
		}

		sbf.append("\n}\n");
		Util.writeOut(javaOutputRoot + clsName + ".java", sbf.toString());
		return true;

	}
}
