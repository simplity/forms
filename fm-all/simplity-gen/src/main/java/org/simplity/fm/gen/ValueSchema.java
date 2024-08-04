package org.simplity.fm.gen;

import java.time.Instant;
import java.time.LocalDate;

import org.simplity.fm.core.valueschema.ValueType;

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
	private static final String C = ",";

	String name;
	String valueType;
	String errorId;
	String validationFn;

	// text specific
	String regex;
	int minLength;
	int maxLength = 1000; // just a safe value if designer has not specified

	// date-specific
	int maxPastDays;
	int maxFutureDays;

	// integer/decimal specific
	long minValue;
	long maxValue;
	int nbrDecimalPlaces;

	// computed
	ValueType valueTypeEnum = ValueType.Text;

	@Override
	public void initialize(final String nam, final int idx) {
		this.name = nam;
		try {
			this.valueTypeEnum = ValueType.valueOf(Util.toClassName(this.valueType));
		} catch (IllegalArgumentException | NullPointerException e) {
			// defaults to text
		}

		if (this.valueType.equals("integer") || this.valueType.equals("decimal")) {
			final int n1 = ("" + this.minValue).length();
			final int n2 = ("" + this.maxValue).length();
			this.maxLength = (n1 > n2 ? n1 : n2);
			if (this.nbrDecimalPlaces > 0) {
				this.maxLength += this.nbrDecimalPlaces + 1;
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
		 * public static XyzSchema = new XyzSchema("name", "errorId",...);
		 *
		 */
		final String cls = Util.toClassName(this.valueType) + "Schema";
		sbf.append(P).append(cls).append(' ').append(this.name).append(" = new ").append(cls).append("(");

		// common parameters for the constructor
		sbf.append(Util.quotedString(this.name)).append(C);
		sbf.append(Util.quotedString(this.errorId));
		/**
		 * additional parameters to the constructor are to be added based on the type
		 */
		switch (this.valueType) {
		case "text":
			sbf.append(C).append(this.minLength);
			sbf.append(C).append(this.maxLength);
			sbf.append(C).append(Util.quotedString(this.regex));
			break;

		case "integer":
			sbf.append(C).append(this.minValue).append('L');
			sbf.append(C).append(this.maxValue).append('L');
			break;

		case "decimal":
			sbf.append(C).append(this.minValue).append('L');
			sbf.append(C).append(this.maxValue).append('L');
			sbf.append(C).append(this.nbrDecimalPlaces);
			break;

		case "date":
			sbf.append(C).append(this.maxPastDays);
			sbf.append(C).append(this.maxFutureDays);
			break;
		default:
			break;
		}

		sbf.append(");");
	}

	private static final String BEG = "\n\t\t";
	private static final char END = ',';

	void emitTs(StringBuilder sbf) {
		sbf.append("\n\t\"").append(this.name).append("\": {");

		Util.addAttr(sbf, BEG, "name", this.name);
		Util.addAttr(sbf, BEG, "valueType", this.valueType);
		if (this.errorId != null && this.errorId.isEmpty() == false) {
			Util.addAttr(sbf, BEG, "errorId", this.errorId);
		}
		if (this.regex != null && this.regex.isEmpty() == false) {
			Util.addAttr(sbf, BEG, "regex", this.regex);
		}
		if (this.maxLength != 0) {
			sbf.append(BEG).append("\"maxLength\": ").append(this.maxLength).append(END);
		}
		if (this.minLength != 0) {
			sbf.append(BEG).append("\"minLength\": ").append(this.minLength).append(END);
		}
		if (this.maxValue != 0) {
			sbf.append(BEG).append("\"maxValue\": ").append(this.maxValue).append(END);
		}
		if (this.minValue != 0) {
			sbf.append(BEG).append("\"minValue\": ").append(this.minValue).append(END);
		}
		if (this.nbrDecimalPlaces != 0) {
			sbf.append(BEG).append("\"nbrFractions\": ").append(this.nbrDecimalPlaces).append(END);
		}
		sbf.setLength(sbf.length() - 1);
		sbf.append("\n\t}");
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
			return "check-box";
		}
		if (this.maxLength > Application.TEXT_AREA_CUTOFF) {
			return "tex-area";
		}
		if (this.name.equalsIgnoreCase("password")) {
			return "password";
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

	/**
	 *
	 * @return sql constant for this type
	 */
	public String getDefaultConstant() {
		switch (this.valueTypeEnum) {
		case Boolean:
			return "false";
		case Decimal:
		case Integer:
			return "0";
		case Date:
			return " DATE '" + LocalDate.now().toString() + "'";
		case Text:
			return "''";
		case Timestamp:
			return " TIMESTAMP '" + Instant.now().toString() + "'";
		default:
			return "''";

		}
	}
}
