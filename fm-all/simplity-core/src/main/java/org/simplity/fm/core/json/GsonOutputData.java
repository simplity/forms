/*
 * Copyright (c) 2020 simplity.org
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

package org.simplity.fm.core.json;

import java.io.IOException;
import java.io.StringWriter;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;

import org.simplity.fm.core.ApplicationError;
import org.simplity.fm.core.service.IOutputData;

import com.google.gson.stream.JsonWriter;

/**
 * highly restrictive implementation that just about serves our purpose.
 *
 * @author simplity.org
 *
 */
@SuppressWarnings("resource")
class GsonOutputData implements IOutputData {
	private static final String NULL = "";
	private final JsonWriter writer;

	/**
	 *
	 * @param sw underlying string writer to which output json is written to
	 */
	GsonOutputData(final StringWriter sw) {
		this.writer = new JsonWriter(sw);
	}

	@Override
	public GsonOutputData beginObject() {
		try {
			this.writer.beginObject();
			return this;
		} catch (final IOException e) {
			throw new ApplicationError("", e);
		}
	}

	@Override
	public GsonOutputData endObject() {
		try {
			this.writer.endObject();
		} catch (final IOException e) {
			throw new ApplicationError("", e);
		}
		return this;
	}

	@Override
	public GsonOutputData beginArray() {
		try {
			this.writer.beginArray();
		} catch (final IOException e) {
			throw new ApplicationError("", e);
		}
		return this;
	}

	@Override
	public GsonOutputData endArray() {
		try {
			this.writer.endArray();
		} catch (final IOException e) {
			throw new ApplicationError("", e);
		}
		return this;
	}

	@Override
	public GsonOutputData addName(final String name) {
		try {
			this.writer.name(name);
		} catch (final IOException e) {
			throw new ApplicationError("", e);
		}
		return this;
	}

	@Override
	public GsonOutputData addValue(final String value) {
		try {
			this.writer.value(value);
		} catch (final IOException e) {
			throw new ApplicationError("", e);
		}
		return this;
	}

	@Override
	public GsonOutputData addValue(final long value) {
		try {
			this.writer.value(value);
		} catch (final IOException e) {
			throw new ApplicationError("", e);
		}
		return this;
	}

	@Override
	public GsonOutputData addValue(final boolean value) {
		try {
			this.writer.value(value);
		} catch (final IOException e) {
			throw new ApplicationError("", e);
		}
		return this;
	}

	@Override
	public GsonOutputData addValue(final double value) {
		try {
			this.writer.value(value);
		} catch (final IOException e) {
			throw new ApplicationError("", e);
		}
		return this;
	}

	@Override
	public GsonOutputData addValue(final LocalDate value) {
		/*
		 * we may have to add a specific formatter in the future
		 */
		try {
			this.writer.value(value.toString());
		} catch (final IOException e) {
			throw new ApplicationError("", e);
		}
		return this;
	}

	@Override
	public GsonOutputData addValue(final Instant value) {
		/*
		 * we may have to add a specific formatter in the future
		 */
		try {
			this.writer.value(value.toString());
		} catch (final IOException e) {
			throw new ApplicationError("", e);
		}
		return this;
	}

	@Override
	public GsonOutputData addPrimitive(final Object primitive) {
		try {
			if (primitive == null) {
				this.writer.value(NULL);
			} else if (primitive instanceof Boolean) {
				this.writer.value(((Boolean) primitive).booleanValue());
			} else if (primitive instanceof Number) {
				this.writer.value((Number) primitive);
				return this;
			} else {
				this.writer.value(primitive.toString());
			}
		} catch (final IOException e) {
			throw new ApplicationError("", e);
		}
		return this;
	}

	@Override
	public IOutputData addNameValuePair(String name, Object value) {
		this.addName(name).addPrimitive(value);
		return this;
	}

	@Override
	public IOutputData addValues(String[] names, Iterable<Object> values) {
		try {
			int idx = 0;
			for (Object value : values) {
				this.writer.name(names[idx]);
				this.addPrimitive(value);
				idx++;
			}
		} catch (final IOException e) {
			throw new ApplicationError("", e);
		}
		return this;
	}

	@Override
	public GsonOutputData addValues(final String[] names, final Object[] values) {
		this.addValues(names, Arrays.asList(values));
		return this;
	}

	@Override
	public GsonOutputData addArray(final String memberName, final String[] names, final Iterable<Object[]> rows) {
		try {
			this.writer.name(memberName);
			this.writer.beginArray();
			if (rows != null) {
				this.addArrayElements(names, rows);
			}
			this.writer.endArray();
		} catch (final IOException e) {
			throw new ApplicationError("", e);
		}
		return this;
	}

	@Override
	public GsonOutputData addArray(final String memberName, final String[] names, final Object[][] rows) {
		this.addArray(memberName, names, Arrays.asList(rows));
		return this;
	}

	@Override
	public GsonOutputData addArrayElements(final String[] names, final Iterable<Object[]> rows) {
		try {
			for (final Object[] row : rows) {
				this.writer.beginObject();
				this.addValues(names, row);
				this.writer.endObject();
			}
		} catch (final IOException e) {
			throw new ApplicationError("", e);
		}
		return this;
	}

	@Override
	public GsonOutputData addArrayElements(final String[] names, final Object[][] rows) {
		this.addArrayElements(names, Arrays.asList(rows));
		return this;
	}

	@Override
	public GsonOutputData addStringAsJson(String json) {
		try {
			this.writer.jsonValue(json);
		} catch (IOException e) {
			throw new ApplicationError("String is not a valid json: " + json);
		}
		return this;
	}

}
