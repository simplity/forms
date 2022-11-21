package org.simplity.fm.core.json;

import java.time.Instant;
import java.time.LocalDate;

import org.simplity.fm.core.service.IInputArray;
import org.simplity.fm.core.service.IInputData;
import org.simplity.fm.core.service.INullableValue;
import org.simplity.fm.core.service.InputValueType;

class NullableValue implements INullableValue {
	private static final String NULL_STRING = "";
	private static final long NULL_INTEGER = 0;
	private static final double NULL_DECIMAL = 0;
	private static final boolean NULL_BOOLEAN = false;
	private static final LocalDate NULL_DATE = LocalDate.ofEpochDay(0);
	private static final Instant NULL_TIMESTAMP = Instant.ofEpochMilli(0);

	private final InputValueType valueType;
	private final Object value;

	NullableValue() {
		this.value = null;
		this.valueType = InputValueType.NoData;
	}

	NullableValue(String value) {
		if (value == null) {
			this.value = null;
			this.valueType = null;
			return;
		}
		this.valueType = InputValueType.Text;
		this.value = value;
	}

	NullableValue(Instant value) {
		if (value == null) {
			this.value = null;
			this.valueType = null;
			return;
		}
		this.valueType = InputValueType.Timestamp;
		this.value = value;
	}

	NullableValue(LocalDate value) {
		if (value == null) {
			this.value = null;
			this.valueType = null;
			return;
		}
		this.valueType = InputValueType.Date;
		this.value = value;
	}

	NullableValue(boolean value) {
		this.valueType = InputValueType.Boolean;
		this.value = value;
	}

	NullableValue(long value) {
		this.valueType = InputValueType.Integer;
		this.value = value;
	}

	NullableValue(double value) {
		this.valueType = InputValueType.Decimal;
		this.value = value;
	}

	NullableValue(IInputArray value) {
		this.valueType = InputValueType.Array;
		this.value = value;
	}

	NullableValue(IInputData value) {
		this.valueType = InputValueType.Data;
		this.value = value;
	}

	@Override
	public boolean isNull() {
		return this.valueType == null;
	}

	@Override
	public InputValueType getValueType() {
		return this.valueType;
	}

	@Override
	public Object getValue() {
		return this.value;
	}

	@Override
	public long getInteger() {
		if (this.valueType == InputValueType.Integer
				|| this.valueType == InputValueType.Decimal) {
			return ((Number) this.value).longValue();
		}
		return NULL_INTEGER;
	}

	@Override
	public boolean getBoolean() {
		if (this.valueType == InputValueType.Boolean) {
			return (Boolean) this.value;
		}
		return NULL_BOOLEAN;
	}

	@Override
	public String getString() {
		if (this.value == null) {
			return NULL_STRING;
		}
		return this.value.toString();
	}

	@Override
	public double getDecimal() {
		if (this.valueType == InputValueType.Integer
				|| this.valueType == InputValueType.Decimal) {
			return ((Number) this.value).doubleValue();
		}
		return NULL_DECIMAL;
	}

	@Override
	public LocalDate getDate() {
		if (this.valueType == InputValueType.Timestamp) {
			return (LocalDate) this.value;
		}
		return NULL_DATE;
	}

	@Override
	public Instant getTimestamp() {
		if (this.valueType == InputValueType.Timestamp) {
			return (Instant) this.value;
		}
		return NULL_TIMESTAMP;
	}

	@Override
	public IInputArray getArray() {
		if (this.valueType == InputValueType.Array) {
			return (IInputArray) this.value;
		}
		return JsonUtil.newInputArray();
	}

	@Override
	public IInputData getData() {
		if (this.valueType == InputValueType.Array) {
			return (IInputData) this.value;
		}
		return JsonUtil.newInputData();
	}

	@Override
	public String toString() {
		return this.getString();
	}
}
