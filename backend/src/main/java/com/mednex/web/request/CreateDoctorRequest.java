package com.mednex.web.request;

import jakarta.validation.constraints.NotBlank;

public record CreateDoctorRequest(
	@NotBlank String firstName,
	@NotBlank String lastName,
	@NotBlank String specialisation,
	@NotBlank String licenceNumber,
	String email
) {}
