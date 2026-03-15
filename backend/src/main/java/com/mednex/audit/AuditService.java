package com.mednex.audit;

import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

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

	public void log(String action, String entityType, String entityId, String userId, String metadataJson) {
		log(action, entityType, entityId, null, userId, metadataJson);
	}

	public void log(String action, String entityType, String entityId, java.util.UUID patientId, String userId, String metadataJson) {
		String tenant = TenantContext.getTenantId();
		AuditLogEntry entry = new AuditLogEntry();
		entry.setTenantId(tenant == null ? "unknown" : tenant);
		entry.setUserId(userId);
		entry.setAction(action);
		entry.setEntityType(entityType);
		entry.setEntityId(entityId);
		entry.setPatientId(patientId);
		entry.setMetadata(metadataJson == null || metadataJson.isBlank() ? "{}" : metadataJson);
		writeAsync(entry, tenant);
	}

	@Async
	protected void writeAsync(@NonNull AuditLogEntry entry, String tenant) {
		try {
			if (tenant != null && !tenant.isBlank()) {
				TenantContext.setTenantId(tenant);
			}
			repo.save(entry);
		} finally {
			TenantContext.clear();
		}
	}
}
