package com.mednex.web.request;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record CreateAppointmentRequest(@NotNull UUID patientId, @NotNull UUID doctorId, @NotNull OffsetDateTime startTime,
		@NotNull OffsetDateTime endTime) {}
