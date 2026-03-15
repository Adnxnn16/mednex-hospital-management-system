package com.mednex.web.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PatientDto(
		UUID id,
		String firstName,
		String lastName,
		LocalDate dob,
		String gender,
		String email,
		String phone,
		String address,
		String bloodGroup,
		String occupation,
		String emergencyContactName,
		String emergencyContactPhone,
		String insuranceProvider,
		String policyNumber,
		String medicalHistory,
		Instant createdAt,
		Instant updatedAt) {}
