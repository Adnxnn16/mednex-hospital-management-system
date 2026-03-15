package com.mednex.audit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuditAspect {
	private final AuditService auditService;

	public AuditAspect(AuditService auditService) {
		this.auditService = auditService;
	}

	@Around("execution(public * com.mednex.service..*(..))")
	public Object aroundServiceCall(ProceedingJoinPoint pjp) throws Throwable {
		Object result = pjp.proceed();
		try {
			String userId = resolveUserId();
			String action = pjp.getSignature().toShortString();
			java.util.UUID patientId = extractPatientId(pjp.getArgs(), result);
			auditService.log(action, resolveEntityType(pjp), extractEntityId(result), patientId, userId, "{}");
		} catch (Exception e) {
			// Don't break the main business flow if auditing fails
		}
		return result;
	}

	private java.util.UUID extractPatientId(Object[] args, Object result) {
		// 1. Check method args for a UUID (direct patientId param)
		for (Object arg : args) {
			if (arg instanceof java.util.UUID uuid) return uuid;
		}
		// 2. Check if arg is a DTO with getPatientId()
		for (Object arg : args) {
			if (arg != null) {
				try {
					var method = arg.getClass().getMethod("getPatientId");
					Object id = method.invoke(arg);
					if (id instanceof java.util.UUID uuid) return uuid;
				} catch (Exception ignored) {}
			}
		}
		// 3. Check if arg is a DTO with patientId() (record style)
		for (Object arg : args) {
			if (arg != null) {
				try {
					var method = arg.getClass().getMethod("patientId");
					Object id = method.invoke(arg);
					if (id instanceof java.util.UUID uuid) return uuid;
				} catch (Exception ignored) {}
			}
		}
		// 4. Check return value
		if (result != null) {
			try {
				var method = result.getClass().getMethod("getId");
				Object id = method.invoke(result);
				if (id instanceof java.util.UUID uuid && result.getClass().getSimpleName().contains("Patient")) {
					return uuid;
				}
			} catch (Exception ignored) {}
			try {
				var method = result.getClass().getMethod("getPatientId");
				Object id = method.invoke(result);
				if (id instanceof java.util.UUID uuid) return uuid;
			} catch (Exception ignored) {}
		}
		return null;
	}

	private String resolveEntityType(ProceedingJoinPoint pjp) {
		String className = pjp.getTarget().getClass().getSimpleName();
		return className.replace("Service", "");
	}

	private String extractEntityId(Object result) {
		if (result == null) return null;
		try {
			var method = result.getClass().getMethod("getId");
			Object id = method.invoke(result);
			return id == null ? null : String.valueOf(id);
		} catch (Exception ignored) {}
		return null;
	}

	private static String resolveUserId() {
		Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
		if (auth instanceof JwtAuthenticationToken jwtAuth) {
			Object preferred = jwtAuth.getTokenAttributes().get("preferred_username");
			return preferred == null ? jwtAuth.getName() : String.valueOf(preferred);
		}
		return auth == null ? null : auth.getName();
	}
}
