package org.simplity.fm.gen;

import java.util.HashMap;
import java.util.Map;

import org.simplity.fm.core.Conventions;
import org.simplity.fm.core.IValueSchemas;
import org.simplity.fm.core.valueschema.BooleanSchema;
import org.simplity.fm.core.valueschema.DateSchema;
import org.simplity.fm.core.valueschema.DecimalSchema;
import org.simplity.fm.core.valueschema.IntegerSchema;
import org.simplity.fm.core.valueschema.TextSchema;
import org.simplity.fm.core.valueschema.TimestampSchema;
import org.simplity.fm.core.valueschema.ValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author simplity.org
 *
 */
public class ValueSchemaMap {

	protected static final Logger logger = LoggerFactory.getLogger(ValueSchemaMap.class);

	private static final String C = ", ";
	private static final String TENANT_ID = "_tenantId";

	/**
	 * we use _text, _boolean etc.. as valueSchema for internal use when we know
	 * that we don't have to validate a value, but we need to specify a valueSchema
	 * 
	 * create default valueSchema for each valueType
	 * 
	 * @return map that is initialized with default ones
	 */
	private static Map<String, ValueSchema> mapWithDefaults() {
		Map<String, ValueSchema> schemas = new HashMap<>();
		for (ValueType vt : ValueType.values()) {
			String typ = Util.toName(vt.name());
			String name = "_" + typ;
			ValueSchema schema = new ValueSchema();
			schema.name = name;
			schema.valueType = typ;
			schema.initialize(name, 0);
			schemas.put(name, schema);
		}
		/*
		 * and one special one for tenantKey.
		 */
		ValueSchema schema = new ValueSchema();
		schema.name = TENANT_ID;
		schema.valueType = "integer";
		schema.initialize(TENANT_ID, 0);
		schemas.put(TENANT_ID, schema);
		return schemas;
	}

	private Map<String, ValueSchema> valueSchemas = mapWithDefaults();

	/**
	 *
	 * @param msgs messages
	 */
	public void setMap(Map<String, ValueSchema> schemas) {
		this.valueSchemas = schemas;
	}

	public Map<String, ValueSchema> getSchemas() {
		return this.valueSchemas;
	}

	/**
	 * 
	 * @param rootFolder
	 * @param packageName
	 */
	public boolean generateJava(final String rootFolder, final String packageName) {

		/*
		 * create ValueSchemas.java in the root folder.
		 */
		final StringBuilder sbf = new StringBuilder();
		sbf.append("package ").append(packageName).append(';');
		sbf.append('\n');

		Util.emitImport(sbf, HashMap.class);
		Util.emitImport(sbf, Map.class);
		sbf.append("\n");

		Util.emitImport(sbf, IValueSchemas.class);
		Util.emitImport(sbf, org.simplity.fm.core.valueschema.ValueSchema.class);
		Util.emitImport(sbf, TextSchema.class);
		Util.emitImport(sbf, IntegerSchema.class);
		Util.emitImport(sbf, DecimalSchema.class);
		Util.emitImport(sbf, BooleanSchema.class);
		Util.emitImport(sbf, DateSchema.class);
		Util.emitImport(sbf, TimestampSchema.class);

		final String clsName = Conventions.App.GENERATED_VALUE_SCHEMAS_CLASS_NAME;

		sbf.append(
				"\n\n/**\n * class that has static attributes for all value schemas defined for this project. It also extends <code>DataTypes</code>");
		sbf.append("\n */ ");
		sbf.append("\npublic class ").append(clsName).append(" implements IValueSchemas {");

		if (this.valueSchemas == null) {
			this.valueSchemas = new HashMap<>();
		}

		final StringBuilder schemaNames = new StringBuilder();
		for (final ValueSchema vs : this.valueSchemas.values()) {
			vs.emitJava(sbf);
			schemaNames.append(vs.name).append(C);
		}

		if (schemaNames.length() > 0) {
			schemaNames.setLength(schemaNames.length() - C.length());
		}

		sbf.append("\n\n\tpublic static final ValueSchema[] allSchemas = {").append(schemaNames.toString())
				.append("};");

		sbf.append("\n\t private Map<String, ValueSchema> schemaMap;");

		sbf.append("\n\t/**\n\t * default constructor\n\t */");

		sbf.append("\n\tpublic ").append(clsName).append("() {");
		sbf.append("\n\t\tthis.schemaMap = new HashMap<>();");
		sbf.append("\n\t\tfor(ValueSchema vs: allSchemas) {");
		sbf.append("\n\t\t\tthis.schemaMap.put(vs.getName(), vs);");
		sbf.append("\n\t\t}\n\t}");

		sbf.append("\n\n@Override");
		sbf.append("\n\tpublic ValueSchema getValueSchema(String schemaName) {");
		sbf.append("\n\t\treturn this.schemaMap.get(schemaName);");
		sbf.append("\n\t}");

		sbf.append("\n}\n");

		Util.writeOut(rootFolder + clsName + ".java", sbf.toString());
		return true;
	}

	public void init() {
		if (this.valueSchemas.size() == 0) {
			logger.warn("No Value Schemas are defined for this project");
		} else {
			Util.initializeMapEntries(this.valueSchemas);
		}
	}

}
