package com.mednex.repo;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mednex.domain.Patient;

public interface PatientRepository extends JpaRepository<Patient, UUID> {}
