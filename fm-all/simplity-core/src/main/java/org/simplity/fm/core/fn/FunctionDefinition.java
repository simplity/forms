// SPDX-License-Identifier: MIT
package org.simplity.fm.core.fn;

import org.simplity.fm.core.service.ServiceContext;
import org.simplity.fm.core.valueschema.ValueType;

/**
 * generic functions that can be called by utility functions in a generic way.
 * We strongly recommend that the implementations provide type-specific methods
 * for use by hand-written code.
 *
 * For example averageOf2Of3 function should provide an API double
 * calculate(doubl1 p1, double p2, double p3). While custom code can use this
 * API, generic utility code can continue to use the evaluateAPI.
 *
 *
 * @author simplity.org
 *
 */
public interface FunctionDefinition {
	/**
	 * evaluate this function with string as arguments. String arguments are
	 * parsed into the right type. Any parse error is added to the ctx. If the
	 * ctx is null, then an exception is thrown, with the assumption that it is
	 * being called inside a server-only context
	 *
	 * @param ctx
	 *            service context. Can be null in case this is executed outside
	 *            of a service context. implementations must take care of this
	 *
	 * @param params
	 *            must have the right type of values for the function
	 * @return result, possibly null; only primitive value/object are expected.
	 */
	Object parseAndEval(ServiceContext ctx, String... params);

	/**
	 * evaluate this function
	 *
	 * @param params
	 *            must have the right type of values for the function
	 * @return result, possibly null; only primitive value/object are expected.
	 */
	Object eval(Object... params);

	/**
	 * meta data about the parameters. Can be used by the caller before calling
	 * to validate input data
	 *
	 * @return array of value types for each parameter. has only one element if
	 *         variable arguments used. null if no arguments are expected
	 */
	ValueType[] getArgumentTypes();

	/**
	 * meta data about the parameters. Can be used by the caller before calling
	 * to validate input data
	 *
	 * @return array of value types for each parameter. Note that the function
	 *         would receive string and parse them into these types.
	 */
	ValueType getReturnType();

	/**
	 *
	 * @return -1 if it accepts var-args. 0 if no parameters are expected.
	 */
	int getNbrArguments();

	/**
	 *
	 * @return true if this function accepts var-args as its sole parameter.
	 *         false otherwise. Note that the function can not be defined to
	 *         take its last argument as var-args
	 */
	boolean acceptsVarArgs();
}
