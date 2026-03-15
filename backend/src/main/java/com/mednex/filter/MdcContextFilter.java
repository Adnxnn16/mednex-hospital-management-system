package com.mednex.filter;

import com.mednex.tenant.TenantContext;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(2)
public class MdcContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull jakarta.servlet.http.HttpServletResponse response,
            @NonNull FilterChain chain) throws ServletException, IOException {
        try {
            String tenantId = TenantContext.getTenantId();
            if (tenantId != null) {
                MDC.put("tenantId", tenantId);
            }

            Authentication auth = SecurityContextHolder
                .getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
                MDC.put("userId",
                    jwt.getClaimAsString("preferred_username"));
            }

            MDC.put("traceId", UUID.randomUUID().toString());
            MDC.put("requestUri", request.getRequestURI());

            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
