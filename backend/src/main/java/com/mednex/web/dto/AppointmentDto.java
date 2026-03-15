package com.mednex.web.dto;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AppointmentDto(UUID id, UUID patientId, UUID doctorId, OffsetDateTime startTime, OffsetDateTime endTime, String status,
		Instant createdAt) {}
