package com.mednex.tenant;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.mednex.audit.AuditService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

public class TenantFilter extends OncePerRequestFilter {
	public static final String TENANT_HEADER = "X-Tenant";

	private final TenantProperties props;
	private final AuditService auditService;

	public TenantFilter(TenantProperties props, AuditService auditService) {
		this.props = props;
		this.auditService = auditService;
	}

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain) throws ServletException, IOException {
		try {
			String headerTenant = request.getHeader(TENANT_HEADER);
			String jwtTenant = extractTenantFromJwt();

			if (headerTenant != null && !headerTenant.isBlank() && jwtTenant != null && !jwtTenant.isBlank()
					&& !headerTenant.equals(jwtTenant)) {
				
				auditService.log("SECURITY_VIOLATION", "TENANT_FILTER", "N/A", extractUserId(), 
					"{\"headerTenant\":\"" + headerTenant + "\", \"jwtTenant\":\"" + jwtTenant + "\", \"ip\":\"" + request.getRemoteAddr() + "\"}");

				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				response.setContentType("application/json");
				response.getWriter().write("{\"message\":\"Cross-tenant access denied\", \"status\":403}");
				return;
			}

			String tenant = (headerTenant != null && !headerTenant.isBlank()) ? headerTenant : jwtTenant;
			if (tenant == null || tenant.isBlank()) {
				tenant = props.defaultTenant();
			}

			if (!props.isAllowed(tenant)) {
				response.setStatus(HttpStatus.BAD_REQUEST.value());
				response.setContentType("application/json");
				response.getWriter().write("{\"message\":\"Invalid tenant\"}");
				return;
			}

			TenantContext.setTenantId(tenant);
			filterChain.doFilter(request, response);
		} finally {
			TenantContext.clear();
		}
	}

	private String extractUserId() {
		Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
		if (auth instanceof JwtAuthenticationToken jwtAuth) {
			return jwtAuth.getName();
		}
		return "anonymous";
	}

	private static String extractTenantFromJwt() {
		Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
		if (auth instanceof JwtAuthenticationToken jwtAuth) {
			Object tenant = jwtAuth.getTokenAttributes().get("tenant");
			return tenant == null ? null : String.valueOf(tenant);
		}
		return null;
	}
}

