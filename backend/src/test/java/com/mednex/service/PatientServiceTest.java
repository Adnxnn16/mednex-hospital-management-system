package com.mednex.service;

import com.mednex.domain.Patient;
import com.mednex.repo.PatientRepository;
import com.mednex.audit.AuditService;
import com.mednex.validation.InvalidMedicalHistoryException;
import com.mednex.validation.MedicalHistoryValidator;
import com.mednex.web.dto.PatientDto;
import com.mednex.web.request.CreatePatientRequest;
import org.springframework.security.access.AccessDeniedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock PatientRepository repo;
    @Mock MedicalHistoryValidator validator;
    @Mock AuditService auditService;
    @InjectMocks PatientService patientService;

    @Test
    void get_returns_dto_when_patient_exists() {
        UUID id = UUID.randomUUID();
        Patient entity = buildPatient(id);

        when(repo.findById(id)).thenReturn(Optional.of(entity));

        PatientDto result = patientService.get(id);

        assertThat(result.id()).isEqualTo(id);
        assertThat(result.firstName()).isEqualTo("John");
        assertThat(result.lastName()).isEqualTo("Doe");
        verify(repo).findById(id);
    }

    @Test
    void get_throws_when_patient_not_found() {
        UUID id = UUID.randomUUID();
        when(repo.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.get(id))
            .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void list_returns_all_patients_as_dtos() {
        Patient p1 = buildPatient(UUID.randomUUID());
        Patient p2 = buildPatient(UUID.randomUUID());
        p2.setFirstName("Jane");

        when(repo.findAll()).thenReturn(List.of(p1, p2));

        List<PatientDto> result = patientService.list();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).firstName()).isEqualTo("John");
        assertThat(result.get(1).firstName()).isEqualTo("Jane");
    }

    @Test
    void create_validates_history_and_saves() {
        // 50 arguments
        CreatePatientRequest req = new CreatePatientRequest(
            "John", "Doe", LocalDate.of(1990, 1, 1), "MALE", "john@test.com", "123-456", "Addr", "O+", "Engineer", "Emergency", "555-0000", "InsureCo", "POL-001", "{\"version\":\"1.0\"}",
            null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        doNothing().when(validator).validate(anyString());
        when(repo.save(any(Patient.class))).thenAnswer(inv -> {
            Patient p = inv.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });

        PatientDto result = patientService.create(req);

        assertThat(result).isNotNull();
        assertThat(result.firstName()).isEqualTo("John");
        verify(validator).validate(anyString());
        verify(repo).save(any(Patient.class));
    }

    @Test
    void create_defaults_empty_medical_history() {
        CreatePatientRequest req = new CreatePatientRequest(
            "John", "Doe", null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        doNothing().when(validator).validate(anyString());
        when(repo.save(any(Patient.class))).thenAnswer(inv -> {
            Patient p = inv.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });

        PatientDto result = patientService.create(req);

        assertThat(result).isNotNull();
        verify(validator).validate(contains("version"));
    }

    @Test
    void create_throws_when_medical_history_invalid() {
        CreatePatientRequest req = new CreatePatientRequest(
            "John", "Doe", null, null, null, null, null, null, null, null, null, null, null, "{\"invalid\":true}",
            null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        doThrow(new InvalidMedicalHistoryException("bad json"))
            .when(validator).validate(anyString());

        assertThatThrownBy(() -> patientService.create(req))
            .isInstanceOf(InvalidMedicalHistoryException.class)
            .hasMessageContaining("bad json");

        verify(repo, never()).save(any());
    }

    @Test
    void addConsultation_success() {
        UUID id = UUID.randomUUID();
        Patient entity = buildPatient(id);
        entity.setMedicalHistory("{\"version\":\"1.0\"}");

        com.mednex.web.request.AddConsultationRequest req = new com.mednex.web.request.AddConsultationRequest(
            "2023-10-01", "Dr. Smith", "Cough", "Flu", "Rest", "Take fluids"
        );

        when(repo.findById(id)).thenReturn(Optional.of(entity));
        when(repo.save(any(Patient.class))).thenAnswer(inv -> inv.getArgument(0));

        PatientDto result = patientService.addConsultation(id, req);

        assertThat(result.medicalHistory()).contains("consultations");
        assertThat(result.medicalHistory()).contains("Dr. Smith");
        assertThat(result.medicalHistory()).contains("Flu");
        verify(repo).save(any(Patient.class));
    }

    @Test
    void create_injects_version_when_missing() {
        CreatePatientRequest req = new CreatePatientRequest(
            "John", "Doe", null, null, null, null, null, null, null, null, null, null, null, "{\"diag\":\"test\"}",
            null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        doNothing().when(validator).validate(anyString());
        when(repo.save(any(Patient.class))).thenAnswer(inv -> inv.getArgument(0));

        PatientDto result = patientService.create(req);

        assertThat(result.medicalHistory()).contains("\"version\":\"1.0\"");
        assertThat(result.medicalHistory()).contains("\"diag\":\"test\"");
    }

    @Test
    void list_returns_all_patients() {
        Patient p1 = buildPatient(UUID.randomUUID());
        Patient p2 = buildPatient(UUID.randomUUID());
        when(repo.findAll()).thenReturn(List.of(p1, p2));

        List<PatientDto> result = patientService.list();

        assertThat(result).hasSize(2);
        verify(repo).findAll();
    }

    // --- helpers ---
    private Patient buildPatient(UUID id) {
        Patient p = new Patient();
        p.setId(id);
        p.setFirstName("John");
        p.setLastName("Doe");
        p.setDob(LocalDate.of(1990, 1, 1));
        p.setGender("MALE");
        p.setMedicalHistory("{\"version\":\"1.0\"}");
        p.setCreatedAt(Instant.now());
        p.setUpdatedAt(Instant.now());
        return p;
    }
}
