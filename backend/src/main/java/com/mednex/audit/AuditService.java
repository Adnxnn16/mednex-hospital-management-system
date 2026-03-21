package com.mednex.audit;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.mednex.domain.AuditLogEntry;
import com.mednex.repo.AuditLogRepository;
import com.mednex.tenant.TenantContext;

@Service
public class AuditService {
	private final AuditLogRepository repo;

	public AuditService(AuditLogRepository repo) {
		this.repo = repo;
	}

	public void log(String action, String entityType, String entityId, java.util.UUID patientId, String userId) {
		log(action, entityType, entityId, patientId, userId, "{}");
	}

	public void log(String action, String entityType, String entityId, String userId) {
		log(action, entityType, entityId, null, userId, "{}");
	}

	public void log1(String action, String entityType, String entityId, String userId, String metadataJson) {
		log(action, entityType, entityId, null, userId, metadataJson);
	}

	public void log(String action, String entityType, String entityId, java.util.UUID patientId, String userId, String metadataJson) {
		log(action, entityType, entityId, patientId, userId, metadataJson, null, null);
	}

	public void logSecurityViolation(String action, String entityType, String entityId, String requestedTenantId, String ipAddress) {
		log(action, entityType, entityId, null, null, "{}", requestedTenantId, ipAddress);
	}

	@Async
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void log(String action, String entityType, String entityId, java.util.UUID patientId, String userId, String metadataJson,
			String requestedTenantId, String ipAddress) {
		String tenant = TenantContext.getTenantId();
		AuditLogEntry entry = new AuditLogEntry();
		entry.setTenantId(requestedTenantId != null && !requestedTenantId.isBlank() ? requestedTenantId : (tenant == null ? "unknown" : tenant));
		entry.setUserId(resolveUser(userId));
		entry.setAction(action);
		entry.setEntityType(entityType);
		entry.setEntityId(entityId);
		entry.setPatientId(patientId);
		entry.setIpAddress(resolveIp(ipAddress));
		entry.setTimestamp(java.time.Instant.now());
		entry.setMetadata(metadataJson == null || metadataJson.isBlank() ? "{}" : metadataJson);
		try {
			if (tenant != null && !tenant.isBlank()) {
				TenantContext.setTenantId(tenant);
			}
			repo.save(entry);
		} finally {
			TenantContext.clear();
		}
	}

	private String resolveUser(String explicitUser) {
		if (explicitUser != null && !explicitUser.isBlank()) return explicitUser;
		var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
		return auth == null ? "anonymous" : auth.getName();
	}

	private String resolveIp(String explicitIp) {
		if (explicitIp != null && !explicitIp.isBlank()) return explicitIp;
		try {
			var attrs = org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
			if (attrs instanceof org.springframework.web.context.request.ServletRequestAttributes sra) {
				return sra.getRequest().getRemoteAddr();
			}
		} catch (Exception ignored) {}
		return "unknown";
	}
}
