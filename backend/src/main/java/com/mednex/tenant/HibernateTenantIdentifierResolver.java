package com.mednex.tenant;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;

public class HibernateTenantIdentifierResolver implements CurrentTenantIdentifierResolver<String> {
	private final TenantProperties props;

	public HibernateTenantIdentifierResolver(TenantProperties props) {
		this.props = props;
	}

	@Override
	public String resolveCurrentTenantIdentifier() {
		String tenant = TenantContext.getTenantId();
		if (tenant == null || tenant.isBlank()) {
			return props.defaultTenant();
		}
		return tenant;
	}

	@Override
	public boolean validateExistingCurrentSessions() {
		return true;
	}
}
