/*
 * Copyright (c) 2019 simplity.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.simplity.fm.gen;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.simplity.fm.core.Conventions;
import org.simplity.fm.core.datatypes.ValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

/**
 * represents a row in our spreadsheet for each data type
 *
 * @author simplity.org
 *
 */
class DataTypes {
	protected static final Logger logger = LoggerFactory.getLogger(DataTypes.class);
	private static final String C = ", ";

	Map<String, DataType> dataTypes = new HashMap<>();

	public void fromJson(final JsonReader reader) throws IOException {
		reader.beginObject();
		while (true) {
			final JsonToken token = reader.peek();
			if (token == JsonToken.END_OBJECT) {
				reader.endObject();
				return;
			}

			final String key = reader.nextName();
			switch (key) {
			case "textTypes":
				Util.addToMap(this.dataTypes, reader, TextType.class);
				continue;
			case "integerTypes":
				Util.addToMap(this.dataTypes, reader, IntegerType.class);
				continue;
			case "decimalTypes":
				Util.addToMap(this.dataTypes, reader, DecimalType.class);
				continue;
			case "booleanTypes":
				Util.addToMap(this.dataTypes, reader, BooleanType.class);
				continue;
			case "dateTypes":
				Util.addToMap(this.dataTypes, reader, DateType.class);
				continue;
			case "timestampTypes":
				Util.addToMap(this.dataTypes, reader, TimestampType.class);
				continue;
			default:
				logger.warn("{} is not a valid attribute of DataTypes. Ignored", key);
				continue;
			}

		}
	}

	void emitTs(final StringBuilder sbf) {
		boolean isFirst = true;
		for (final DataType dt : this.dataTypes.values()) {
			if (isFirst) {
				isFirst = false;
			} else {
				sbf.append(',');
			}
			sbf.append("\n\t").append(dt.name).append(": {");

			sbf.append("\n\t\tname: '").append(dt.name).append("',");
			sbf.append("\n\t\tvalueType: '").append(Util.toName(dt.valueType.name())).append("'");

			emitOptional(sbf, "errorId", dt.errorId);
			emitOptional(sbf, "regex", dt.getRegex());
			emitOptional(sbf, "maxLength", dt.getMaxLength());
			emitOptional(sbf, "minLength", dt.getMinLength());
			emitOptional(sbf, "maxValue", dt.getMaxValue());
			emitOptional(sbf, "minValue", dt.getMinValue());
			emitOptional(sbf, "nbrDecimalPlaces", dt.getNbrDecimals());

			sbf.append("\n\t}");
		}
	}

	private static void emitOptional(final StringBuilder sbf, final String att, final String val) {
		if (val == null || val.isEmpty()) {
			return;
		}

		sbf.append(",\n\t\t").append(att).append(": ").append(Util.escape(val));
	}

	private static void emitOptional(final StringBuilder sbf, final String att, final long val) {
		if (val == 0) {
			return;
		}

		sbf.append(",\n\t\t").append(att).append(": ").append(val);
	}

	void emitJava(final String rootFolder, final String packageName, final String dataTypesFileName) {
		/*
		 * create DataTypes.java in the root folder.
		 */
		final StringBuilder sbf = new StringBuilder();
		this.emitJavaTypes(sbf, packageName);
		Util.writeOut(rootFolder + dataTypesFileName + ".java", sbf);
	}

