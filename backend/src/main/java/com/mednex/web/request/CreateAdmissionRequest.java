package com.mednex.web.request;

import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record CreateAdmissionRequest(@Valid @NotNull CreatePatientRequest patient, @NotNull UUID bedId, String notes) {}
