package com.mednex.web.dto;

import java.time.Instant;
import java.util.UUID;

public record DoctorDto(
	UUID id,
	String firstName,
	String lastName,
	String specialisation,
	String licenceNumber,
	String tenantId,
	String email,
	Instant createdAt
) {
	/** Backward-compatible display name for frontend. */
	public String fullName() {
		return ((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "")).trim();
	}
	/** Alias for frontend compatibility. */
	public String specialty() { return specialisation; }
}
