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
 * one place where all constants across layers are defined
 *
 * @author simplity.org
 *
 */
public class Conventions {
	/**
	 * HTTP related
	 */
	public static class Http {

		/**
		 * header name to specify the service name
		 */
		public static final String HEADER_SERVICE = "_s";
		/**
		 * header name with which token is sent
		 */
		public static final String HEADER_AUTH = "Authorization";
		/**
		 * various headers that we respond back with
		 */
		public static final String[] HDR_NAMES = { "Access-Control-Allow-Methods", "Access-Control-Allow-Headers",
				"Access-Control-Max-Age", "Connection", "Cache-Control", "Expires", "Accept" };
		/**
		 * values for the headers
		 */
		public static final String[] HDR_TEXTS = { "POST, GET, OPTIONS",
				"content-type, " + HEADER_SERVICE + ", " + HEADER_AUTH, "1728", "Keep-Alive",
				"no-cache, no-store, must-revalidate", "11111110", "application/json" };
		/**
		 * how to get the IP address of the client-request? There is no guaranteed way,
		 * but we will look for it in this order
		 */
		public static final String[] HDR_NAMES_FOR_IP = { "X-Real-IP", "X-Forwarded-For", "Remote-Addr" };

		/**
		 * name in the context for user-IP address
		 */
		public static final String CLIENT_IP_FIELD_NAME = "_clientIp";
		/**
		 * name in the context for sessionId
		 */
		public static final String SESSION_ID_FIELD_NAME = "sessionId";
		/**
		 * all Ok
		 */
		public static final int STATUS_ALL_OK = 200;
		/**
		 * input data is malformed
		 */
		public static final int STATUS_INVALID_DATA = 400;
		/**
		 * user has to be authenticated for this request
		 */
		public static final int STATUS_AUTH_REQUIRED = 401;
		/**
		 * requested service not served by this server
		 */
		public static final int STATUS_INVALID_SERVICE = 406;
		/**
		 * this method is not allowed
		 */
		public static final int STATUS_METHOD_NOT_ALLOWED = 405;
		/**
		 * server had an internal error
		 */
		public static final int STATUS_INTERNAL_ERROR = 500;

		/**
		 * service has generated an error. We do not have the exact code. 409 is the
		 * closest!!
		 */
		public static final int STATUS_SERVICE_FAILED = 409;
	}

	/**
	 *
	 * COnventions for request-response paradigm
	 *
	 */
	public static class Request {
		/**
		 * tag/name of form data in the request/response pay load
		 */
		public static final String TAG_DATA = "data";
		/**
		 * tag/field name in response payload that is set to true/false
		 */
		public static final String TAG_ALL_OK = "allOk";

		/**
		 * tag/field name in response for the auth token
		 */
		public static final String TAG_TOKEN = "token";
		/**
		 * tag/field name in response payload that has an array of messages.
		 */
		public static final String TAG_MESSAGES = "messages";
		/**
		 * tag/attribute/field name in the payload for a list of rows being
		 * sent/returned
		 */
		public static final String TAG_LIST = "list";
		/**
		 * tag/attribute/field name of key for a keyed-list a keyed-list
		 */
		public static final String TAG_KEY = "key";
		/**
		 * tag/attribute/field name in the payload for all lists for all keys in a
		 * keyed-list
		 */
		public static final String TAG_LISTS = "lists";
		/**
		 * tag/attribute/field name of input data to request all lists for all possible
		 * keys in a keyed-list
		 */
		public static final String TAG_ALL_KEYS = "forAllKeys";

		/**
		 * field name of value in a list entry. e.g. {"value": 23, "text": "Some
		 * District Name"
		 */
		public static final String TAG_LIST_ENTRY_VALUE = "value";
		/**
		 * field name of text in a list entry. e.g. {"value": 23, "text": "Some District
		 * Name"
		 */
		public static final String TAG_LIST_ENTRY_TEXT = "text";
		/**
		 * number of rows of data (expected or delivered)
		 */
		public static final String TAG_MAX_ROWS = "maxRows";
		/**
		 * filter conditions
		 */
		public static final String TAG_CONDITIONS = "conditions";

		/**
		 * filter sort order. "sort" : {"field1":"asc/desc", "field2"...}
		 */
		public static final String TAG_SORT = "sort";
		/**
		 * field/tag name for filter condition
		 */
		public static final String TAG_FILTER_COMP = "comp";
		/**
		 * field/tag name for filter value
		 */
		public static final String TAG_FILTER_VALUE = "value";
		/**
		 * field/tag name for filter to-value in case the comparator is between
		 */
		public static final String TAG_FILTER_VALUE_TO = "toValue";
		/**
		 * default MAX nbr rows
		 */
		public static final int DEFAULT_NBR_ROWS = 500;

		/**
		 * formName + sep + operation is treated as a service name
		 */
		public static final char SERVICE_OPER_SEPARATOR = '_';

	}

	/**
	 * comparators, typically used in expressions and row selection criteria
	 *
	 * @author simplity.org
	 *
	 */
	public static class Filter {
		/** */
		public static final String EQ = "=";
		/** */
		public static final String NE = "!=";
		/** */
		public static final String LT = "<";
		/** */
		public static final String LE = "<=";
		/** */
		public static final String GT = ">";
		/** */
		public static final String GE = ">=";
		/** */
		public static final String CONTAINS = "~";
		/** */
		public static final String STARTS_WITH = "^";
		/** */
		public static final String BETWEEN = "><";

		/** one of the entries in a list */
		public static final String IN_LIST = "@";
	}

