package com.mednex.service;

import com.mednex.audit.AuditService;
import com.mednex.domain.Appointment;
import com.mednex.domain.Doctor;
import com.mednex.domain.Patient;
import com.mednex.repo.AppointmentRepository;
import com.mednex.repo.DoctorRepository;
import com.mednex.repo.PatientRepository;
import com.mednex.web.dto.AppointmentDto;
import com.mednex.web.request.CreateAppointmentRequest;
import org.springframework.security.access.AccessDeniedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock AppointmentRepository appointmentRepo;
    @Mock PatientRepository patientRepo;
    @Mock DoctorRepository doctorRepo;
    @Mock EmailService emailService;
    @Mock AuditService auditService;
    @InjectMocks AppointmentService appointmentService;

    @Test
    void book_succeeds_when_slot_is_free() {
        CreateAppointmentRequest req = buildRequest();
        Patient patient = buildPatient();
        Doctor doctor = buildDoctor();

        when(appointmentRepo.findOverlappingWithLock(any(), any(), any()))
            .thenReturn(Collections.emptyList());
        when(patientRepo.findById(req.patientId()))
            .thenReturn(Optional.of(patient));
        when(doctorRepo.findById(req.doctorId()))
            .thenReturn(Optional.of(doctor));
        when(appointmentRepo.save(any(Appointment.class)))
            .thenAnswer(inv -> {
                Appointment a = inv.getArgument(0);
                a.setId(UUID.randomUUID());
                return a;
            });

        AppointmentDto result = appointmentService.book(req);

        assertThat(result).isNotNull();
        verify(appointmentRepo).save(any(Appointment.class));
        verify(auditService).log(eq("BOOK_APPOINTMENT"), eq("Appointment"), anyString(), any(UUID.class), isNull(), anyString());
    }

    @Test
    void book_throws_conflict_when_slot_is_taken() {
        CreateAppointmentRequest req = buildRequest();

        when(appointmentRepo.findOverlappingWithLock(any(), any(), any()))
            .thenReturn(List.of(new Appointment()));

        assertThatThrownBy(() -> appointmentService.book(req))
            .isInstanceOf(ConflictException.class)
            .hasMessageContaining("Time slot unavailable");

        verify(appointmentRepo, never()).save(any());
    }

    @Test
    void book_throws_when_patient_not_found() {
        CreateAppointmentRequest req = buildRequest();

        when(appointmentRepo.findOverlappingWithLock(any(), any(), any()))
            .thenReturn(Collections.emptyList());
        when(patientRepo.findById(req.patientId()))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.book(req))
            .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void list_returns_all_appointments() {
        Appointment appt = new Appointment();
        appt.setId(UUID.randomUUID());
        Patient p = buildPatient();
        Doctor d = buildDoctor();
        appt.setPatient(p);
        appt.setDoctor(d);
        appt.setStartTime(OffsetDateTime.now());
        appt.setEndTime(OffsetDateTime.now().plusMinutes(30));

        when(appointmentRepo.findAll()).thenReturn(List.of(appt));

        List<AppointmentDto> result = appointmentService.list();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).patientId()).isEqualTo(p.getId());
    }

    // --- helpers ---
    private CreateAppointmentRequest buildRequest() {
        return new CreateAppointmentRequest(
            UUID.randomUUID(), UUID.randomUUID(),
            OffsetDateTime.of(2026, 6, 1, 9, 0, 0, 0, java.time.ZoneOffset.UTC),
            OffsetDateTime.of(2026, 6, 1, 9, 30, 0, 0, java.time.ZoneOffset.UTC));
    }

    private Patient buildPatient() {
        Patient p = new Patient();
        p.setId(UUID.randomUUID());
        p.setFirstName("John");
        p.setLastName("Doe");
        p.setEmail("john@test.com");
        return p;
    }

    private Doctor buildDoctor() {
        Doctor d = new Doctor();
        d.setId(UUID.randomUUID());
        d.setFirstName("John");
        d.setLastName("Smith");
        d.setSpecialisation("General");
        d.setLicenceNumber("LIC-001");
        d.setTenantId("hospital_a");
        return d;
    }
}