	void emitJavaTypes(final StringBuilder sbf, final String packageName) {
		sbf.append("package ").append(packageName).append(';');
		sbf.append('\n');

		Util.emitImport(sbf, HashMap.class);
		Util.emitImport(sbf, Map.class);
		sbf.append("\n");

		Util.emitImport(sbf, org.simplity.fm.core.IDataTypes.class);
		Util.emitImport(sbf, org.simplity.fm.core.datatypes.DataType.class);
		for (final ValueType vt : ValueType.values()) {
			Util.emitImport(sbf, Util.getDataTypeClass(vt));
		}

		final String cls = Conventions.App.GENERATED_DATA_TYPES_CLASS_NAME;

		sbf.append(
				"\n\n/**\n * class that has static attributes for all data types defined for this project. It also extends <code>DataTypes</code>");
		sbf.append("\n */ ");
		sbf.append("\npublic class ").append(cls).append(" implements IDataTypes {");

		final StringBuilder dtNames = new StringBuilder();
		for (final DataType dt : this.dataTypes.values()) {
			dt.emitJava(sbf);
			dtNames.append(dt.name).append(C);
		}
		if (dtNames.length() > 0) {
			dtNames.setLength(dtNames.length() - C.length());
		}

		sbf.append("\n\n\tpublic static final DataType[] allTypes = {").append(dtNames.toString()).append("};");

		sbf.append("\n\t private Map<String, DataType> typesMap;");

		sbf.append("\n\t/**\n\t * default constructor\n\t */");

		sbf.append("\n\tpublic ").append(cls).append("() {");
		sbf.append("\n\t\tthis.typesMap = new HashMap<>();");
		sbf.append("\n\t\tfor(DataType dt: allTypes) {");
		sbf.append("\n\t\t\tthis.typesMap.put(dt.getName(), dt);");
		sbf.append("\n\t\t}\n\t}");

		sbf.append("\n\n@Override");
		sbf.append("\n\tpublic DataType getDataType(String name) {");
		sbf.append("\n\t\treturn this.typesMap.get(name);");
		sbf.append("\n\t}");

		sbf.append("\n}\n");
	}

	protected abstract static class DataType implements Util.INamedMember {
		private static final String P = "\n\tpublic static final ";
		ValueType valueType;
		String name;
		String errorId;

		@Override
		public void setNameAndIdx(final String name, final int idx) {
			this.name = name;
		}

		/**
		 * @param sbf
		 */
		protected void emitJava(final StringBuilder sbf) {
			final String cls = this.getClass().getSimpleName();
			sbf.append(P).append(cls).append(' ').append(this.name);
			sbf.append(" = new ").append(cls).append("(").append(Util.escape(this.name));
			sbf.append(C).append(Util.escape(this.errorId));
			this.emitIstanceParams(sbf);
			sbf.append(");");
		}

		protected abstract int getMaxLength();

		/* concrete classes override this if required to be emitted for ts */

		protected int getMinLength() {
			return 0;
		}

		protected long getMinValue() {
			return 0;
		}

		protected long getMaxValue() {
			return 0;
		}

		protected int getNbrDecimals() {
			return 0;
		}

		protected String getRegex() {
			return null;
		}

		protected abstract void emitIstanceParams(StringBuilder sbf);

		void emitTs(final StringBuilder sbf, final String defaultValue, final List<String> vals, final String prefix) {
			if (this.name.equalsIgnoreCase("email")) {
				vals.add("email");
			}
			sbf.append(prefix).append("valueType: ").append(this.valueType.ordinal());
			if (defaultValue != null) {
				sbf.append(prefix).append("defaultValue: ");
				if (this.valueType == ValueType.Text || this.valueType == ValueType.Date) {
					sbf.append(Util.escapeTs(defaultValue));
				} else {
					sbf.append(defaultValue);
				}
			}

			if (this.errorId != null) {
				sbf.append(prefix).append("errorId: '").append(this.errorId).append('\'');
			}
			this.emitTsSpecific(sbf, vals, prefix);
		}

		@SuppressWarnings("unused")
		protected void emitTsSpecific(final StringBuilder sbf, final List<String> vals, final String prefix) {
			// let concrete class add if required
		}
	}

	protected static class BooleanType extends DataType {
		BooleanType() {
			this.valueType = ValueType.Boolean;
		}

		@Override
		protected void emitIstanceParams(final StringBuilder sbf) {
			//
		}

		@Override
		protected int getMaxLength() {
			// boolean is almost always a check-box or drop-down, but our design
			// requires a max as a safety ALWAYS
			return 10;
		}

	}

	protected static class DateType extends DataType {
		int maxPastDays;
		int maxFutureDays;

		DateType() {
			this.valueType = ValueType.Date;
		}

		@Override
		protected void emitIstanceParams(final StringBuilder sbf) {
			sbf.append(C).append(this.maxPastDays);
			sbf.append(C).append(this.maxFutureDays);
		}

		@Override
		protected void emitTsSpecific(final StringBuilder sbf, final List<String> vals, final String prefix) {
			sbf.append(prefix).append("minValue: ").append(this.maxPastDays);
			sbf.append(prefix).append("maxValue: ").append(this.maxFutureDays);
		}

		@Override
		protected int getMaxLength() {
			// 25, just a a safety
			return 25;
		}

