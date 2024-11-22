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

package org.simplity.fm.core.infra.defalt;

import java.util.HashMap;
import java.util.Map;

import org.simplity.fm.core.Conventions;
import org.simplity.fm.core.IMessages;
import org.simplity.fm.core.IValueSchemas;
import org.simplity.fm.core.Message;
import org.simplity.fm.core.data.DbRecord;
import org.simplity.fm.core.data.Form;
import org.simplity.fm.core.data.IoType;
import org.simplity.fm.core.data.Record;
import org.simplity.fm.core.fn.Average;
import org.simplity.fm.core.fn.Concat;
import org.simplity.fm.core.fn.IFunction;
import org.simplity.fm.core.fn.Max;
import org.simplity.fm.core.fn.Min;
import org.simplity.fm.core.fn.Sum;
import org.simplity.fm.core.infra.ICompProvider;
import org.simplity.fm.core.service.IService;
import org.simplity.fm.core.service.IServiceContext;
import org.simplity.fm.core.service.ListService;
import org.simplity.fm.core.validn.IValueList;
import org.simplity.fm.core.valueschema.ValueSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uses ServiceLoader() to look for a provider. Uses a default empty provider
 * instead of throwing exception. That is, if no provider is available, all
 * requests will responded with null, after logging an error message.
 *
 * @author simplity.org
 *
 */

public class DefaultCompProvider implements ICompProvider {
	private static final Logger logger = LoggerFactory.getLogger(DefaultCompProvider.class);
	private static final char DOT = '.';
	private static final String RECORD = Conventions.App.RECORD_CLASS_SUFIX;
	private static final String FORM = Conventions.App.FORM_CLASS_SUFIX;

	private final IValueSchemas dataTypes;
	private final String formRoot;
	private final String recordRoot;
	private final String listRoot;
	private final String serviceRoot;
	private final String customListRoot;
	private final String fnRoot;
	private final IMessages messages;
	private final Map<String, Form<?>> forms = new HashMap<>();
	private final Map<String, Record> records = new HashMap<>();
	private final Map<String, IValueList> lists = new HashMap<>();
	private final Map<String, IService> services = new HashMap<>();
	private final Map<String, IFunction> functions = new HashMap<>();

	public DefaultCompProvider(final String rootPackageName) {
		final String root = rootPackageName + DOT;
		final String genRoot = root + Conventions.App.FOLDER_NAME_GEN + DOT;

		this.dataTypes = locateSchemas(genRoot);
		this.messages = locateMessages(genRoot);
		this.formRoot = genRoot + Conventions.App.FOLDER_NAME_FORM + DOT;
		this.recordRoot = genRoot + Conventions.App.FOLDER_NAME_RECORD + DOT;
		this.listRoot = genRoot + Conventions.App.FOLDER_NAME_LIST + DOT;
		this.customListRoot = root + Conventions.App.FOLDER_NAME_CUSTOM_LIST + DOT;
		this.serviceRoot = root + Conventions.App.FOLDER_NAME_SERVICE + DOT;
		this.fnRoot = root + Conventions.App.FOLDER_NAME_FN + DOT;
		/*
		 * add hard-wired services to the list
		 */
		this.services.put(Conventions.App.SERVICE_LIST, ListService.getInstance());
		/*
		 * add standard functions
		 */
		this.addStandardFuntions();
	}

	private static IValueSchemas locateSchemas(String genRoot) {
		String cls = genRoot + Conventions.App.GENERATED_VALUE_SCHEMAS_CLASS_NAME;
		try {
			return (IValueSchemas) Class.forName(cls).getConstructor().newInstance();
		} catch (final Exception e) {
			logger.error("Unable to locate class {}  as IDataTypes", cls);
			return null;
		}
	}

