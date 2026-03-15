package com.mednex.web.dto;

import java.time.Instant;

public record AuditLogDto(long id, String tenantId, String userId, String action, String entityType, String entityId, Instant occurredAt,
		String metadata) {}
