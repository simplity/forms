package org.simplity.fm.core.validn;

import java.time.Instant;
import java.time.LocalDate;

import org.simplity.fm.core.ApplicationError;
import org.simplity.fm.core.data.Record;
import org.simplity.fm.core.service.IServiceContext;
import org.simplity.fm.core.valueschema.ValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * validation of values for a pair of fields
 */
public class InterFieldValidation implements IValidation {

	private static final Logger logger = LoggerFactory.getLogger(InterFieldValidation.class);
	protected final int index1;
	protected final int index2;
	protected final String fieldName;
	protected final String messageId;
	protected final InterFieldValidationType validationType;
	/**
	 * validation is relevant only if the field1 has this specific value. null if no
	 * such constraint
	 */
	protected final String field1ValueToMatch;

	public InterFieldValidation(int index1, int index2, String fieldName, String messageId, String field1Value,
			InterFieldValidationType validationType) {
		this.index1 = index1;
		this.index2 = index2;
		this.fieldName = fieldName;
		this.messageId = messageId;
		this.field1ValueToMatch = field1Value;
		this.validationType = validationType;
	}

	@Override
	public boolean isValid(Record record, IServiceContext ctx) {
		final Object v1 = record.fetchValue(this.index1);
		final Object v2 = record.fetchValue(this.index2);
		final ValueType vt = record.fetchValueTypes()[this.index1];
		final boolean v1Exists = isSpecified(v1, vt);
		final boolean v2Exists = isSpecified(v2, vt);

		if (this.field1ValueToMatch != null) {
			/*
			 * is this rule applicable?
			 */
			if (this.field1ValueToMatch.equals(v1) == false) {
				return true;
			}
		}

		switch (this.validationType) {
		case BothOrNone:
			if (v1Exists) {
				/*
				 * val2 must be specified
				 */
				return v2Exists;
			}
			/*
			 * val2 should be skipped
			 */
			return !v2Exists;

		case BothOrSecond:
			if (v1Exists) {
				/*
				 * val2 must be specified
				 */
				return v2Exists;
			}
			/**
			 * val2 has no restrictions
			 */
			return true;

		case OneOf:

			if (v1Exists) {
				/*
				 * val2 should be skipped
				 */
				return !v2Exists;
			}

			/*
			 * val2 is required
			 */
			return v2Exists;

		case Different:
			if (v1Exists) {
				return !v1.equals(v2);
			}
			/*
			 * null can not be compared with null!!
			 */
			return false;

		case Equal:
			if (v1Exists) {
				return v1.equals(v2);
			}
			/*
			 * null can not be compared with null!!
			 */
			return false;

		case Range:
			return rangeOk(v1, v2, false, vt);

		case RangeOrEqual:
			return rangeOk(v1, v2, true, vt);

		default:
			throw new ApplicationError("Inter-field Validation " + this.validationType.name() + " is not handled ");

		}

	}

	private boolean rangeOk(Object value1, Object value2, boolean equalOk, ValueType vt) {
		if (value1 == null || value2 == null) {
			return false;
		}
		try {
			switch (vt) {
			case Boolean:
				if (equalOk) {
					return value1.equals(value1);
				}
				logger.error("Field " + this.getFieldName()
						+ " is a boolean, but it has an inter-field validation for from-to (range). This validation will ALWAYS fail");
				return false;
			case Date:
				return dateOk((LocalDate) value1, (LocalDate) value2, equalOk);

			case Decimal:
				return doubleOk((Double) value1, (Double) value2, equalOk);

			case Integer:
				return longOk((Long) value1, (Long) value2, equalOk);

			case Text:
				return textOk((String) value1, (String) value2, equalOk);

			case Timestamp:
				return timestampOk((Instant) value1, (Instant) value2, equalOk);

			default:
				throw new ApplicationError("ValueType " + vt.name() + " not handled in inter-field validation");
			}
		} catch (ClassCastException e) {
			throw new ApplicationError("Field " + this.getFieldName() + " is of value type " + vt.name()
					+ " and has a inter-field validation for range. However the actual value of field1 is "
					+ value1.getClass().getName() + " and field2 is " + value2.getClass().getName());
		}
	}

	/**
	 * @param fm
	 * @param to
	 * @return
	 */
	private static boolean timestampOk(final Instant fm, final Instant to, boolean equalOk) {
		if (equalOk) {
			return !fm.isAfter(to);
		}
		return to.isAfter(fm);
	}

	private static boolean longOk(final long fm, final long to, boolean equalOk) {
		if (equalOk) {
			return to >= fm;
		}
		return to > fm;
	}

	private static boolean doubleOk(final double fm, final double to, boolean equalOk) {
		if (equalOk) {
			return to >= fm;
		}
		return to > fm;
	}

	private static boolean dateOk(final LocalDate fm, final LocalDate to, boolean equalOk) {
		if (equalOk) {
			return !fm.isAfter(to);
		}
		return to.isAfter(fm);
	}

	private static boolean textOk(final String fm, final String to, boolean equalOk) {
		final int n = to.compareToIgnoreCase(fm);
		if (equalOk) {
			return n >= 0;
		}
		return n > 0;
	}

	private static boolean isSpecified(Object val, ValueType vt) {
		if (val == null) {
			return false;
		}

		final String text = val.toString();
		if (text.isEmpty()) {
			return false;
		}

		if (vt != ValueType.Integer && vt != ValueType.Decimal) {
			return true;
		}
		return ((Number) val).intValue() != 0;
	}

	@Override
	public String getFieldName() {
		return this.fieldName;
	}
}
