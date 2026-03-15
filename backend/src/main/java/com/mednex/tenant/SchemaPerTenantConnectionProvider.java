package com.mednex.tenant;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.hibernate.engine.jdbc.connections.spi.AbstractDataSourceBasedMultiTenantConnectionProviderImpl;

public class SchemaPerTenantConnectionProvider extends AbstractDataSourceBasedMultiTenantConnectionProviderImpl<String> {
	private final DataSource dataSource;

	public SchemaPerTenantConnectionProvider(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	protected DataSource selectAnyDataSource() {
		return dataSource;
	}

	@Override
	protected DataSource selectDataSource(String tenantIdentifier) {
		return dataSource;
	}

	@Override
	public Connection getConnection(String tenantIdentifier) throws SQLException {
		Connection connection = super.getConnection(tenantIdentifier);
		setSearchPath(connection, tenantIdentifier);
		return connection;
	}

	@Override
	public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
		try {
			setSearchPath(connection, "public");
		} finally {
			super.releaseConnection(tenantIdentifier, connection);
		}
	}

	private static void setSearchPath(Connection connection, String schema) throws SQLException {
		if (schema == null || !schema.matches("[a-z0-9_]+")) {
			throw new SQLException("Invalid schema name");
		}
		try (Statement st = connection.createStatement()) {
			st.execute("SET search_path TO \"" + schema + "\", public");
		}
	}
}
