package com.mednex.service;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mednex.domain.Doctor;
import com.mednex.repo.DoctorRepository;
import com.mednex.tenant.TenantContext;
import com.mednex.web.dto.DoctorDto;
import com.mednex.web.request.CreateDoctorRequest;

@Service
public class DoctorService {
	private static final Logger log = LoggerFactory.getLogger(DoctorService.class);
	private final DoctorRepository repo;

	public DoctorService(DoctorRepository repo) {
		this.repo = repo;
	}

	@Transactional(readOnly = true)
	public List<DoctorDto> list() {
		return repo.findAll().stream().map(DoctorService::toDto).toList();
	}

	@Transactional(readOnly = true)
	public Doctor getEntity(UUID id) {
		return repo.findById(id).orElseThrow(() -> {
			log.warn("SECURITY_PROBE tenantId={} doctorId={}",
				TenantContext.getTenantId(), id);
			return new AccessDeniedException(
				"Access denied: resource not available in current tenant context"
			);
		});
	}

	@Transactional
	public DoctorDto create(CreateDoctorRequest req) {
		String tenantId = TenantContext.getTenantId();
		if (tenantId == null || tenantId.isBlank()) {
			throw new IllegalStateException("Tenant context is required to create a doctor");
		}
		Doctor d = new Doctor();
		d.setFirstName(req.firstName());
		d.setLastName(req.lastName());
		d.setSpecialisation(req.specialisation());
		d.setLicenceNumber(req.licenceNumber());
		d.setEmail(req.email());
		d.setTenantId(tenantId);
		return toDto(repo.save(d));
	}

	static DoctorDto toDto(Doctor d) {
		return new DoctorDto(
			d.getId(),
			d.getFirstName(),
			d.getLastName(),
			d.getSpecialisation(),
			d.getLicenceNumber(),
			d.getTenantId(),
			d.getEmail(),
			d.getCreatedAt()
		);
	}
}
