package com.mednex.web.request;

import jakarta.validation.constraints.NotBlank;

public record AddConsultationRequest(
	@NotBlank String date,
	@NotBlank String doctorName,
	String symptoms,
	String diagnosis,
	String treatment,
	String notes
) {}
