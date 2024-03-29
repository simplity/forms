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

package org.simplity.fm.core.infra;

import org.simplity.fm.core.UserContext;
import org.simplity.fm.core.service.IOutputData;
import org.simplity.fm.core.service.IServiceContext;

/**
 * interface for client-code to create a custom IServiceContext or use the
 * default one provided by the framework
 *
 * @author simplity.org
 *
 */
public interface IServiceContextFactory {

	/**
	 * create a service context for the logged-in user
	 *
	 * @param userSession
	 *            non-null
	 * @param outData
	 *            non-null
	 * @return non-null instance of IServiceCOntext that will be passed to the
	 *         service execution thread.
	 */
	IServiceContext newContext(UserContext userSession, IOutputData outData);

	/**
	 * create a service context for the logged-in user
	 *
	 * @param outData
	 *            non-null
	 *
	 * @param userSession
	 *            non-null
	 * @return non-null instance of IServiceCOntext that wil be passed to the
	 *         service execution thread.
	 */
	IServiceContext newSessionLessContext(IOutputData outData);

}
