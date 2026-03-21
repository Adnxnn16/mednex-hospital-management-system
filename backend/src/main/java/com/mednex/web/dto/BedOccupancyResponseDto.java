package com.mednex.web.dto;

import java.util.List;

/**
 * PRD-compliant bed occupancy response with ward-level breakdown.
 */
public record BedOccupancyResponseDto(
	String tenantId,
	long totalBeds,
	long occupiedBeds,
	double occupancyRate,
	List<WardOccupancyItemDto> wards
) {}
