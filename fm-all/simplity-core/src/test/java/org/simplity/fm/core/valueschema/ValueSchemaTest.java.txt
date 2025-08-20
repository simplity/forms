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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

/**
 * @author simplity.org
 *
 */

public class ValueSchemaTest {
	@SuppressWarnings("resource")
	protected ResultSet rs = Mockito.mock(ResultSet.class);
	@SuppressWarnings("resource")
	protected PreparedStatement ps = Mockito.mock(PreparedStatement.class);

	@Nested
	@DisplayName("Test ValueType.Boolean")
	class BooleanTest {
		@ParameterizedTest
		@ValueSource(strings = { "1", " true", "TRUE ", "  True ", "tRuE" })
		void shouldParseAsTrue(final String value) {
			assertEquals(true, ValueType.Boolean.parse(value));
		}

		@ParameterizedTest
		@ValueSource(strings = { "  0 ", "false", "FALSE  ", "False", "FaLSe" })
		void shouldParseAsFalse(final String value) {
			assertEquals(false, ValueType.Boolean.parse(value));
		}

		@ParameterizedTest
		@ValueSource(strings = { "", "a", "3", "Yes", "t", "f", "true1", "true true" })
		void shouldParseAsNull(final String value) {
			assertEquals(null, ValueType.Boolean.parse(value));
		}
	}

	@Nested
	@DisplayName("Test ValueType.Text")
	class TextTest {
		@ParameterizedTest
		@ValueSource(strings = { " 1", "true ", "TRUE ", "" })
		void shouldTrimStrings(final String value) {
			assertEquals(value.trim(), ValueType.Text.parse(value));
		}

	}

	@Nested
	@DisplayName("Test ValueType.Integer")
	class IntegerTest {
		@ParameterizedTest
		@ValueSource(strings = { "1a", "a1", "1+1", "1 1", "tRuE", " ", "1  a" })
		void shouldParseNonNumbersAsNull(final String value) {
			assertNull(ValueType.Integer.parse(value));
		}

		@Test
		void shouldParseUpTo19Digits() {
			assertEquals(1234567890123456789L, ValueType.Integer.parse("1234567890123456789"));
		}

		@ParameterizedTest
		@ValueSource(strings = { "12345678901234567890", "12345678901234567890123.345" })
		void shouldFailIfMoreThan19Digits(final String value) {
			assertNull(ValueType.Integer.parse(value));
		}

		@Test
		void shouldParseNumberStartingWith0() {
			assertEquals(1L, ValueType.Integer.parse("01"));
		}

		@Test
		void shouldParseNumbersStartingWithPlus() {
			assertEquals(1L, ValueType.Integer.parse("+1"));
		}

		@Test
		void shouldParseNumbersStartingWithMinus() {
			assertEquals(-1L, ValueType.Integer.parse("-1"));
		}

		@ParameterizedTest
		@ValueSource(strings = { "1.0", "1.49", "0.5" })
		void shouldParseDecimalsAsRounded(final String value) {
			assertEquals(1L, ValueType.Integer.parse(value));
		}

		@ParameterizedTest
		@ValueSource(strings = { "-1.0", "-1.5", "-0.6", "-.56" })
		void shouldParseNegativeDecimalsAsRounded(final String value) {
			assertEquals(-1L, ValueType.Integer.parse(value));
		}

	}

	@Nested
	@DisplayName("Test ValueType.Integer")
	class DecimlTest {
		@ParameterizedTest
		@ValueSource(strings = { "1a", "a1", "1+1", "1 1", "tRuE", " ", "1  a" })
		void shouldParseNonNumbersAsNull(final String value) {
			assertNull(ValueType.Decimal.parse(value));
		}

		@ParameterizedTest
		@ValueSource(strings = { "1", "1.1", "0.001", ".011", "12345678901234567890.1234" })
		void shouldParseValidDecimals(final String value) {
			final double d = Double.parseDouble(value);
			assertEquals(d, ValueType.Decimal.parse(value));
		}

	}

	@Nested
	@DisplayName("Test ValueType.Date")
	class DateTest {
		@ParameterizedTest
		@ValueSource(strings = { " 2011-11-12 ", " 2999-12-31" })
		void shouldParseVlidDates(final String value) {
			assertEquals(LocalDate.parse(value.trim()), ValueType.Date.parse(value));
		}

		@ParameterizedTest
		@ValueSource(strings = { "abcd", "20111-11-12", "2020-02-30", "2019-02-29" })
		void shouldReturnNullForInvalidDates(final String value) {
			assertNull(ValueType.Date.parse(value));
		}

	}

	@Nested
	@DisplayName("Test ValueType.Timestamp")
	class TimestampTest {
		@ParameterizedTest
		@ValueSource(strings = { " 2011-11-12T12:23:59Z", " 2020-02-29T12:23:59Z  " })
		void shouldParseValidStamps(final String value) {
			assertEquals(Instant.parse(value.trim()), ValueType.Timestamp.parse(value));
		}

		@ParameterizedTest
		@ValueSource(strings = { "abcd", "2011-11-12 12:23:59.12Z", "2011-11-12T12:23:59.12" })
		void shouldReturnNullForInvalidStamps(final String value) {
			assertNull(ValueType.Timestamp.parse(value));
		}

	}

}
