package com.mednex.web;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mednex.service.AppointmentService;
import com.mednex.web.dto.AppointmentDto;
import com.mednex.web.request.CreateAppointmentRequest;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {
	private final AppointmentService service;

	public AppointmentController(AppointmentService service) {
		this.service = service;
	}

	@GetMapping
	@PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_DOCTOR','ROLE_NURSE')")
	public List<AppointmentDto> list() {
		return service.list();
	}

	@PostMapping
	@PreAuthorize("hasAnyAuthority('ROLE_DOCTOR','ROLE_NURSE')")
	public AppointmentDto book(@Valid @RequestBody CreateAppointmentRequest req) {
		return service.book(req);
	}
}
