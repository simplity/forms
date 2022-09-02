package org.simplity.fm.gen;

import org.simplity.fm.core.datatypes.ValueType;

/**
 * Data Structure will all possible attributes for all types of values.
 *
 * @author simplity.org
 *
 */
public class ValueSchema implements Util.IInitializer {

	/**
	 * Default value schema to be used when it is missing
	 */
	public static ValueSchema DEFAULT_SCHEMA = getDefaultSchema("defaultText");
	private static final String P = "\n\tpublic static final ";
	private static final String C = ", ";

	String valueType;
	String name;
	String errorId;

	// text specific
	String pattern;
	int minLength;
	int maxLength = 25; // just a safe value if designer has not specified

	// date-specific
	int maxPastDays;
	int maxFutureDays;

	// integer/decimal specific
	long minValue;
	long maxValue;
	int nbrFractions;

	// computed
	ValueType valueTypeEnum = ValueType.Text;

	@Override
	public void initialize(final String nam, final int idx) {
		this.name = nam;
		try {
			this.valueTypeEnum = ValueType.valueOf(this.valueType);
		} catch (IllegalArgumentException | NullPointerException e) {
			// defaults to text
		}

		if (this.valueType.equals("integer")
				|| this.valueType.equals("decimal")) {
			final int n1 = ("" + this.minValue).length();
			final int n2 = ("" + this.maxValue).length();
			this.maxLength = (n1 > n2 ? n1 : n2);
			if (this.nbrFractions > 0) {
				this.maxLength += this.nbrFractions + 1;
			}
		} else if (this.valueType.equals("date")) {
			this.minValue = this.maxPastDays;
			this.maxValue = this.maxFutureDays;
		}

	}

	/**
	 *
	 * @param sbf
	 */
	public void emitJava(final StringBuilder sbf) {

		/**
		 * public static XyzType = new XyzType("name", "errorId",...);
		 *
		 */
		final String cls = Util.toClassName(this.valueType) + "Type";
		sbf.append(P).append(cls).append(' ').append(this.name)
				.append(" = new ").append(cls).append("(");

		// common parameters for the constructor
		sbf.append(Util.qoutedString(this.name)).append(C);
		sbf.append(Util.qoutedString(this.errorId));
		/**
		 * additional parameters to the constructor are to be added based on the
		 * type
		 */
		switch (this.valueType) {
		case "text" :
			sbf.append(C).append(this.minLength);
			sbf.append(C).append(this.maxLength);
			sbf.append(C).append(Util.qoutedString(this.pattern));
			break;

		case "integer" :
			sbf.append(C).append(this.minValue).append('L');
			sbf.append(C).append(this.maxValue).append('L');
			break;

		case "decimal" :
			sbf.append(C).append(this.minValue).append('L');
			sbf.append(C).append(this.maxValue).append('L');
			sbf.append(C).append(this.nbrFractions);
			break;

		case "date" :
			sbf.append(C).append(this.maxPastDays);
			sbf.append(C).append(this.maxFutureDays);
			break;
		}

		sbf.append(");");
	}

	void emitTs(StringBuilder sbf) {
		sbf.append("\n\t").append(this.name).append(": {");

		sbf.append("\n\t\tname: '").append(this.name).append("',");
		sbf.append("\n\t\tvalueType: '").append(this.valueType).append("',");
		if (this.errorId != null && this.errorId.isBlank() == false) {
			sbf.append("\n\t\terrorId: '").append(errorId).append("',");
		}
		if (this.pattern != null && this.pattern.isBlank() == false) {
			sbf.append("\n\t\tregex: ").append(Util.singleQuotedString(pattern))
					.append(C);
		}
		if (this.maxLength != 0) {
			sbf.append(",\n\t\tmaxLength: ").append(this.maxLength).append(C);
		}
		if (this.minLength != 0) {
			sbf.append(",\n\t\tminLength: ").append(this.minLength).append(C);
		}
		if (this.maxValue != 0) {
			sbf.append(",\n\t\tmaxValue: ").append(this.maxValue).append(C);
		}
		if (this.minValue != 0) {
			sbf.append(",\n\t\tminValue: ").append(this.minValue).append(C);
		}
		if (this.nbrFractions != 0) {
			sbf.append(",\n\t\tnbrFractions: ").append(this.nbrFractions)
					.append(C);
		}

		sbf.append("\n\t},");
	}

	/**
	 *
	 * @return value-type as string
	 */
	public String getValueType() {
		return this.valueType;
	}

	/**
	 *
	 * @return rendering type for this value type
	 */
	public String getRenderType() {
		if (this.valueTypeEnum == ValueType.Boolean) {
			return "checkbox";
		}
		if (this.maxLength > Application.TEXT_AREA_CUTOFF) {
			return "textarea";
		}
		return "text";
	}

	/**
	 * create a default text-schema with this name
	 *
	 * @param schemaName
	 * @return text-schema with this name
	 */
	public static ValueSchema getDefaultSchema(String schemaName) {
		ValueSchema vs = new ValueSchema();
		vs.name = schemaName;
		vs.maxLength = 200;
		vs.valueType = "text";
		vs.valueTypeEnum = ValueType.Text;
		return vs;
	}
}
