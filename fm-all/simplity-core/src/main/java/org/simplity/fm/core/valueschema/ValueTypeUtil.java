package org.simplity.fm.core.valueschema;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * utility class to deal with value types
 *
 * @author simplity.org
 *
 */
public class ValueTypeUtil {
	/**
	 * On a need basis, we "guess" the intended vakueType of a object at run
	 * time
	 */

	private static Map<Class<?>, ValueType> CLASS_TO_VT = new HashMap<>();
	static {
		CLASS_TO_VT.put(String.class, ValueType.Text);
		CLASS_TO_VT.put(LocalDate.class, ValueType.Date);
		CLASS_TO_VT.put(Instant.class, ValueType.Timestamp);
		CLASS_TO_VT.put(Byte.class, ValueType.Integer);
		CLASS_TO_VT.put(Short.class, ValueType.Integer);
		CLASS_TO_VT.put(Integer.class, ValueType.Integer);
		CLASS_TO_VT.put(Long.class, ValueType.Integer);
		CLASS_TO_VT.put(Double.class, ValueType.Decimal);
		CLASS_TO_VT.put(Float.class, ValueType.Decimal);
		CLASS_TO_VT.put(Boolean.class, ValueType.Boolean);
	}

	/**
	 *
	 * @param value
	 *            non-null value-object
	 * @return valueType. Text in case the object is not one of the standard
	 *         types we use to store data
	 */
	public static ValueType valueTypeOf(Object value) {
		final ValueType vt = CLASS_TO_VT.get(value.getClass());
		if (vt == null) {
			return ValueType.Text;
		}
		return vt;
	}

	/**
	 *
	 * @param values
	 *            non-null value-object
	 * @return valueTypes. Text in case the object is not one of the standard
	 *         types we use to store data
	 */
	public static ValueType[] valueTypesOf(Object[] values) {
		ValueType[] types = new ValueType[values.length];
		for (int i = 0; i < types.length; i++) {
			ValueType vt = CLASS_TO_VT.get(values[i].getClass());
			if (vt == null) {
				vt = ValueType.Text;
			}
			types[i] = vt;
		}
		return types;
	}

}
