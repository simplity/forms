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
