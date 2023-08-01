package org.simplity.fm.example.gen;

import java.util.HashMap;
import java.util.Map;

import org.simplity.fm.core.IValueSchemas;
import org.simplity.fm.core.valueschema.BooleanSchema;
import org.simplity.fm.core.valueschema.ValueSchema;
import org.simplity.fm.core.valueschema.DateSchema;
import org.simplity.fm.core.valueschema.DecimalSchema;
import org.simplity.fm.core.valueschema.IntegerSchema;
import org.simplity.fm.core.valueschema.TextSchema;
import org.simplity.fm.core.valueschema.TimestampSchema;

/**
 * class that has static attributes for all data types defined for this project. It also extends <code>DataTypes</code>
 */ 
public class DefinedValueSchemas implements IValueSchemas {
	public static final BooleanSchema bool = new BooleanSchema("bool", "invalidBool");
	public static final DecimalSchema grade = new DecimalSchema("grade", "invalidGrade", 0L, 100L, 2);
	public static final DateSchema date = new DateSchema("date", "invalidDate", 73000, 73000);
	public static final IntegerSchema country = new IntegerSchema("country", "invalidCountry", 0L, 999L);
	public static final IntegerSchema flexibleId = new IntegerSchema("flexibleId", "invalidFlexibleId", -1L, 9999999999999L);
	public static final IntegerSchema id = new IntegerSchema("id", "invalidId", 0L, 9999999999999L);
	public static final IntegerSchema integer = new IntegerSchema("integer", "invalidInteger", 0L, 9999999999999L);
	public static final IntegerSchema income = new IntegerSchema("income", "invalidIncome", 0L, 9999999999999L);
	public static final IntegerSchema marks = new IntegerSchema("marks", "invalidMarks", 0L, 100L);
	public static final IntegerSchema tenantKey = new IntegerSchema("tenantKey", "invalidTenentKey", 0L, 9999999999999L);
	public static final IntegerSchema valuationType = new IntegerSchema("valuationType", "invalidValuationType", 0L, 3L);
	public static final TextSchema accountStatus = new TextSchema("accountStatus", "invalidAccountStatus", 0, 10, null);
	public static final TextSchema code = new TextSchema("code", "invalidCode", 0, 50, null);
	public static final TextSchema desc = new TextSchema("desc", "invalidDesc", 0, 1000, null);
	public static final TextSchema email = new TextSchema("email", "invalidEmail", 0, 1000, null);
	public static final TextSchema gender = new TextSchema("gender", "invalidGender", 0, 10, null);
	public static final TextSchema govtCode = new TextSchema("govtCode", "invalidGovtCode", 0, 50, null);
	public static final TextSchema ip = new TextSchema("ip", "invalidIp", 0, 1000, null);
	public static final TextSchema json = new TextSchema("json", "invalidJson", 0, 10000, null);
	public static final TextSchema loginId = new TextSchema("loginId", "invalidLoginId", 0, 50, null);
	public static final TextSchema marksOrAb = new TextSchema("marksOrAb", "invalidMarksOrAb", 0, 3, null);
	public static final TextSchema name = new TextSchema("name", "invalidName", 0, 50, null);
	public static final TextSchema phone = new TextSchema("phone", "invalidPhone", 10, 12, null);
	public static final TextSchema pin = new TextSchema("pin", "invalidPin", 6, 6, null);
	public static final TextSchema state = new TextSchema("state", "invalidState", 0, 50, null);
	public static final TextSchema text = new TextSchema("text", "invalidText", 0, 1000, null);
	public static final TextSchema uniqueId = new TextSchema("uniqueId", "invalidUniqueId", 16, 16, null);
	public static final TextSchema url = new TextSchema("url", "invalidUrl", 0, 1000, null);
	public static final TextSchema userType = new TextSchema("userType", "invalidUserType", 0, 20, null);
	public static final TimestampSchema timestamp = new TimestampSchema("timestamp", "invalidTimestamp");

	public static final ValueSchema[] allTypes = {bool, grade, date, country, flexibleId, id, integer, income, marks, tenantKey, valuationType, accountStatus, code, desc, email, gender, govtCode, ip, json, loginId, marksOrAb, name, phone, pin, state, text, uniqueId, url, userType, timestamp};
	 private Map<String, ValueSchema> typesMap;
	/**
	 * default constructor
	 */
	public DefinedValueSchemas() {
		this.typesMap = new HashMap<>();
		for(ValueSchema dt: allTypes) {
			this.typesMap.put(dt.getName(), dt);
		}
	}

@Override
	public ValueSchema getDataValueSchema(String name) {
		return this.typesMap.get(name);
	}
}
