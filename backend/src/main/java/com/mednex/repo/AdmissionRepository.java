package com.mednex.repo;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mednex.domain.Admission;

public interface AdmissionRepository extends JpaRepository<Admission, UUID> {}
