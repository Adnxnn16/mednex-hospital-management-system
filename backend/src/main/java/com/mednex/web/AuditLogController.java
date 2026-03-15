package com.mednex.web;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mednex.repo.AuditLogRepository;
import com.mednex.web.dto.AuditLogDto;

@RestController
@RequestMapping("/api/audit-log")
public class AuditLogController {
	private final AuditLogRepository repo;

	public AuditLogController(AuditLogRepository repo) {
		this.repo = repo;
	}

	@GetMapping
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public List<AuditLogDto> list() {
		return repo.findAll().stream().map(e -> new AuditLogDto(e.getId(), e.getTenantId(), e.getUserId(), e.getAction(), e.getEntityType(),
				e.getEntityId(), e.getOccurredAt(), e.getMetadata())).toList();
	}
}
