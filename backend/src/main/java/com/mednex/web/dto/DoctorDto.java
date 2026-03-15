package com.mednex.web.dto;

import java.time.Instant;
import java.util.UUID;

public record DoctorDto(UUID id, String fullName, String email, String specialty, Instant createdAt) {}
