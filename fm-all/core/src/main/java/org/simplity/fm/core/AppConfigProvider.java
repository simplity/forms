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

package org.simplity.fm.core;

/**
 * Interface to launch the user defined App. This is picked-up by the frame-work
 * using ServiceLoader java utility
 *
 * client-app should have folder named "META-INF" in their jar files (generally
 * put in src/main/resources). THis folder contains a folder named "services". a
 * file named "org.simplity.fm.app.AppConfigProvider". This file should contain
 * one
 * line with the fully qualified class name that implements this interface. for
 * example <code>"com.myCompany.myApp.app.Bootstrapper"</code>
 *
 *
 * @author simplity.org
 *
 */
public interface AppConfigProvider {
	/**
	 *
	 * @return configuration details required to configure the App
	 */
	App.Config getConfig();
}