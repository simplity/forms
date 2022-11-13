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
	private static final char SINGLEQUOTE = '\'';

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
	String hint;
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
		sbf.append(C).append(Util.qoutedString(this.defaultValue));
		// 6. error id
		sbf.append(C).append(Util.qoutedString(this.errorId));
		// 7. listName as string, null if not required
		/*
		 * list is handled by inter-field in case key is specified
		 */
		if (this.listKey == null) {
			sbf.append(C).append(Util.qoutedString(this.listName));
		} else {
			sbf.append(C).append("null");
		}

		// additional parameters for a DbField
		if (isDb) {
			// 7. column Name
			sbf.append(C).append(Util.qoutedString(this.nameInDb));
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
			def.append(indent).append("isRequired: true,");
		}

		if (this.listName != null) {
			def.append(indent).append("listName: ")
					.append(Util.singleQuotedString(this.listName))
					.append(COMA);
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
	static final String END = "',";
	static final char COMA = ',';

	/**
	 * @param sbf
	 */
	public void emitFormTs(final StringBuilder sbf) {
		sbf.append("\n\t\t").append(this.fieldName).append(": {");
		sbf.append(BEGIN).append("name: '").append(this.fieldName).append(END);
		sbf.append(BEGIN).append("valueSchema: '").append(this.valueSchema)
				.append(END);
		sbf.append(BEGIN).append("valueType: '")
				.append(this.schemaInstance.getValueType()).append(END);
		sbf.append(BEGIN).append("isRequired: ").append(this.isRequired)
				.append(COMA);
		String lbl = this.label;
		if (lbl == null || lbl.isEmpty()) {
			lbl = Util.toLabel(this.fieldName);
		}
		Util.addAttrTs(sbf, BEGIN, "label", lbl);
		Util.addAttrTs(sbf, BEGIN, "defaultValue", this.defaultValue);
		Util.addAttrTs(sbf, BEGIN, "icon", this.icon);
		Util.addAttrTs(sbf, BEGIN, "suffix", this.fieldSuffix);
		Util.addAttrTs(sbf, BEGIN, "prefix", this.fieldPrefix);
		Util.addAttrTs(sbf, BEGIN, "placeHolder", this.placeHolder);
		Util.addAttrTs(sbf, BEGIN, "hint", this.hint);
		Util.addAttrTs(sbf, BEGIN, "errorId", this.errorId);
		Util.addAttrTs(sbf, BEGIN, "listName", this.listName);
		Util.addAttrTs(sbf, BEGIN, "listKeyName", this.listKey);
		if (this.renderInList) {
			sbf.append(BEGIN).append("renderInList : true,");
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
			Util.addAttrTs(sbf, BEGIN, "renderType", rt);
		}
		sbf.append("\n\t\t}");
	}

	/**
	 *
	 * @param string
	 *            builder to which SQL is emitted
	 */
	void emitSql(StringBuilder sbf, StringBuilder dataSbf,
			StringBuilder valSbf) {
		sbf.append(this.nameInDb);
		dataSbf.append(this.nameInDb);
		valSbf.append(this.schemaInstance.getDefaultConstant());
		switch (this.fieldTypeEnum) {
		case CreatedAt :
		case ModifiedAt :
			sbf.append(" DATETIME NOT NULL");
			return;

		case CreatedBy :
		case ModifiedBy :
		case TenantKey :
			sbf.append(" INTEGER NOT NULL");
			return;

		case GeneratedPrimaryKey :
			sbf.append(" INTEGER GENERATED ALWAYS AS IDENTITY");
			return;

		case OptionalData :
		case PrimaryKey :
		case RequiredData :
			break;
		}

		switch (this.schemaInstance.valueTypeEnum) {
		case Boolean :
			sbf.append(" BOOLEAN NOT NULL DEFAULT FALSE");
			return;

		case Date :
			sbf.append(" DATE ");
			if (this.isRequired) {
				sbf.append("NOT NULL ");
			}
			if (this.defaultValue != null) {
				sbf.append("DEFAULT DATE ");
				if (this.defaultValue.equalsIgnoreCase("today")) {
					sbf.append("CURRENT_DATE ");
				} else {
					sbf.append(SINGLEQUOTE).append(this.defaultValue)
							.append("' ");
				}
			}
			return;

		case Decimal :
			// DECIMAL(max-digits,nbr-decimals)
			sbf.append(" DECIMAL(");
			sbf.append(this.schemaInstance.maxLength - 1);
			sbf.append(',').append(this.schemaInstance.nbrFractions)
					.append(") NOT NULL DEFAULT 0");
			return;

		case Integer :
			sbf.append(" INTEGER NULL DEFAULT ");
			if (this.defaultValue != null) {
				sbf.append(this.defaultValue);
			} else {
				sbf.append('0');
			}
			return;

		case Text :
			sbf.append(" CHARACTER VARYING NOT NULL DEFAULT '");
			if (this.defaultValue != null) {
				sbf.append(this.defaultValue);
			}
			sbf.append(SINGLEQUOTE);
			return;

		case Timestamp :
			sbf.append(" TIMESTAMP ");
			if (this.isRequired) {
				sbf.append("NOT NULL ");
			}
			if (this.defaultValue != null) {
				sbf.append("DEFAULT TIMESTAMP ");
				if (this.defaultValue.equalsIgnoreCase("now")) {
					sbf.append("CURRENT_TIMESTAMP ");
				} else {
					sbf.append(SINGLEQUOTE).append(this.defaultValue)
							.append(SINGLEQUOTE);
				}
			}
			return;
		}
	}

	boolean isColumn() {
		return this.nameInDb != null;
	}
}
