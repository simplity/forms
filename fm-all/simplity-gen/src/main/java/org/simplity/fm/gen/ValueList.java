package org.simplity.fm.gen;

import java.util.HashMap;
import java.util.Map;

import org.simplity.fm.gen.Util.IInitializer;

/**
 * data structure for a Value List of any type
 *
 * @author simplity.org
 *
 */
public class ValueList implements IInitializer {
	private static final String LIST_SIMPLE = "simple";
	private static final String LIST_KEYED = "keyed";
	private static final String C = ", ";
	private static final String WHERE = " WHERE ";
	private static final String AND = " AND ";

	private String name;
	private String listType; // simple, keyed or runtime
	private Pair[] list; // in case it is a simple list
	private Map<String, Pair[]> keys; // in case this is a keyed-list

	/**
	 * attributes for a runtime list
	 */
	private String dbTableName;
	private String dbColumn1;
	private String dbColumn2;
	private String keyColumn;
	private boolean keyIsNumeric;
	private boolean valueIsNumeric;
	private String tenantColumnName;
	private String activeColumnName;
	/*
	 * in case this list is also required in batches
	 */
	private String parentTable;
	private String parentIdColumnName;
	private String parentNameColumnName;

	@Override
	public void initialize(String nam, int idx) {
		this.name = nam;
	}

	/**
	 * generate the .java file for this list
	 *
	 * @param folder
	 * @param packageName
	 */
	public void generateJava(String folder, String packageName) {
		final StringBuilder sbf = new StringBuilder();
		sbf.append("package ").append(packageName).append(';');
		sbf.append('\n');

		String clsName = Util.toClassName(this.name);

		if (this.listType.equals(LIST_SIMPLE)) {
			this.emitJavaSimple(sbf, clsName);
		} else if (this.listType.equals(LIST_KEYED)) {
			this.emitJavaKeyed(sbf, clsName);
		} else {
			this.emitJavaRuntime(sbf, clsName);
		}
		Util.writeOut(folder + clsName + ".java", sbf.toString());
	}

	private void emitJavaRuntime(StringBuilder sbf, final String clsName) {

		Util.emitImport(sbf, org.simplity.fm.core.validn.RuntimeList.class);

		sbf.append("\n\n/**\n * ").append(clsName).append("\n */");
		sbf.append("\npublic class ").append(clsName).append(" extends RuntimeList {");

		sbf.append("\n\t private static final String NAME = \"").append(this.name).append("\";");

		sbf.append("\n\t private static final String LIST_SQL = \"SELECT ");
		sbf.append(this.dbColumn1).append(C).append(this.dbColumn2).append(" FROM ").append(this.dbTableName);

		boolean whereAdded = false;
		if (this.activeColumnName != null) {
			sbf.append(WHERE).append(this.activeColumnName).append("=true");
			whereAdded = true;
		}
		if (this.keyColumn != null) {
			if (whereAdded) {
				sbf.append(AND);
			} else {
				sbf.append(WHERE);
				whereAdded = true;
			}
			sbf.append(this.keyColumn).append("=?");
		}
		if (this.tenantColumnName != null) {
			if (whereAdded) {
				sbf.append(AND);
			} else {
				sbf.append(WHERE);
				whereAdded = true;
			}
			sbf.append(this.tenantColumnName).append("=?");
		}
		sbf.append("\";");

		sbf.append("\n\t private static final String CHECK_SQL = \"SELECT ").append(this.dbColumn1).append(" FROM ")
				.append(this.dbTableName);
		sbf.append(WHERE).append(this.dbColumn1).append("=?");
		if (this.activeColumnName != null) {
			sbf.append(AND).append(this.activeColumnName).append("=true");
		}
		if (this.keyColumn != null) {
			sbf.append(AND).append(this.keyColumn).append("=?");
		}
		sbf.append("\";");

		if (this.parentTable != null) {
			sbf.append("\n\t private static final String ALL_SQL = \"SELECT a.").append(this.dbColumn1);
			sbf.append(", a.").append(this.dbColumn2).append(", b.").append(this.parentNameColumnName).append(" FROM ");
			sbf.append(this.dbTableName).append(" a, ").append(this.parentTable).append(" b ");
			sbf.append(WHERE).append("a.").append(this.keyColumn).append("=b.").append(this.parentIdColumnName);
			if (this.tenantColumnName != null) {
				sbf.append(AND).append(this.tenantColumnName).append("=?");
			}
			sbf.append("\";");
		}

		sbf.append("\n\n/**\n * ").append(clsName).append("\n */");
		sbf.append("\n\tpublic ").append(clsName).append("() {");
		sbf.append("\n\t\tthis.name = NAME;");
		sbf.append("\n\t\tthis.listSql = LIST_SQL;");
		sbf.append("\n\t\tthis.checkSql = CHECK_SQL;");

		if (this.valueIsNumeric) {
			sbf.append("\n\t\tthis.valueIsNumeric = true;");
		}

		if (this.keyColumn != null) {
			sbf.append("\n\t\tthis.hasKey = true;");
			if (this.keyIsNumeric) {
				sbf.append("\n\t\tthis.keyIsNumeric = true;");
			}
		}

		if (this.tenantColumnName != null) {
			sbf.append("\n\t\tthis.isTenantSpecific = true;");
		}

		sbf.append("\n\t}\n}\n");
	}

