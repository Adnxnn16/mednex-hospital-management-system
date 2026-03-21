package com.mednex.service;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mednex.domain.Bed;
import com.mednex.repo.BedRepository;
import com.mednex.tenant.TenantContext;
import com.mednex.web.dto.BedDto;

@Service
public class BedService {
	private static final Logger log = LoggerFactory.getLogger(BedService.class);
	private final BedRepository repo;

	public BedService(BedRepository repo) {
		this.repo = repo;
	}

	@PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_NURSE','ROLE_DOCTOR')")
	@Transactional(readOnly = true)
	public List<BedDto> list() {
		return repo.findAll().stream().map(BedService::toDto).toList();
	}

	@Transactional(readOnly = true)
	public Bed getEntity(UUID id) {
		return repo.findById(id).orElseThrow(() -> {
			log.warn("SECURITY_PROBE tenantId={} bedId={}",
				TenantContext.getTenantId(), id);
			return new AccessDeniedException(
				"Access denied: resource not available in current tenant context"
			);
		});
	}

	@PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_NURSE')")
	@Transactional
	public BedDto updateStatus(UUID id, String status) {
		Bed bed = getEntity(id);
		bed.setStatus(status);
		return toDto(repo.save(bed));
	}

	static BedDto toDto(Bed b) {
		return new BedDto(b.getId(), b.getWard(), b.getRoom(), b.getBedNumber(), b.getStatus(), b.getCreatedAt());
	}
}
