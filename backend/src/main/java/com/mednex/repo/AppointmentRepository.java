package com.mednex.repo;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mednex.domain.Appointment;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
    
    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.doctor.id = :doctorId " +
           "AND ((a.startTime < :endTime AND a.endTime > :startTime))")
    boolean hasConflict(@Param("doctorId") UUID doctorId, 
                       @Param("startTime") OffsetDateTime startTime, 
                       @Param("endTime") OffsetDateTime endTime);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.QueryHints({@jakarta.persistence.QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")})
    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId AND (a.startTime < :endTime AND a.endTime > :startTime)")
    java.util.List<Appointment> findOverlappingWithLock(@Param("doctorId") UUID doctorId, 
                                          @Param("startTime") OffsetDateTime startTime, 
                                          @Param("endTime") OffsetDateTime endTime);
}
