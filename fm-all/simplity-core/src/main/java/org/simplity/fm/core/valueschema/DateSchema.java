// SPDX-License-Identifier: MIT
package org.simplity.fm.core.valueschema;

import java.time.LocalDate;

/**
 * validation parameters for a an integral value
 *
 * @author simplity.org
 *
 */
public class DateSchema extends ValueSchema {
	private final int maxPastDays;
	private final int maxFutureDays;

	/**
	 * @param name
	 * @param messageId
	 *
	 * @param maxPastDays
	 *            0 means today is OK. 100 means 100 days before today is the
	 *            min, -100 means 100 days after today is the min
	 * @param maxFutureDays
	 *            0 means today is OK. -100 means 100 days before today is the
	 *            max. 100 means 100 days after today is the max
	 */
	public DateSchema(final String name, final String messageId, final int maxPastDays, final int maxFutureDays) {
		this.valueType = ValueType.Date;
		this.name = name;
		this.messageId = messageId;
		this.maxPastDays = maxPastDays;
		this.maxFutureDays = maxFutureDays;
	}

	@Override
	public LocalDate parse(final String text) {
		try {
			if (text.length() >= 10) {
				return this.validate(LocalDate.parse(text.substring(0, 10)));
			}

			return this.validate(LocalDate.ofEpochDay(Long.parseLong(text)));

		} catch (final Exception e) {
			return null;
		}

	}

	@Override
	public LocalDate parse(final Object object) {
		if (object instanceof LocalDate) {
			return this.validate((LocalDate) object);
		}

		if (object instanceof String) {
			return this.parse((String) object);
		}
		return null;
	}

	private LocalDate validate(final LocalDate date) {
		final LocalDate today = LocalDate.now();
		if (today.plusDays(-this.maxPastDays).isAfter(date)) {
			return null;
		}
		if (today.plusDays(this.maxFutureDays).isBefore(date)) {
			return null;
		}
		return date;
	}
}
