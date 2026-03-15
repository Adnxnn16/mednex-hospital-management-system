package com.mednex.web.request;

import jakarta.validation.constraints.NotBlank;

public record CreateDoctorRequest(@NotBlank String fullName, String email, String specialty) {}
