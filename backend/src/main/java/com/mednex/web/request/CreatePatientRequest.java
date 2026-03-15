package com.mednex.web.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;

public record CreatePatientRequest(
		@NotBlank String firstName,
		@NotBlank String lastName,
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
		String medicalHistory) {}
