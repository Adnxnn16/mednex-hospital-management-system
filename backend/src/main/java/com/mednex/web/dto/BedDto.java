package com.mednex.web.dto;

import java.time.Instant;
import java.util.UUID;

public record BedDto(UUID id, String ward, String room, String bedNumber, String status, Instant createdAt) {}
