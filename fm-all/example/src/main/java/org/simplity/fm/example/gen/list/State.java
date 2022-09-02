package org.simplity.fm.example.gen.list;

import java.util.HashMap;
import org.simplity.fm.core.validn.KeyedValueList;
import org.simplity.fm.core.validn.ValueList;

/**
 * State
 */
public class State extends KeyedValueList {
	private static final Object[] KEYS = {"130", "91"
		};
	private static final Object[][][] VALUES = {
			{
				{"Karnataka", "Karnataka"}, 
				{"Tamil Nadu", "Tamil Nadu"}, 
				{"Kerala", "Kerala"}, 
				{"Uttar Pradesh", "Uttar Pradesh"}
			}, 
			{
				{"Karnataka", "Karnataka"}, 
				{"Tamil Nadu", "Tamil Nadu"}, 
				{"Kerala", "Kerala"}, 
				{"Uttar Pradesh", "Uttar Pradesh"}
			}};
	private static final String NAME = "state";

/**
 * State
 */
	public State() {
		this.name = NAME;
		this.values = new HashMap<>();
		for (int i = 0; i < KEYS.length;i++) {
			this.values.put(KEYS[i], new ValueList(KEYS[i], VALUES[i]));
		}
	}
}
