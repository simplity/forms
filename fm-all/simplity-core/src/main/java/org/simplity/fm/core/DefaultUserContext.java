// SPDX-License-Identifier: MIT
package org.simplity.fm.core;

import java.io.Reader;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.simplity.fm.core.data.OverrideUtil;
import org.simplity.fm.core.data.OverrideUtil.Overrides;
import org.simplity.fm.core.data.RecordOverride;

/**
 * data that is to be cached for a logged-in user that is used across service
 * requests. This is a base class that the actual Apps extend to make this
 * useful
 *
 * @author simplity.org
 *
 */
public class DefaultUserContext implements UserContext {
	protected final Map<String, Object> values = new HashMap<>();

	/**
	 * userId for whom this context is created. not-null non-empty. If this is for a
	 * guest, the proposed design is that the app has the concept of a guest.
	 */
	protected final long userId;

	/**
	 * tenant id, if the app has such a concept
	 */
	protected Object tenantId;

	/**
	 * if form/records are overridden for this
	 */
	protected String overrideId;
	/**
	 * name of forms that are overridden in this context
	 */
	protected Set<String> formOverrides;
	/**
	 * name of records that are overridden in this context
	 */
	protected Set<String> recordOverrides;
	/**
	 * pending jobs that the user has asked for, but not delivered yet
	 */
	protected Set<String> jobs;

	/**
	 *
	 * @param userId
	 */
	public DefaultUserContext(final long userId) {
		this.userId = userId;
	}

	/**
	 *
	 * @return the ID of the user to whom this session belongs to
	 */
	@Override
	public long getUserId() {
		return this.userId;
	}

	/**
	 *
	 * @return null if this app is not multi-tenant, or if it is not set
	 */
	public Object getTenantId() {
		return this.tenantId;
	}

	/**
	 *
	 * @param id non-null, the right type, typically long
	 */
	public void setTenantId(Object id) {
		this.tenantId = id;
	}

	/**
	 * to be invoked by the extended class to cache the form/record overrides
	 */
	protected void setOverrides(final String id) {
		final Overrides overs = OverrideUtil.getOverides(id);
		if (overs == null) {
			return;
		}

		this.overrideId = id;
		this.formOverrides = new HashSet<>();
		Collections.addAll(this.formOverrides, overs.forms);

		this.recordOverrides = new HashSet<>();
		Collections.addAll(this.recordOverrides, overs.records);
	}

	/**
	 *
	 * @param recordName
	 * @return null if this is not if overridden in the current context. overrideId
	 *         if present.
	 *
	 */
	@Override
	public String getRecordOverrideId(final String recordName) {
		if (this.recordOverrides != null && this.recordOverrides.contains(recordName)) {
			return this.overrideId;
		}
		return null;
	}

	/**
	 *
	 * @param formName
	 * @return id with which this form is overridden. null if it is not overridden.
	 */
	@Override
	public String getFormOverrideId(final String formName) {
		if (this.formOverrides != null && this.formOverrides.contains(formName)) {
			return this.overrideId;
		}
		return null;
	}

	/**
	 * get the record override for this record in this context
	 *
	 * @param recordName
	 * @return record override, or null if it is not found
	 */
	@Override
	public RecordOverride getRecordOverride(final String recordName) {
		return OverrideUtil.getRecord(this.overrideId, recordName);
	}

	/**
	 * save a jobId in the context
	 *
	 * @param jobId must be a valid jobId returned by the JobManager
	 */
	@Override
	public void addJob(final String jobId) {
		if (this.jobs == null) {
			this.jobs = new HashSet<>();
		}
		this.jobs.add(jobId);
	}

	/**
	 * remove the jobId from this list once it is taken care of..
	 *
	 * @param jobId
	 */
	@Override
	public void removeJob(final String jobId) {
		if (this.jobs != null) {
			this.jobs.remove(jobId);
		}
	}

	@Override
	public Object getValue(String key) {
		return this.values.get(key);
	}

	@Override
	public void setValue(String key, Object value) {
		if (value == null) {
			this.values.remove(key);
		} else {
			this.values.put(key, value);
		}

	}

	@Override
	public void persist(Writer writer) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean load(Reader reader) {
		// TODO Auto-generated method stub
		return false;
	}
}
