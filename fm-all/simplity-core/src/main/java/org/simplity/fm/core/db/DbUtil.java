package org.simplity.fm.core.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDate;

import org.simplity.fm.core.ApplicationError;
import org.simplity.fm.core.Conventions;
import org.simplity.fm.core.data.Record;
import org.simplity.fm.core.valueschema.ValueType;

/**
 * static class with utilities for DB related functionalities
 *
 * @author simplity.org
 *
 */
public class DbUtil {

	/**
	 * set value for a parameter for a prepared statement
	 *
	 * @param ps
	 * @param position
	 *            1-based
	 * @param value
	 *            can be null
	 * @param valueType
	 *            not null can be null, in which case it is treated as text
	 * @throws SQLException
	 */
	public static final void setPsParamValue(final PreparedStatement ps,
			final int position, final Object value, final ValueType valueType)
			throws SQLException {

		switch (valueType) {
		case Boolean :
			if (value == null) {
				ps.setNull(position, Types.BOOLEAN);
			} else {
				ps.setBoolean(position, (boolean) value);
			}
			return;

		case Date :
			java.sql.Date date = null;
			if (value != null) {
				date = java.sql.Date.valueOf((LocalDate) value);
			}
			ps.setDate(position, date);
			return;

		case Decimal :
			if (value == null) {
				if (Conventions.Db.TREAT_NULL_AS_ZERO) {
					ps.setDouble(position, 0.0);
				} else {
					ps.setNull(position, Types.DECIMAL);
				}
			} else {
				ps.setDouble(position, (Double) value);
			}
			return;

		case Integer :
			if (value == null) {
				if (Conventions.Db.TREAT_NULL_AS_ZERO) {
					ps.setLong(position, 0L);
				} else {
					ps.setNull(position, Types.INTEGER);
				}
			} else {
				ps.setLong(position, (long) value);
			}
			return;

		case Text :
			ps.setString(position,
					value == null
							? Conventions.Db.TEXT_VALUE_OF_NULL
							: value.toString());
			return;

		case Timestamp :
			java.sql.Timestamp stamp = null;
			if (value != null) {
				stamp = java.sql.Timestamp.from((Instant) value);
			}
			ps.setTimestamp(position, stamp);
			return;

		default :
			throw new ApplicationError("ValueType " + valueType
					+ " is not handled in DB related operations");
		}

	}

	/**
	 * set values for all the parameters in a prepared statement
	 *
	 * @param ps
	 * @param values
	 *            has the right number of values for the ps
	 * @param valueTypes
	 *            one value-type for each of the values
	 * @throws SQLException
	 */
	public static final void setPsParamValues(final PreparedStatement ps,
			final Object[] values, final ValueType[] valueTypes)
			throws SQLException {
		for (int i = 0; i < values.length; i++) {
			setPsParamValue(ps, i + 1, values[i], valueTypes[i]);
		}
	}

	/**
	 * set values for all the parameters in a prepared statement
	 *
	 * @param ps
	 * @param record
	 *            which has the right values for the parameters in the PS
	 * @throws SQLException
	 */
	public static final void setPsParamValues(final PreparedStatement ps,
			Record record) throws SQLException {
		if (record == null) {
			return;
		}

		Object[] values = record.fetchRawData();
		ValueType[] valueTypes = record.fetchValueTypes();
		for (int i = 0; i < values.length; i++) {
			setPsParamValue(ps, i + 1, values[i], valueTypes[i]);
		}
	}

	/**
	 *
	 * @param rs
	 *            non-null
	 * @param position
	 *            1-based
	 * @param valueType
	 *            non-null
	 * @return retrieved object value. can be null
	 * @throws SQLException
	 */
	public static final Object getValueFromRs(final ResultSet rs,
			final int position, ValueType valueType) throws SQLException {
		switch (valueType) {
		case Boolean :
			final boolean bool = rs.getBoolean(position);
			if (rs.wasNull()) {
				return null;
			}
			return bool;

		case Date :
			final java.sql.Date date = rs.getDate(position);
			if (date == null) {
				return null;
			}
			return date.toLocalDate();

		case Decimal :
			final double dbl = rs.getDouble(position);
			if (rs.wasNull()) {
				if (Conventions.Db.TREAT_NULL_AS_ZERO) {
					return 0.0;
				}
				return null;
			}
			return dbl;

		case Integer :
			final long nbr = rs.getLong(position);
			if (rs.wasNull()) {
				if (Conventions.Db.TREAT_NULL_AS_ZERO) {
					return 0L;
				}
				return null;
			}
			return nbr;

		case Text :
			String text = rs.getString(position);
			if (text == null) {
				text = Conventions.Db.TEXT_VALUE_OF_NULL;
			}
			return text;

		case Timestamp :
			final java.sql.Timestamp stamp = rs.getTimestamp(position);
			if (stamp == null) {
				return null;
			}
			return stamp.toInstant();

		default :
			throw new ApplicationError("ValueType " + valueType
					+ " is not handled in DB related operations");
		}
	}

	/**
	 *
	 * @param rs
	 *            non-null
	 * @param valueTypes
	 *            non-null
	 * @return retrieved object values.
	 * @throws SQLException
	 */
	public static final Object[] getValuesFromRs(final ResultSet rs,
			ValueType[] valueTypes) throws SQLException {
		Object[] values = new Object[valueTypes.length];
		for (int i = 0; i < values.length; i++) {
			values[i] = getValueFromRs(rs, i + 1, valueTypes[i]);
		}
		return values;
	}

	/**
	 *
	 * @param rs
	 *            non-null
	 * @param record
	 *            to which data is to be extracted based on the value types of
	 *            the fields non-null
	 * @throws SQLException
	 */
	public static final void rsToRecord(final ResultSet rs, Record record)
			throws SQLException {
		ValueType[] valueTypes = record.fetchValueTypes();
		for (int i = 0; i < valueTypes.length; i++) {
			record.assignValue(i, getValueFromRs(rs, i + 1, valueTypes[i]));
		}
	}
}
