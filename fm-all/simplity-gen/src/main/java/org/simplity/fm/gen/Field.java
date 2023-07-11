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

import java.util.HashMap;
import java.util.Map;

import org.simplity.fm.core.ApplicationError;
import org.simplity.fm.core.Conventions;
import org.simplity.fm.core.data.FieldType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * represents a Field row in fields sheet of a forms work book
 *
 * @author simplity.org
 *
 */
class Field {
	private static final Map<String, FieldType> fieldTypes = createMap();
	private static final Logger logger = LoggerFactory.getLogger(Field.class);
	private static final String C = ", ";
	private static final char Q = '\'';

	String fieldName;
	String fieldType = "optionalData";
	String nameInDb;
	boolean isList;
	String valueSchema;
	String errorId;
	String defaultValue;
	String listName;
	String listKey;
	String label;
	String icon;
	String fieldSuffix;
	String fieldPrefix;
	String placeHolder;
	String description;
	boolean renderInList;
	boolean hideInSave;

	// synthetic attributes
	boolean isRequired;
	ValueSchema schemaInstance;

	int index;
	FieldType fieldTypeEnum;

	public void init(final int idx, Map<String, ValueSchema> schemas) {
		this.index = idx;
		this.schemaInstance = schemas.get(this.valueSchema);
		if (this.schemaInstance == null) {
			if (this.valueSchema == null) {
				logger.error(
						"Field {} has not defined a value-schema. A Default is assumed.",
						this.fieldName);
			} else {
				logger.error(
						"Field {} has specified {} as value-schema, but it is not defined. A default text-schema is used instead",
						this.valueSchema, this.fieldName);
			}
			this.schemaInstance = ValueSchema.DEFAULT_SCHEMA;
			this.valueSchema = this.schemaInstance.name;
		}

		this.fieldTypeEnum = fieldTypes.get(this.fieldType.toLowerCase());
		if (fieldTypeEnum == null) {
			logger.error(
					"{} is an invalid fieldType for field {}. optional data is  assumed",
					this.fieldType, this.fieldName);
			this.fieldType = "optionalData";
			this.fieldTypeEnum = FieldType.OptionalData;
		}
		this.isRequired = this.fieldTypeEnum == FieldType.RequiredData
				|| this.fieldTypeEnum == FieldType.PrimaryKey;
	}

	void emitJavaCode(final StringBuilder sbf, final boolean isDb) {
		sbf.append("\n\t\t\tnew ");
		if (isDb) {
			sbf.append("Db");
		}
		// 1. name
		sbf.append("Field(\"").append(this.fieldName).append('"');
		// 2. index
		sbf.append(C).append(this.index);
		// 3. schema name. All Schema names are statically defined in the main
		// class. e.g. DataTypes.schemaName
		sbf.append(C).append(Conventions.App.GENERATED_DATA_TYPES_CLASS_NAME)
				.append('.').append(this.valueSchema);
		// 4. isList as boolean
		sbf.append(C).append(this.isList);
		// 5. default value as string
		sbf.append(C).append(Util.quotedString(this.defaultValue));
		// 6. error id
		sbf.append(C).append(Util.quotedString(this.errorId));
		// 7. listName as string, null if not required
		/*
		 * list is handled by inter-field in case key is specified
		 */
		if (this.listKey == null) {
			sbf.append(C).append(Util.quotedString(this.listName));
		} else {
			sbf.append(C).append("null");
		}

		// additional parameters for a DbField
		if (isDb) {
			// 7. column Name
			sbf.append(C).append(Util.quotedString(this.nameInDb));
			// 8. columnType as Enum
			sbf.append(C).append("FieldType.")
					.append(this.fieldTypeEnum.name());
		} else {
			// 7. isRequired for non-db field
			sbf.append(C).append(this.isRequired);
		}
		sbf.append(')');
	}

	public void emitTs(final StringBuilder def, final String indent) {

		if (this.isRequired) {
			def.append(indent).append("\"isRequired\": true,");
		}

		if (this.listName != null) {
			def.append(indent).append("\"listName\": ")
					.append(Util.quotedString(this.listName)).append(COMA);
		}

	}

	/**
	 * @return
	 */
	private static Map<String, FieldType> createMap() {
		final Map<String, FieldType> map = new HashMap<>();
		for (final FieldType vt : FieldType.values()) {
			map.put(vt.name().toLowerCase(), vt);
		}
		return map;
	}

	static final String BEGIN = "\n\t\t\t";
	static final String END = "\",";
	static final char COMA = ',';

