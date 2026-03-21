package com.mednex.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mednex.domain.AuditLogEntry;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLogEntry, UUID> {}
