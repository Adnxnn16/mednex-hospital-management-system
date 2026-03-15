package com.mednex.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mednex.domain.AuditLogEntry;

public interface AuditLogRepository extends JpaRepository<AuditLogEntry, Long> {}
