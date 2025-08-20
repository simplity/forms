package org.simplity.fm.example.gen.list;

import java.util.HashMap;
import org.simplity.fm.core.validn.KeyedValueList;
import org.simplity.fm.core.validn.SimpleValueList;

/**
 * ValuationType
 */
public class ValuationType extends KeyedValueList {
	private static final Object[] KEYS = {"1", "2", "3"
		};
	private static final Object[][][] VALUES = {
			{
				{"1", "Initial"}, 
				{"2", "Reval/Moderation"}, 
				{"3", "3rd Valuation"}
			}, 
			{
				{"2", "Revaluation"}, 
				{"3", "3rd Valuationn"}
			}, 
			{
				{"2", "Challenge"}, 
				{"3", "3rd Valuation"}
			}};
	private static final String NAME = "valuationType";

/**
 * ValuationType
 */
	public ValuationType() {
		this.name = NAME;
		this.values = new HashMap<>();
		for (int i = 0; i < KEYS.length;i++) {
			this.values.put(KEYS[i], new SimpleValueList(KEYS[i], VALUES[i]));
		}
	}
}
