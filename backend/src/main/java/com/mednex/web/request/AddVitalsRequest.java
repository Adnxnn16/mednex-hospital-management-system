package com.mednex.web.request;

import jakarta.validation.constraints.NotBlank;

public record AddVitalsRequest(
	@NotBlank String bloodPressure,
	@NotBlank String heartRate,
	@NotBlank String temperature,
	@NotBlank String oxygenLevel
) {}
