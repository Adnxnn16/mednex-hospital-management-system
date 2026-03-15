package com.mednex.web.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateBedStatusRequest(@NotBlank String status) {}
