package org.simplity.fm.gen.db;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;

import org.simplity.fm.core.data.DataTable;
import org.simplity.fm.core.db.IDbDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetaUtil {
	private static final Logger logger = LoggerFactory.getLogger(MetaUtil.class);

	/**
	 * get details of tables/views
	 * 
	 * @param driver         dbDriver
	 * @param tables         map of tableName-record
	 * @param columnsOfTable map of tableName-dataRows that are compatible with
	 *                       ColumnMetaRecord
	 * @return number of tables/views
	 * @throws SQLException
	 */
	public static final int getTableDetails(final IDbDriver driver, final Map<String, TableMetaRecord> tables,
			final Map<String, DataTable<ColumnMetaRecord>> columnsOfTable) throws SQLException {
		int existingRows = tables.size();
		driver.doReadMetaData(metaData -> {
			try (ResultSet rs = metaData.getTables(null, null, "%", new String[] { "TABLE", "VIEW" })) {
				while (rs.next()) {
					TableMetaRecord tableRecord = pouplateTable(rs);
					String recordName = tableRecord.getName();
					tables.put(recordName, tableRecord);

					try (ResultSet rs1 = metaData.getColumns(null, null, tableRecord.getNameInDb(), "%")) {
						DataTable<ColumnMetaRecord> columns = pouplateColumns(rs1);
						columnsOfTable.put(recordName, columns);
						logger.info(tables.size() + ". " + recordName + ": " + columns.length() + " columns added.");
					}
				}
			}
		});
		return tables.size() - existingRows;
	}

	private static TableMetaRecord pouplateTable(ResultSet rs) throws SQLException {
		final String tableName = rs.getString("TABLE_NAME");
		final String tableFieldName = toFieldName(tableName);
		final String label = toLabel(tableName);
		String tableType = rs.getString("TABLE_TYPE");
		String remarks = rs.getString("REMARKS");
		if (remarks == null) {
			remarks = label;
		}

		TableMetaRecord table = new TableMetaRecord();
		table.setName(tableFieldName);
		table.setDescription(remarks);
		table.setNameInDb(tableName);
		table.setIsView(tableType.equals("VIEW"));

		return table;
	}

	private static DataTable<ColumnMetaRecord> pouplateColumns(ResultSet rs) throws SQLException {
		DataTable<ColumnMetaRecord> columns = new DataTable<>(new ColumnMetaRecord());
		while (rs.next()) {
			/**
			 * get values from the RS
			 */
			String columnName = rs.getString("COLUMN_NAME");
			String typeName = rs.getString("TYPE_NAME");
			int type = rs.getInt("DATA_TYPE");
			int size = rs.getInt("COLUMN_SIZE");
			int nbrDigits = rs.getInt("DECIMAL_DIGITS");
			int nullable = rs.getInt("DECIMAL_DIGITS");
			String autoIncremenet = rs.getString("IS_AUTOINCREMENT");
			String desc = rs.getString("REMARKS");
			if (desc == null) {
				desc = toLabel(columnName);
			}
			/**
			 * calculate derived fields
			 */
			String fieldType = "requiredData";
			String renderAs = "text-field";
			String valueType = toValueType(type);
			if (autoIncremenet.equals("YES")) {
				fieldType = "generatedPrimaryKey";
				renderAs = "hidden";
			} else if (nullable == DatabaseMetaData.columnNoNulls) {
				fieldType = "optionalData";
			}

			String valueTypeInfo = typeName + " " + size;
			if (nbrDigits > 0) {
				valueTypeInfo += " decimals=" + nbrDigits;
			}

			/**
			 * push values into a record
			 */
			ColumnMetaRecord column = new ColumnMetaRecord();
			column.setName(toFieldName(columnName));
			column.setNameInDb(columnName);
			column.setDescription(desc);
			column.setFieldType(fieldType);
			column.setValueType(valueType);
			column.setValueTypeInfo(valueTypeInfo);
			column.setLabel(toLabel(columnName));
			column.setRenderAs(renderAs);

			/**
			 * add record to the table
			 */
			columns.addRecord(column);
		}
		return columns;

	}

	private static String toValueType(int sqlType) {
		switch (sqlType) {
		case Types.BIT:
		case Types.BOOLEAN:
			return "boolean";

		case Types.DECIMAL:
		case Types.DOUBLE:
		case Types.FLOAT:
		case Types.NUMERIC:
		case Types.REAL:
			return "decimal";

		case Types.INTEGER:
		case Types.BIGINT:
		case Types.SMALLINT:
		case Types.TINYINT:
			return "integer";

		case Types.DATE:
			return "date";
		case Types.TIMESTAMP:
			return "timestamp";

		default:
			return "text";
		}
	}

	private static String toFieldName(String dbName) {
		final String[] parts = dbName.split("_");
		String name = parts[0];
		for (int i = 1; i < parts.length; i++) {
			String part = parts[i];
			name += capitalise(part);
		}
		return name;
	}

	private static String toLabel(String dbName) {
		final String[] parts = dbName.split("_");
		String name = capitalise(parts[0]);
		for (int i = 1; i < parts.length; i++) {
			String part = parts[i];
			name += ' ' + capitalise(part);
		}
		return name;
	}

	private static String capitalise(String word) {
		if (word == null || word.length() == 0) {
			return "";
		}
		return word.toUpperCase().charAt(0) + word.substring(1);
	}

}
