package org.simplity.fm.example.gen.list;

import org.simplity.fm.core.validn.RuntimeList;

/**
 * UserType
 */
public class UserType extends RuntimeList {
	 private static final String NAME = "userType";
	 private static final String LIST_SQL = "SELECT null, null FROM null";
	 private static final String CHECK_SQL = "SELECT null FROM null WHERE null=?";

/**
 * UserType
 */
	public UserType() {
		this.name = NAME;
		this.listSql = LIST_SQL;
		this.checkSql = CHECK_SQL;
	}
}
