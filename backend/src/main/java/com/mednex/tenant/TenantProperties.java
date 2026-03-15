package com.mednex.tenant;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mednex.tenants")
public record TenantProperties(List<String> allowed, String defaultTenant) {
	public boolean isAllowed(String tenantId) {
		if (tenantId == null || tenantId.isBlank()) {
			return false;
		}
		return allowed != null && allowed.contains(tenantId);
	}
}