	/**
	 *
	 */
	public static class App {
		/**
		 * file name that has he application level components
		 */
		public static final String APP_FILE = "application.json";
		/**
		 * file that has the actual text for message ids
		 */
		public static final String MESSAGES_FILE = "messages.json";
		/**
		 * file that has the value lists for this app
		 */
		public static final String LISTS_FILE = "valueLists.json";
		/**
		 * file that has the actual text for message ids
		 */
		public static final String VALUE_SCHEMAS_FILE = "valueSchemas.json";
		/**
		 * folder name (sun-package) under which all sources are generated
		 */
		public static final String FOLDER_NAME_GEN = "gen";

		/**
		 * all data types defined in the app are put into this generated class. This is
		 * generated by the generator and placed in the root of generated package
		 */
		public static final String GENERATED_VALUE_SCHEMAS_CLASS_NAME = "DefinedValueSchemas";
		/**
		 * all data types defined in the app are put into this generated class. This is
		 * generated by the generator and placed in the gen root package
		 */
		public static final String GENERATED_MESSAGES_CLASS_NAME = "DefinedMessages";

		/**
		 * folder name under which services are defined
		 */
		public static final String FOLDER_NAME_SERVICE = "service";
		/**
		 * folder name under which functions are defined
		 */
		public static final String FOLDER_NAME_FN = "fn";
		/**
		 * folder name under which sqls are defined
		 */
		public static final String FOLDER_NAME_SQL = "sql";
		/**
		 * folder name under which classes related to list are generated
		 */
		public static final String FOLDER_NAME_LIST = "list";

		/**
		 * folder name under root package for custom classes to serve as runtime list
		 */
		public static final String FOLDER_NAME_CUSTOM_LIST = "clist";
		/**
		 * folder name under which form classes are generated
		 */
		public static final String FOLDER_NAME_FORM = "form";

		/**
		 * folder name under which records are generated
		 */
		public static final String FOLDER_NAME_RECORD = "rec";
		/**
		 * folder name under which pages are generated
		 */
		public static final String FOLDER_NAME_PAGE = "page";
		/**
		 * generated form classes are named with this suffix. e.g. instituteForm
		 */
		public static final String FORM_CLASS_SUFIX = "Form";
		/**
		 * generated sql classes are named with this suffix. e.g. instituteForm
		 */
		public static final String SQL_CLASS_SUFIX = "Sql";
		/**
		 * generated record classes are named with this suffix. e.g. instituteRecord
		 */
		public static final String RECORD_CLASS_SUFIX = "Record";

		/**
		 * file extension for record metadata
		 */
		public static final String EXTENSION_RECORD = ".rec.json";
		/**
		 * file extension for form metadata
		 */
		public static final String EXTENSION_FORM = ".form.json";
		/**
		 * file extension for sql metadata
		 */
		public static final String EXTENSION_SQL = ".sql.json";

		/**
		 * predefined name for list service
		 */
		public static final String SERVICE_LIST = "_getList";

		/**
		 * predefined name for the service to get the output of an asynch-service
		 * requested earlier
		 */
		public static final String SERVICE_GET_RESPONSE = "_getResponse";
		/**
		 * name with which the user data is saved in the context
		 */
		public static final String USER_IN_CTX = "_user";
		/**
		 * name of record to be used to get user details for creating context
		 */
		public static final String USER_RECORD_NAME = "user";

	}

	/**
	 * names used in batch upload
	 *
	 */
	public static class Upload {
		/**
		 * list of name-value pairs. Note that he value may come from some
		 * configuration. Hence this is used instead of just constants
		 */
		public static final String TAG_PARAMS = "params";
		/**
		 * look-ups get the internal value for a text/name used by end-user. This is the
		 * reverse of drop-down
		 */
		public static final String TAG_LOOKUPS = "lookups";
		/**
		 * list of function used
		 */
		public static final String TAG_FUNCTIONS = "functions";
		/**
		 * forms to be used for inserting row
		 */
		public static final String TAG_INSERTS = "inserts";
		/**
		 * one specific form
		 */
		public static final String TAG_FORM = "form";
		/**
		 * if set, and if the form generates a key, the generated key is saved in the
		 * value list with this value.
		 */
		public static final String TAG_GENERATED_KEY = "generatedKeyOutputName";
		/**
		 * list of fields from this form.
		 */
		public static final String TAG_FIELDS = "fields";
		/**
		 * indicates that this is set to a constant value
		 */
		public static final char TYPE_CONST = '=';
		/**
		 * indicates that this is a variable name whose value is found in the variable
		 * list. Note that $a..$z,$aa etc.. are the columns from the spread sheet
		 */
		public static final char TYPE_VAR = '$';
		/**
		 * indicates that this is a parameter that is set for this uploader
		 */
		public static final char TYPE_PARAM = '@';
		/**
		 * indicates that the field value is to be looked-up from this look-up list that
		 * is already specified at the top.
		 */
		public static final char TYPE_LOOKUP = '#';
		/**
		 * indicates that this is a function name
		 */
		public static final char TYPE_FN = '%';
		/**
		 * in a keyed list, values are flattened for lookup by indexing them with key +
		 * '|' + text
		 */
		public static final char KEY_TEXT_SEPARATOR = '|';
	}

	/**
	 * database related.
	 *
	 *
	 */
	public class Db {
		/**
		 * We strongly recommend treating null in db as empty string in the programming
		 * world, there by reducing possible null-pointer exceptions.
		 */
		public static final String TEXT_VALUE_OF_NULL = "";
		/**
		 * this is app-specific. BUt we do not intend to push it there till we have
		 * compelling use case to do so.
		 */
		public static final boolean TREAT_NULL_AS_ZERO = true;
	}
}
