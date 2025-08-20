// SPDX-License-Identifier: MIT
package org.simplity.fm.core.data;

import java.util.Map;

/**
 * tenant specific over-rides for a record. This feature provides flexibility to
 * change basic field-level validations by tenant. For example, we may have all
 * possible fields across all tenants in the customer record. specific tenants
 * can choose to redefine one or more fields in this.
 *
 * @author simplity.org
 *
 */
public class RecordOverride {
	/**
	 * name of the record.
	 */
	String name;
	/**
	 * tenant id for which this override is meant for
	 */
	String tenantId;
	/**
	 * field definitions to be overridden. note that the field name must exist
	 * in the record, and this can only change its meta-data
	 */
	Map<String, FieldOverride> fields;
}
