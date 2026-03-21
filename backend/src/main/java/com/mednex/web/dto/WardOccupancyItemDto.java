package com.mednex.web.dto;

/**
 * Per-ward occupancy for PRD analytics response.
 */
public record WardOccupancyItemDto(
	String wardName,
	long totalBeds,
	long occupiedBeds,
	double occupancyRate
) {}
