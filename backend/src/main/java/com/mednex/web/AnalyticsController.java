package com.mednex.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mednex.service.AnalyticsService;
import com.mednex.web.dto.BedOccupancyResponseDto;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {
	private final AnalyticsService service;

	public AnalyticsController(AnalyticsService service) {
		this.service = service;
	}

	@GetMapping("/bed-occupancy")
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public BedOccupancyResponseDto bedOccupancy() {
		return service.bedOccupancy();
	}
}