	private static IMessages locateMessages(String genRoot) {
		String cls = genRoot + Conventions.App.GENERATED_MESSAGES_CLASS_NAME;
		try {
			return (IMessages) Class.forName(cls).getConstructor().newInstance();
		} catch (final Exception e) {
			logger.warn("Unable to locate class {}  as IMessages. YOu will see only message ids, and not message texts",
					cls);
			return null;
		}
	}

	private void addStandardFuntions() {
		this.functions.put("concat", new Concat());
		this.functions.put("average", new Average());
		this.functions.put("sum", new Sum());
		this.functions.put("min", new Min());
		this.functions.put("max", new Max());
	}

	@Override
	public Form<?> getForm(final String formId) {
		Form<?> form = this.forms.get(formId);
		if (form == null) {
			form = this.loadForm(formId);
			if (form != null) {
				this.forms.put(formId, form);
			}
		}
		return form;
	}

	@Override
	public ValueSchema getValueSchema(final String dataTypeId) {
		return this.dataTypes.getValueSchema(dataTypeId);
	}

	@Override
	public IValueList getValueList(final String listId) {
		IValueList list = this.lists.get(listId);
		if (list != null) {
			return list;
		}
		final String clsName = toClassName(listId);
		String cls = this.listRoot + clsName;
		try {
			list = (IValueList) Class.forName(cls).getConstructor().newInstance();
			this.lists.put(listId, list);
			return list;
		} catch (final ClassNotFoundException e1) {
			// we will try a custom class instead
		} catch (final Exception e) {
			logger.error("Internal Error: List named " + listId
					+ " exists but an exception occurred while creating an instance of its associated class " + cls
					+ ". Error :", e);
			return null;
		}

		cls = this.customListRoot + clsName;
		try {
			list = (IValueList) Class.forName(cls).getConstructor().newInstance();
			this.lists.put(listId, list);
			return list;
		} catch (final ClassNotFoundException e1) {
			logger.error(
					"{} is an invalid list name because we could not locate class {} in generated package {} or custom package {}",
					listId, cls, this.listRoot, this.customListRoot);
			return null;
		} catch (final Exception e) {
			logger.error("Internal Error: Exception while instantiating class {}. Error :", cls, e);
			return null;
		}

	}

	@Override
	public Message getMessage(final String messageId) {
		return this.messages.getMessage(messageId);
	}

	@Override
	public IService getService(final String serviceId, final IServiceContext ctx) {
		IService service = this.services.get(serviceId);
		if (service != null) {
			return service;
		}
		/*
		 * we first check for a class. this approach allows us to over-ride standard
		 * formIO services
		 */
		final String cls = this.serviceRoot + toClassName(serviceId);
		try {
			service = (IService) Class.forName(cls).getConstructor().newInstance();
			this.services.put(serviceId, service);
		} catch (final Exception e) {
			/*
			 * it is not a class. Let us see if we can generate it. Also, form based
			 * services are not cached to simplify form overrides. This is not an issue
			 * because the for is anyways cached, and hence no disk access f\even if we do
			 * not cache the service
			 */
			service = this.tryFormIo(serviceId, ctx);
			if (service == null) {
				logger.error("Service {} is not served by this application", serviceId);
				return null;
			}
		}
		return service;
	}

	private IService tryFormIo(final String serviceName, final IServiceContext ctx) {
		final int idx = serviceName.indexOf(Conventions.Request.SERVICE_OPER_SEPARATOR);
		if (idx <= 0) {
			logger.info("Service name {} is not of the form operation_name. Service is not generated");
			return null;
		}

		final String OperationName = toClassName(serviceName.substring(0, idx));
		IoType opern = null;
		try {
			opern = IoType.valueOf(OperationName);
		} catch (final Exception e) {
			logger.warn(
					"Service name {} is of the form operation_name, but {} is not a valid operation. No service is generated",
					serviceName, OperationName);
			return null;
		}

		final String formName = serviceName.substring(idx + 1);
		logger.info("Looking to generate a service for operantion {} on {}", opern, formName);

		/*
		 * we provide flexibility for the service name to have Form suffix at the end,
		 * failing which we try the name itself as form
		 */
		if (formName.endsWith(FORM)) {
			final String fn = formName.substring(0, formName.length() - FORM.length());
			final Form<?> form = this.getForm(fn);
			if (form != null) {
				return form.getService(opern);
			}
		}

		final Form<?> form = this.getForm(formName, ctx);
		if (form != null) {
			return form.getService(opern);
		}

		/**
		 * it could be a record
		 */
		final Record rec = this.getRecord(formName, ctx);
		if (rec != null && rec instanceof DbRecord) {
			return ((DbRecord) rec).getService(opern, serviceName);
		}

		logger.info("{} is not a form or DbRecord and hence a service is not generated for oepration {}", formName,
				opern);
		return null;

	}