	private void emitJavaKeyed(StringBuilder sbf, final String clsName) {
		Util.emitImport(sbf, HashMap.class);
		Util.emitImport(sbf, org.simplity.fm.core.validn.KeyedValueList.class);
		Util.emitImport(sbf, org.simplity.fm.core.validn.ValueList.class);

		sbf.append("\n\n/**\n * ").append(clsName).append("\n */");
		sbf.append("\npublic class ").append(clsName).append(" extends KeyedValueList {");

		sbf.append("\n\tprivate static final Object[] KEYS = {");

		final StringBuilder vals = new StringBuilder();
		vals.append("\n\tprivate static final Object[][][] VALUES = {");

		for (final Map.Entry<String, Pair[]> entry : this.keys.entrySet()) {
			final String key = entry.getKey();
			if (this.keyIsNumeric) {
				sbf.append(key).append('L');
			} else {
				sbf.append(Util.quotedString(key.toString()));
			}
			sbf.append(C);
			emitJavaSet(vals, entry.getValue());
			vals.append(C);
		}
		sbf.setLength(sbf.length() - C.length());
		sbf.append("\n\t\t};");

		vals.setLength(vals.length() - C.length());
		vals.append("};");
		sbf.append(vals.toString());

		sbf.append("\n\tprivate static final String NAME = \"").append(this.name).append("\";");

		sbf.append("\n\n/**\n * ").append(clsName).append("\n */");

		sbf.append("\n\tpublic ").append(Util.toClassName(this.name)).append("() {");
		sbf.append("\n\t\tthis.name = NAME;");
		sbf.append("\n\t\tthis.values = new HashMap<>();");

		sbf.append("\n\t\tfor (int i = 0; i < KEYS.length;i++) {");
		sbf.append("\n\t\t\tthis.values.put(KEYS[i], new ValueList(KEYS[i], VALUES[i]));");
		sbf.append("\n\t\t}");
		sbf.append("\n\t}");
		sbf.append("\n}\n");

	}

	private static void emitJavaSet(final StringBuilder vals, final Pair[] ps) {
		vals.append("\n\t\t\t{");
		for (final Pair p : ps) {
			vals.append("\n\t\t\t\t{");
			if (p.value instanceof String) {
				vals.append(Util.quotedString(p.value.toString()));
			} else {
				vals.append(p.value).append('L');
			}
			vals.append(C).append(Util.quotedString(p.label)).append("}");
			vals.append(C);
		}
		vals.setLength(vals.length() - C.length());
		vals.append("\n\t\t\t}");
	}

	private void emitJavaSimple(StringBuilder sbf, final String clsName) {
		Util.emitImport(sbf, org.simplity.fm.core.validn.ValueList.class);

		sbf.append("\n\n/**\n * ").append(clsName).append("\n */");
		sbf.append("\n\npublic class ").append(Util.toClassName(this.name)).append(" extends ValueList {");
		sbf.append("\n\tprivate static final Object[][] VALUES = { ");

		for (final Pair p : this.list) {
			sbf.append("\n\t\t{");
			if (p.value instanceof String) {
				sbf.append(Util.quotedString(p.value.toString()));
			} else {
				/*
				 * it may be double because of the way JSON parsers work
				 */
				long n = ((Number) p.value).longValue();
				sbf.append(n).append('L');
			}
			sbf.append(C).append(Util.quotedString(p.label)).append("}");
			sbf.append(C);
		}

		sbf.setLength(sbf.length() - C.length());
		sbf.append("\n\t};");
		sbf.append("\n\t private static final String NAME = \"").append(this.name).append("\";");

		sbf.append("\n\n\t/**\n\t * @param name\n\t * @param valueList\n\t */");
		sbf.append("\n\tpublic ").append(Util.toClassName(this.name)).append("(String name, Object[][] valueList) {");
		sbf.append("\n\t\tsuper(name, valueList);");
		sbf.append("\n\t}");

		sbf.append("\n\n\t/**\n\t *").append(this.name).append("\n\t */");
		sbf.append("\n\tpublic ").append(Util.toClassName(this.name)).append("() {");
		sbf.append("\n\t\tsuper(NAME, VALUES);");
		sbf.append("\n\t}");

		sbf.append("\n}\n");
	}
}
