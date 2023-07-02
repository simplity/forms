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

package org.simplity.fm.core.service;

/**
 * Simple implementation of IService, with default implementation of methods
 * except serve()
 * Service API is still in its initial stages, and hence is likely to be revised
 * in every new version.
 * Hence it is better to extend this class rather than implement IService.
 *
 * @author simplity.org
 *
 */
public abstract class AbstractService implements IService {
	protected String serviceName;

	protected AbstractService() {
		// allowing extended classes to set serviceName later
	}

	protected AbstractService(final String name) {
		this.serviceName = name;
	}

	@Override
	public String getId() {
		return this.serviceName;
	}

	@Override
	public boolean serveGuests() {
		return false;
	}

	@Override
	public boolean isAbortable() {
		return false;
	}

	@Override
	public boolean isAsynch() {
		return false;
	}

}