		@Override
		protected long getMaxValue() {
			return this.maxFutureDays;
		}

		@Override
		protected long getMinValue() {
			return this.maxPastDays;
		}
	}

	protected static class IntegerType extends DataType {
		long minValue;
		long maxValue;

		IntegerType() {
			this.valueType = ValueType.Integer;
		}

		@Override
		protected void emitIstanceParams(final StringBuilder sbf) {
			sbf.append(C).append(this.minValue).append('L');
			sbf.append(C).append(this.maxValue).append('L');
		}

		@Override
		protected void emitTsSpecific(final StringBuilder sbf, final List<String> vals, final String prefix) {
			if (this.minValue != 0) {
				sbf.append(prefix).append("minValue: ").append(this.minValue);
				vals.add("min(" + this.minValue + ')');
			}
			if (this.maxValue != 0) {
				sbf.append(prefix).append("maxValue: ").append(this.maxValue);
				vals.add("max(" + this.maxValue + ')');
			}
		}

		@Override
		protected int getMaxLength() {
			final int n1 = ("" + this.minValue).length() + 1;
			final int n2 = ("" + this.maxValue).length() + 1;
			return n1 > n2 ? n1 : n2;
		}

		@Override
		protected long getMaxValue() {
			return this.maxValue;
		}

		@Override
		protected long getMinValue() {
			return this.minValue;
		}
	}

	protected static class DecimalType extends DataType {
		long minValue;
		long maxValue;
		int nbrFractions;

		DecimalType() {
			this.valueType = ValueType.Decimal;
		}

		@Override
		protected void emitIstanceParams(final StringBuilder sbf) {
			sbf.append(C).append(this.minValue).append('L');
			sbf.append(C).append(this.maxValue).append('L');
			sbf.append(C).append(this.nbrFractions);
		}

		@Override
		protected void emitTsSpecific(final StringBuilder sbf, final List<String> vals, final String prefix) {
			sbf.append(prefix).append("nbrFractions: ").append(this.nbrFractions);
			if (this.minValue != 0) {
				sbf.append(prefix).append("minValue: ").append(this.minValue);
				vals.add("min(" + this.minValue + ')');
			}
			if (this.maxValue != 0) {
				sbf.append(prefix).append("maxValue: ").append(this.maxValue);
				vals.add("max(" + this.maxValue + ')');
			}
		}

		@Override
		protected int getMaxLength() {
			final int n1 = ("" + this.minValue).length();
			final int n2 = ("" + this.maxValue).length();
			return (n1 > n2 ? n1 : n2) + this.nbrFractions + 1;
		}

		@Override
		protected long getMaxValue() {
			return this.maxValue;
		}

		@Override
		protected long getMinValue() {
			return this.minValue;
		}

		@Override
		protected int getNbrDecimals() {
			return this.nbrFractions;
		}
	}

	protected static class TimestampType extends DataType {
		TimestampType() {
			this.valueType = ValueType.Timestamp;
		}

		@Override
		protected void emitIstanceParams(final StringBuilder sbf) {
			//
		}

		@Override
		protected int getMaxLength() {
			return 25;
		}
	}

	protected static class TextType extends DataType {
		String regex;
		int minLength;
		int maxLength;

		TextType() {
			this.valueType = ValueType.Text;
		}

		@Override
		protected void emitIstanceParams(final StringBuilder sbf) {
			sbf.append(C).append(this.minLength);
			sbf.append(C).append(this.maxLength);
			sbf.append(C).append(Util.escape(this.regex));
		}

		@Override
		protected void emitTsSpecific(final StringBuilder sbf, final List<String> vals, final String prefix) {
			if (this.minLength != 0) {
				sbf.append(prefix).append("minLength: ").append(this.minLength);
				vals.add("minLength(" + this.minLength + ')');
			}
			if (this.maxLength != 0) {
				sbf.append(prefix).append("maxLength: ").append(this.maxLength);
				vals.add("maxLength(" + this.maxLength + ')');
			}
			if (this.regex != null) {
				vals.add("pattern(" + Util.escapeTs(this.regex) + ")");
			}
		}

		@Override
		protected int getMaxLength() {
			return this.maxLength;
		}

		@Override
		protected int getMinLength() {
			return this.minLength;
		}

		@Override
		protected String getRegex() {
			return this.regex;
		}
	}
}
