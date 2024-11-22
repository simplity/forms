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
import org.simplity.fm.core.valueschema.ValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * represents a Field row in fields sheet of a forms work book
 *
 * @author simplity.org
 *
 */
class Field implements Cloneable {
	private static final Map<String, FieldType> fieldTypes = createMap();
	private static final Logger logger = LoggerFactory.getLogger(Field.class);
	private static final String C = ", ";
	private static final char Q = '\'';

	String name;
	String fieldType = "optionalData";
	String nameInDb;
	boolean isList;
	String valueType;
	String valueSchema;
	String messageId;
	String defaultValue;
	String listName;
	String listKey;
	String label;
	String icon;
	String fieldSuffix;
	String fieldPrefix;
	String placeHolder;
	String description;
	boolean visibleInList;
	boolean visibleInSave;
	String renderAs;

	// synthetic attributes
	boolean isRequired;
	ValueSchema schemaInstance;

	int index;
	FieldType fieldTypeEnum;
	ValueType valueTypeEnum;

	public Field() {
		super();

	}

	public void init(final int idx, Map<String, ValueSchema> schemas) {
		this.index = idx;
		try {
			final String vt = this.valueType.toUpperCase().charAt(0) + this.valueType.substring(1);
			this.valueTypeEnum = ValueType.valueOf(vt);
		} catch (Exception e) {
			logger.error("Field {} has an invalid valueType={}. 'text' assumed.", this.name, this.valueType);
			this.valueTypeEnum = ValueType.Text;
		}

		if (this.valueSchema != null) {
			this.schemaInstance = schemas.get(this.valueSchema);
			if (this.schemaInstance == null) {
				logger.error("Field {} has specified {} as value-schema, but it is not defined. Ignored", this.name,
						this.valueSchema);
				this.valueSchema = null;
			}
		}

		this.fieldTypeEnum = fieldTypes.get(this.fieldType.toLowerCase());
		if (this.fieldTypeEnum == null)

		{
			logger.error("{} is an invalid fieldType for field {}. optional data is  assumed", this.fieldType,
					this.name);
			this.fieldType = "optionalData";
			this.fieldTypeEnum = FieldType.OptionalData;
		}
		this.isRequired = this.fieldTypeEnum == FieldType.RequiredData || this.fieldTypeEnum == FieldType.PrimaryKey;
	}

	/**
	 * must be called only after init() of this field
	 */
	Field makeACopy(final int idx) {
		try {
			Field copy = (Field) super.clone();
			copy.index = idx;
			return copy;
		} catch (Exception e) {
			throw new ApplicationError("Field.makeACopy() is broken!!");
		}
	}

	void emitJavaCode(final StringBuilder sbf, final boolean isDb) {
		sbf.append("\n\t\t\tnew ");
		if (isDb) {
			sbf.append("Db");
		}
		// 1. name
		sbf.append("Field(\"").append(this.name).append('"');
		// 2. index
		sbf.append(C).append(this.index);
		// 3. valueType
		sbf.append(", ValueType.").append(this.valueTypeEnum.name());
		// 4. schema name. All Schema names are statically defined in the main
		// class. e.g. DataTypes.schemaName
		if (this.valueSchema == null) {
			sbf.append(", null");
		} else {
			sbf.append(C).append(Conventions.App.GENERATED_VALUE_SCHEMAS_CLASS_NAME).append('.')
					.append(this.valueSchema);
		}
		// 5. isList as boolean
		sbf.append(C).append(this.isList);
		// 6. default value as string
		sbf.append(C).append(Util.quotedString(this.defaultValue));
		// 7. message id
		sbf.append(C).append(Util.quotedString(this.messageId));
		// 8. listName as string, null if not required
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
			sbf.append(C).append("FieldType.").append(this.fieldTypeEnum.name());
		} else {
			// 7. isRequired for non-db field
			sbf.append(C).append(this.isRequired);
		}
		sbf.append(')');
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
	 * not all fields are used for data-sql. caller may use the returned value to
	 * check this
	 *
	 * @param string builder to which SQL is emitted
	 * @return true if sql for insert added, false otherwise.
	 */
	boolean emitSql(StringBuilder sbf, StringBuilder dataSbf, StringBuilder valSbf) {
		sbf.append(this.nameInDb);
		// generated primary key should not be included in the data sql
		if (this.fieldTypeEnum == FieldType.GeneratedPrimaryKey) {
			sbf.append(" INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY");
			return false;
		}

		dataSbf.append(this.nameInDb);

		switch (this.fieldTypeEnum) {
		case CreatedAt:
		case ModifiedAt:
			sbf.append(" TIMESTAMP NOT NULL");
			valSbf.append("CURRENT_TIMESTAMP");
			return true;

		case CreatedBy:
		case ModifiedBy:
		case TenantKey:
			sbf.append(" INTEGER NOT NULL");
			valSbf.append("1");
			return true;

		case OptionalData:
		case PrimaryKey:
		case RequiredData:
			break;

		default:
			throw new ApplicationError("FieldType " + this.fieldTypeEnum + " not handled by field sql generator");
		}

		String value = "";
		switch (this.valueTypeEnum) {
		case Boolean:
			sbf.append(" BOOLEAN NOT NULL DEFAULT FALSE");
			valSbf.append("false");
			return true;

		case Date:
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

		case Decimal:
			// DECIMAL(max-digits,nbr-decimals)
			value = "0";
			sbf.append(" DECIMAL(");
			int size = 13;
			int nbrDecimals = 2;
			if (this.schemaInstance != null) {
				size = this.schemaInstance.maxLength - 1;
				nbrDecimals = this.schemaInstance.nbrDecimalPlaces;
			}
			sbf.append(size).append(',').append(nbrDecimals).append(") NOT NULL DEFAULT ");
			if (this.defaultValue != null) {
				sbf.append(this.defaultValue);
				value = this.defaultValue;
			} else {
				sbf.append('0');
			}
			valSbf.append(value);
			return true;

		case Integer:
			value = "0";
			sbf.append(" INTEGER NOT NULL DEFAULT ");
			if (this.defaultValue != null) {
				value = this.defaultValue;
			}
			sbf.append(value);
			valSbf.append(value);
			return true;

		case Text:
			value = "''";
			sbf.append(" CHARACTER VARYING NOT NULL DEFAULT ");
			if (this.defaultValue != null) {
				value = this.defaultValue.replaceAll("'", "''");
			}

			sbf.append(value);
			valSbf.append(value);
			return true;

		case Timestamp:
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

		default:
			throw new ApplicationError("ValueType " + this.valueTypeEnum + " not handled by field sql generator");
		}
	}

	boolean isColumn() {
		return this.nameInDb != null;
	}
}
