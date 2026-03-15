package com.mednex.web;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mednex.service.DoctorService;
import com.mednex.web.dto.DoctorDto;
import com.mednex.web.request.CreateDoctorRequest;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {
	private final DoctorService service;

	public DoctorController(DoctorService service) {
		this.service = service;
	}

	@GetMapping
	@PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_DOCTOR','ROLE_NURSE')")
	public List<DoctorDto> list() {
		return service.list();
	}

	@PostMapping
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public DoctorDto create(@Valid @RequestBody CreateDoctorRequest req) {
		return service.create(req);
	}
}
