// SPDX-License-Identifier: MIT
package org.simplity.fm.core;

import org.simplity.fm.core.app.AppConfig;
import org.simplity.fm.core.infra.DbConnectionFactory;
import org.simplity.fm.core.infra.defalt.JdbcConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an example boot-strapper to be written by an App
 *
 * @author simplity.org
 *
 */
public class Bootstrapper {
	private static final Logger logger = LoggerFactory
			.getLogger(Bootstrapper.class);
	private static final String APP_NAME = "simplityExample";

	/**
	 *
	 * @return COnfig
	 */
	public AppConfig getConfig() {
		final AppConfig config = new AppConfig();
		config.appName = APP_NAME;
		/*
		 * so long as you keep this class in the rot package, it is safe to use
		 * the following..
		 */
		config.appRootPackage = this.getClass().getPackage().getName();
		/*
		 * set all other classes here...
		 */
		// config.dbConnectionFactory = dbSetupWithConString();
		return config;
	}

	/**
	 * method to be used if this app wants to use connection string for db
	 * connection
	 *
	 * @param conString
	 * @param driverName
	 */
	@SuppressWarnings("unused")
	private static DbConnectionFactory dbSetupWithConString() {
		final String conString = "Get it from wherever you are to get it, but never hard code in the code here";
		final String driverName = "driver name for the vendor/provider of JDBC you are using";
		logger.info(
				"Setting up db with driver name = {} and connectionString=****",
				driverName);
		return JdbcConnectionFactory.getFactory(conString, driverName);
	}

	/**
	 * method to be used to set up rdbms using data source JNDI
	 *
	 * @param dataSourceName
	 */
	@SuppressWarnings("unused")
	private static DbConnectionFactory dbSetupWithDataSource() {
		final String dataSourceName = "the designated JNDI name as per the documentation of the container/framework that provides this service";
		logger.info("Setting up db with dataSource name = {}", dataSourceName);
		return JdbcConnectionFactory.getFactory(dataSourceName);
	}

	/**
	 * method to be used if the app has a custom connection factory
	 *
	 * @param factory
	 */
	@SuppressWarnings("unused")
	private static DbConnectionFactory dbSetupWithCustomFctory() {
		/*
		 * write the code to get the factory, whichever you are supposed to get
		 * it..
		 */
		final DbConnectionFactory factory = null;
		// logger.info("Setting up db with a custom connection factory class
		// {}", factory.getClass().getName());
		return factory;
	}

}
