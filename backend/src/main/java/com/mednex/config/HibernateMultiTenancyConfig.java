package com.mednex.config;

import javax.sql.DataSource;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mednex.tenant.HibernateTenantIdentifierResolver;
import com.mednex.tenant.SchemaPerTenantConnectionProvider;
import com.mednex.tenant.TenantFilter;
import com.mednex.tenant.TenantProperties;

@Configuration
@EnableConfigurationProperties({ TenantProperties.class, SecurityProperties.class })
public class HibernateMultiTenancyConfig {

	@Bean
	public MultiTenantConnectionProvider<String> multiTenantConnectionProvider(DataSource dataSource) {
		return new SchemaPerTenantConnectionProvider(dataSource);
	}

	@Bean
	public CurrentTenantIdentifierResolver<String> currentTenantIdentifierResolver(TenantProperties props) {
		return new HibernateTenantIdentifierResolver(props);
	}

	@Bean
	public HibernatePropertiesCustomizer hibernatePropertiesCustomizer(
			MultiTenantConnectionProvider<String> multiTenantConnectionProvider,
			CurrentTenantIdentifierResolver<String> currentTenantIdentifierResolver) {
		return (properties) -> {
			properties.put("hibernate.multi_tenant_connection_provider", multiTenantConnectionProvider);
			properties.put("hibernate.tenant_identifier_resolver", currentTenantIdentifierResolver);
		};
	}

	@Bean
	public TenantFilter tenantFilter(TenantProperties props, com.mednex.audit.AuditService auditService) {
		return new TenantFilter(props, auditService);
	}
}
