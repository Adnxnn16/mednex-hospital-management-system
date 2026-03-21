package com.mednex.web.dto;

import java.time.Instant;
import java.util.UUID;

public record AuditLogDto(UUID id, String tenantId, String userId, String action, String entityType, String entityId, Instant occurredAt,
		String metadata) {}
