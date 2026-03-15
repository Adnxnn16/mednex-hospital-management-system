package com.mednex.web;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mednex.service.AdmissionService;
import com.mednex.web.dto.AdmissionDto;
import com.mednex.web.request.CreateAdmissionRequest;

@RestController
@RequestMapping("/api/admissions")
public class AdmissionController {
	private final AdmissionService service;

	public AdmissionController(AdmissionService service) {
		this.service = service;
	}

	@GetMapping
	@PreAuthorize("hasAuthority('ROLE_NURSE')")
	public List<AdmissionDto> list() {
		return service.list();
	}

	@PostMapping
	@PreAuthorize("hasAuthority('ROLE_NURSE')")
	public AdmissionDto admit(@Valid @RequestBody CreateAdmissionRequest req, Authentication auth) {
		String createdBy = auth == null ? null : auth.getName();
		return service.admit(req, createdBy);
	}

	@PostMapping("/{id}/discharge")
	@PreAuthorize("hasAuthority('ROLE_NURSE')")
	public AdmissionDto discharge(@org.springframework.web.bind.annotation.PathVariable UUID id, Authentication auth) {
		String dischargedBy = auth == null ? null : auth.getName();
		return service.discharge(id, dischargedBy);
	}

	@PostMapping("/{id}/vitals")
	@PreAuthorize("hasAuthority('ROLE_NURSE')")
	public AdmissionDto addVitals(@org.springframework.web.bind.annotation.PathVariable UUID id,
			@Valid @RequestBody com.mednex.web.request.AddVitalsRequest req, Authentication auth) {
		String addedBy = auth == null ? null : auth.getName();
		return service.addVitals(id, req, addedBy);
	}
}