	@Override
	public IFunction getFunction(final String functionName) {
		IFunction fn = this.functions.get(functionName);
		if (fn != null) {
			return fn;
		}
		final String cls = this.fnRoot + toClassName(functionName);
		try {
			fn = (IFunction) Class.forName(cls).getConstructor().newInstance();
		} catch (final Exception e) {
			logger.error("No Function named {} because we could not locate class {}", functionName, cls);
			return null;
		}
		this.functions.put(functionName, fn);
		return fn;
	}

	private static String toClassName(final String name) {
		int idx = name.lastIndexOf('.');
		if (idx == -1) {
			return name.substring(0, 1).toUpperCase() + name.substring(1);
		}
		idx++;
		return name.substring(0, idx) + name.substring(idx, idx + 1).toUpperCase() + name.substring(idx + 1);
	}

	@Override
	public Record getRecord(final String recordName, final IServiceContext ctx) {
		final String id = ctx.getRecordOverrideId(recordName);
		if (id == null) {
			return this.getRecord(recordName);
		}

		/*
		 * record is cached with this id as prefix
		 */
		final String key = id + recordName;

		Record rec = this.records.get(key);
		if (rec != null) {
			return rec;
		}

		/**
		 * load and override it
		 */
		rec = this.loadRecord(recordName);
		if (rec != null) {
			rec.override(ctx);
			this.records.put(key, rec);
		}

		return rec;
	}

	private Record loadRecord(final String recordName) {
		final String cls = this.recordRoot + toClassName(recordName) + RECORD;
		try {
			return (Record) Class.forName(cls).getConstructor().newInstance();
		} catch (final ClassNotFoundException e) {
			logger.error("No record named {} because we could not locate class {}", recordName, cls);
			return null;
		} catch (final Exception e) {
			logger.error("Internal Error: record named" + recordName
					+ " exists but an excption occured while while creating an instance. Error :", e);
			return null;
		}
	}

	@Override
	public Record getRecord(final String recordName) {
		Record rec = this.records.get(recordName);
		if (rec == null) {
			rec = this.loadRecord(recordName);
			if (rec != null) {
				this.records.put(recordName, rec);
			}
		}
		return rec;
	}

	@Override
	public Form<?> getForm(final String formId, final IServiceContext ctx) {
		final String id = ctx.getFormOverrideId(formId);
		if (id == null) {
			return this.getForm(formId);
		}

		final String key = id + formId;
		Form<?> form = this.forms.get(key);
		if (form != null) {
			return form;
		}

		/**
		 * load and override it
		 */
		form = this.loadForm(formId);
		if (form != null) {
			form.override(ctx);
			this.forms.put(key, form);
		}

		return form;
	}

	private Form<?> loadForm(final String formId) {
		final String cls = this.formRoot + toClassName(formId) + FORM;
		try {
			return (Form<?>) Class.forName(cls).getConstructor().newInstance();
		} catch (final ClassNotFoundException e) {
			logger.error("No form named {} because we could not locate class {}", formId, cls);
			return null;
		} catch (final Exception e) {
			logger.error("Internal Error: Form named " + formId
					+ " exists but an exception occurred while creating an instance. Error :", e);
			return null;
		}
	}
}
