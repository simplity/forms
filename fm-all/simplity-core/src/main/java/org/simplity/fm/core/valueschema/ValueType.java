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

package org.simplity.fm.core.valueschema;

import java.time.Instant;
import java.time.LocalDate;

/**
 * text, number etc..
 *
 * @author simplity.org
 *
 */
public enum ValueType {
	/**
	 * text
	 */
	Text {
		@Override
		public String doParse(final String value) {
			return value;
		}

		@Override
		protected boolean typeOk(final Object value) {
			return true;
		}
	},
	/**
	 * whole number
	 */
	Integer {
		@Override
		public Long doParse(final String value) {
			try {
				return (Long.parseLong(value));
			} catch (final Exception e) {
				try {
					/*
					 * not long. is it decimal?
					 */
					final double d = Double.parseDouble(value);
					/*
					 * why was it not valid lng but a valid decimal?
					 */
					final int idx = value.indexOf('.');
					if (idx == -1) {
						/*
						 * it was a long that was longer than long!
						 */
						return null;
					}
					if (idx > 19) {
						/*
						 * valid decimal, but has more than 19 digits. cannot be
						 * accepted as long
						 */
						return null;
					}
					return Math.round(d);

				} catch (final Exception e1) {
					return null;
				}
			}
		}

		@Override
		protected boolean typeOk(final Object value) {
			return value instanceof Number;
		}
	},
	/**
	 * whole number
	 */
	Decimal {
		@Override
		public Double doParse(final String value) {
			try {
				return Double.parseDouble(value);
			} catch (final Exception e) {
				return null;
			}
		}

		@Override
		protected boolean typeOk(final Object value) {
			return value instanceof Number;
		}
	},
	/**
	 * boolean
	 */
	Boolean {
		@Override
		public Boolean doParse(final String value) {
			switch (value.toLowerCase()) {
			case "1" :
			case "true" :
				return true;
			case "0" :
			case "false" :
				return false;
			default :
				return null;
			}
		}

		@Override
		protected boolean typeOk(final Object value) {
			return value instanceof Boolean;
		}
	},
	/**
	 * Date as in calendar. No time, no time-zone. like a date-of-birth. Most
	 * commonly used value-type amongst the three types
	 */
	Date {
		@Override
		public LocalDate doParse(final String value) {
			try {
				return LocalDate.parse(value);
			} catch (final Exception e) {
				//
			}
			return null;
		}

		@Override
		protected boolean typeOk(final Object value) {
			return value instanceof LocalDate;
		}
	},

	/**
	 * an instant of time. will show up as different date/time .based on the
	 * locale. Likely candidate to represent most "date-time" fields
	 */
	Timestamp {
		@Override
		public Instant doParse(final String value) {
			try {
				return Instant.parse(value);
			} catch (final Exception e) {
				System.err.println(value + " is not a vlid instant");
			}
			return null;
		}

		@Override
		protected boolean typeOk(final Object value) {
			return value instanceof Instant;
		}
	};

	/**
	 * parse this value type from a string
	 *
	 * @param value
	 *            non-null to ensure that the caller can figure out whether the
	 *            parse failed or not.
	 *
	 * @return parsed value of this type. null if value the value could not be
	 *         parsed to the desired type
	 */
	public Object parse(final String value) {
		return this.doParse(value.trim());
	}

	protected abstract Object doParse(String value);

	/**
	 * @param value
	 *            can be null.
	 * @return true if the value is null, or is an instance of the right type.
	 *         false otherwise.
	 */
	public boolean isRighType(final Object value) {
		if (value == null) {
			return true;
		}
		return this.typeOk(value);
	}

	protected abstract boolean typeOk(Object value);
}
