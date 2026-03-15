package com.mednex.repo;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mednex.domain.Doctor;

public interface DoctorRepository extends JpaRepository<Doctor, UUID> {}
