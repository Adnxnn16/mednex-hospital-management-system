package com.mednex.web;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mednex.service.BedService;
import com.mednex.web.dto.BedDto;
import com.mednex.web.request.UpdateBedStatusRequest;

@RestController
@RequestMapping("/api/beds")
public class BedController {
	private final BedService service;

	public BedController(BedService service) {
		this.service = service;
	}

	@GetMapping
	@PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_NURSE','ROLE_DOCTOR')")
	public List<BedDto> list() {
		return service.list();
	}

	@PatchMapping("/{id}/status")
	@PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_NURSE')")
	public BedDto updateStatus(@PathVariable UUID id, @Valid @RequestBody UpdateBedStatusRequest req) {
		return service.updateStatus(id, req.status());
	}
}