	/**
	 * @param sbf
	 */
	public void emitFormTs(final StringBuilder sbf) {
		sbf.append("\n\t\t\"").append(this.fieldName).append("\": {");
		sbf.append(BEGIN).append("\"name\": \"").append(this.fieldName)
				.append(END);
		sbf.append(BEGIN).append("\"valueSchema\": \"").append(this.valueSchema)
				.append(END);
		sbf.append(BEGIN).append("\"valueType\": \"")
				.append(this.schemaInstance.getValueType()).append(END);
		sbf.append(BEGIN).append("\"isRequired\": ").append(this.isRequired)
				.append(COMA);
		String lbl = this.label;
		if (lbl == null || lbl.isEmpty()) {
			lbl = Util.toLabel(this.fieldName);
		}
		Util.addAttr(sbf, BEGIN, "label", lbl);
		Util.addAttr(sbf, BEGIN, "defaultValue", this.defaultValue);
		Util.addAttr(sbf, BEGIN, "icon", this.icon);
		Util.addAttr(sbf, BEGIN, "suffix", this.fieldSuffix);
		Util.addAttr(sbf, BEGIN, "prefix", this.fieldPrefix);
		Util.addAttr(sbf, BEGIN, "placeHolder", this.placeHolder);
		Util.addAttr(sbf, BEGIN, "hint", this.description);
		Util.addAttr(sbf, BEGIN, "errorId", this.errorId);
		Util.addAttr(sbf, BEGIN, "listName", this.listName);
		Util.addAttr(sbf, BEGIN, "listKeyName", this.listKey);
		if (this.renderInList) {
			sbf.append(BEGIN).append("\"renderInList\" : true,");
		}
		if (this.fieldTypeEnum == FieldType.PrimaryKey
				|| this.fieldTypeEnum == FieldType.OptionalData
				|| this.fieldTypeEnum == FieldType.RequiredData) {
			String rt;
			if (this.listName != null) {
				rt = "select";
			} else {
				rt = this.schemaInstance.getRenderType();
			}
			Util.addAttr(sbf, BEGIN, "renderType", rt);
		}
		sbf.setLength(sbf.length() - 1);
		sbf.append("\n\t\t}");
	}

	/**
	 * not all fields are used for data-sql. caller may use the returned value
	 * to check this
	 *
	 * @param string
	 *            builder to which SQL is emitted
	 * @return true if sql for insert added, false otherwise.
	 */
	boolean emitSql(StringBuilder sbf, StringBuilder dataSbf,
			StringBuilder valSbf) {
		sbf.append(this.nameInDb);
		// generated primary key should not be included in the data sql
		if (this.fieldTypeEnum == FieldType.GeneratedPrimaryKey) {
			sbf.append(" INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY");
			return false;
		}

		dataSbf.append(this.nameInDb);

		switch (this.fieldTypeEnum) {
		case CreatedAt :
		case ModifiedAt :
			sbf.append(" TIMESTAMP NOT NULL");
			valSbf.append("CURRENT_TIMESTAMP");
			return true;

		case CreatedBy :
		case ModifiedBy :
		case TenantKey :
			sbf.append(" INTEGER NOT NULL");
			valSbf.append("1");
			return true;

		case OptionalData :
		case PrimaryKey :
		case RequiredData :
			break;

		default :
			throw new ApplicationError("FieldType " + this.fieldTypeEnum
					+ " not handled by field sql generator");
		}

		String value = "";
		switch (this.schemaInstance.valueTypeEnum) {
		case Boolean :
			sbf.append(" BOOLEAN NOT NULL DEFAULT FALSE");
			valSbf.append("false");
			return true;

		case Date :
			sbf.append(" DATE ");
			if (this.isRequired) {
				sbf.append("NOT NULL ");
			}

			value = "CURRENT_DATE";
			if (this.defaultValue != null) {
				sbf.append("DEFAULT DATE ");
				if (this.defaultValue.equalsIgnoreCase("today")) {
					sbf.append("CURRENT_DATE ");
				} else {
					sbf.append(Q).append(this.defaultValue).append("' ");
					value = "DATE '" + this.defaultValue + Q;
				}
			}
			valSbf.append(value);
			return true;

		case Decimal :
			// DECIMAL(max-digits,nbr-decimals)
			value = "0";
			sbf.append(" DECIMAL(");
			sbf.append(this.schemaInstance.maxLength - 1);
			sbf.append(',').append(this.schemaInstance.nbrFractions)
					.append(") NOT NULL DEFAULT ");
			if (this.defaultValue != null) {
				sbf.append(this.defaultValue);
				value = this.defaultValue;
			} else {
				sbf.append('0');
			}
			valSbf.append(value);
			return true;

		case Integer :
			value = "0";
			sbf.append(" INTEGER NOT NULL DEFAULT ");
			if (this.defaultValue != null) {
				value = this.defaultValue;
			}
			sbf.append(value);
			valSbf.append(value);
			return true;

		case Text :
			value = "''";
			sbf.append(" CHARACTER VARYING NOT NULL DEFAULT ");
			if (this.defaultValue != null) {
				value = this.defaultValue.replaceAll("'", "''");
			}

			sbf.append(value);
			valSbf.append(value);
			return true;

		case Timestamp :
			sbf.append(" TIMESTAMP ");
			if (this.isRequired) {
				sbf.append("NOT NULL ");
			}
			value = "TIMESTAMP CURRENT_TIMESTAMP";

			if (this.defaultValue != null) {
				if (this.defaultValue.equalsIgnoreCase("now") == false) {
					value = "TIMESTAMP " + this.defaultValue + Q;
				}
				sbf.append(value);
			}
			valSbf.append(value);
			return true;

		default :
			throw new ApplicationError(
					"ValueType " + this.schemaInstance.valueTypeEnum
							+ " not handled by field sql generator");
		}
	}

	boolean isColumn() {
		return this.nameInDb != null;
	}
}
