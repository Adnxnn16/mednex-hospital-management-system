package com.mednex.web.dto;

import java.time.Instant;
import java.util.UUID;

public record AdmissionDto(UUID id, UUID patientId, UUID bedId, Instant admittedAt, Instant dischargedAt, String notes, String vitals) {}
