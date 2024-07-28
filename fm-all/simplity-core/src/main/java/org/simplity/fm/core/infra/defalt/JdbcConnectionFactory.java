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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.simplity.fm.core.infra.IDbConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * serves as an example, or even a base class, for an application to design its
 * IConnecitonFactory class.
 *
 * @author simplity.org
 *
 */
public class JdbcConnectionFactory implements IDbConnectionFactory {
	private static final Logger logger = LoggerFactory.getLogger(JdbcConnectionFactory.class);

	/**
	 * get a factory that gets connection to default schema. This factory can not
	 * get connection to any other schema
	 *
	 * @param conString       non-null connection string
	 * @param driverClassName non-null driver class name
	 * @return factory that can be used to get connection to a default schema. null
	 *         in case the credentials could not be used to get a sample connection
	 */
	public static IDbConnectionFactory getFactory(String conString, String driverClassName) {
		IFactory f = getCsFactory(conString, driverClassName);
		if (f == null) {
			return null;
		}
		return new JdbcConnectionFactory(f, null, null);
	}

	/**
	 * get a factory that gets connection to default schema. This factory an not get
	 * connection to any other schema
	 *
	 * @param dataSourceName non-null jndi name for data source
	 * @return factory that can be used to get connection to a default schema. null
	 *         in case the credentials could not be used to get a sample connection
	 */
	public static IDbConnectionFactory getFactory(String dataSourceName) {
		IFactory f = getDsFactory(dataSourceName, null);
		if (f == null) {
			return null;
		}
		return new JdbcConnectionFactory(f, null, null);
	}

	/**
	 * get a factory that gets connection to default schema. This factory can not
	 * get connection to any other schema
	 *
	 * @param dataSource non-null instance of data source
	 * @return factory that can be used to get connection to a default schema. null
	 *         in case the credentials could not be used to get a sample connection
	 */
	public static IDbConnectionFactory getFactory(DataSource dataSource) {
		IFactory f = getDsFactory(null, dataSource);
		if (f == null) {
			return null;
		}
		return new JdbcConnectionFactory(f, null, null);
	}

	private final IFactory defFactory;
	private final IFactory altFactory;
	private final String altSchema;

	private JdbcConnectionFactory(IFactory defFactory, String altSchema, IFactory altFactory) {
		this.defFactory = defFactory;
		this.altFactory = altFactory;
		this.altSchema = altSchema;
	}

	@SuppressWarnings("resource")
	@Override
	public Connection getConnection() throws SQLException {
		if (this.defFactory == null) {
			throw new SQLException("No credentials set up for accessing a db");
		}
		return this.defFactory.getConnection();
	}

	@SuppressWarnings("resource")
	@Override
	public Connection getConnection(String schemaName) throws SQLException {
		if (this.altSchema == null || this.altFactory == null || this.altSchema.contentEquals(schemaName) == false) {
			throw new SQLException("No credentials set up for schema {}", schemaName);
		}
		return this.altFactory.getConnection();
	}

	private static IFactory getDsFactory(String jndiName, DataSource dataSource) {
		DataSource ds = dataSource;
		if (ds == null) {
			try {
				ds = (DataSource) new InitialContext().lookup(jndiName);
			} catch (Exception e) {
				//
			}
		}

		if (ds == null) {
			logger.error("Could not locate the driver class using JNDI name {}", jndiName);
			return null;
		}
		/*
		 * test it..
		 */
		IFactory factory = new DsBasedFactory(ds);
		String msg = testIt(factory);
		if (msg != null) {
			logger.error(
					"Driver  was located successfully with the JNDI name {}, but it failed to get a valid connection to the database. Error: {}",
					jndiName, msg);
			return null;
		}

		logger.info("DB driver set successfully based on JNDI name {} ", jndiName);
		return factory;
	}

	private static String testIt(IFactory factory) {
		try (Connection con = factory.getConnection()) {
			return null;
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	private static IFactory getCsFactory(String conString, String driverClassName) {
		try {
			Class.forName(driverClassName);
		} catch (Exception e) {
			logger.error(
					"Unable to locate Driver Class {} . Please check the className, class-path settings and proper JDBC jar file",
					driverClassName);
			return null;
		}

		IFactory factory = new CsBasedFactory(conString);
		String msg = testIt(factory);
		if (msg != null) {
			logger.error("Error while using driver class {} with a connection string. Error: {} ", driverClassName,
					msg);
			return null;
		}

		logger.info("DB driver set based on connection string for driver {} ", driverClassName);
		return factory;
	}

	protected interface IFactory {
		Connection getConnection() throws SQLException;
	}

	protected static class DsBasedFactory implements IFactory {
		private final DataSource ds;

		protected DsBasedFactory(DataSource ds) {
			this.ds = ds;
		}

		@SuppressWarnings("resource")
		@Override
		public Connection getConnection() throws SQLException {
			return this.ds.getConnection();
		}

	}

	protected static class CsBasedFactory implements IFactory {
		private final String conString;

		protected CsBasedFactory(String conString) {
			this.conString = conString;
		}

		@SuppressWarnings("resource")
		@Override
		public Connection getConnection() throws SQLException {
			return DriverManager.getConnection(this.conString);
		}

	}
}
