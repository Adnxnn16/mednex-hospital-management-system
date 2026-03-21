package com.mednex.service;

import com.mednex.audit.AuditService;
import com.mednex.domain.Admission;
import com.mednex.domain.Bed;
import com.mednex.domain.Patient;
import com.mednex.repo.AdmissionRepository;
import com.mednex.repo.BedRepository;
import com.mednex.repo.PatientRepository;
import com.mednex.validation.MedicalHistoryValidator;
import com.mednex.web.dto.AdmissionDto;
import com.mednex.web.request.CreateAdmissionRequest;
import com.mednex.web.request.CreatePatientRequest;
import org.springframework.security.access.AccessDeniedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdmissionServiceTest {

    @Mock PatientRepository patientRepo;
    @Mock BedRepository bedRepo;
    @Mock AdmissionRepository admissionRepo;
    @Mock AuditService auditService;
    @Mock MedicalHistoryValidator validator;
    @InjectMocks AdmissionService admissionService;

    @Test
    void admit_marks_bed_as_occupied_and_creates_patient() {
        UUID bedId = UUID.randomUUID();
        Bed bed = new Bed();
        bed.setId(bedId);
        bed.setStatus("AVAILABLE");

        // 50 arguments total
        CreatePatientRequest patientReq = new CreatePatientRequest(
            "John", "Doe", null, "MALE", null, null, null, null, null, null, null, null, null, "{}",
            null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        
        CreateAdmissionRequest req = new CreateAdmissionRequest(patientReq, bedId, "Test notes");

        when(bedRepo.findById(bedId)).thenReturn(Optional.of(bed));
        doNothing().when(validator).validate(anyString());
        when(patientRepo.save(any(Patient.class))).thenAnswer(inv -> {
            Patient p = inv.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });
        when(bedRepo.save(any(Bed.class))).thenReturn(bed);
        when(admissionRepo.save(any(Admission.class))).thenAnswer(inv -> {
            Admission a = inv.getArgument(0);
            a.setId(UUID.randomUUID());
            return a;
        });

        AdmissionDto result = admissionService.admit(req, "nurse1");

        assertThat(result).isNotNull();
        assertThat(bed.getStatus()).isEqualTo("OCCUPIED");
        verify(bedRepo, times(2)).save(bed);
        verify(admissionRepo).save(any(Admission.class));
        verify(auditService).log(eq("ADMIT"), eq("Patient"), anyString(), any(UUID.class), eq("nurse1"), anyString());
    }

    @Test
    void admit_throws_when_bed_not_found() {
        UUID bedId = UUID.randomUUID();
        CreatePatientRequest patientReq = new CreatePatientRequest(
            "John", "Doe", null, null, null, null, null, null, null, null, null, null, null, "{}",
            null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        
        CreateAdmissionRequest req = new CreateAdmissionRequest(patientReq, bedId, null);

        when(bedRepo.findById(bedId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> admissionService.admit(req, "nurse1"))
            .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void admit_throws_conflict_when_bed_is_occupied() {
        UUID bedId = UUID.randomUUID();
        Bed bed = new Bed();
        bed.setId(bedId);
        bed.setStatus("OCCUPIED");

        CreatePatientRequest patientReq = new CreatePatientRequest(
            "John", "Doe", null, null, null, null, null, null, null, null, null, null, null, "{}",
            null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        
        CreateAdmissionRequest req = new CreateAdmissionRequest(patientReq, bedId, null);

        when(bedRepo.findById(bedId)).thenReturn(Optional.of(bed));

        assertThatThrownBy(() -> admissionService.admit(req, "nurse1"))
            .isInstanceOf(ConflictException.class)
            .hasMessageContaining("already occupied");

        verify(admissionRepo, never()).save(any());
    }

    @Test
    void discharge_sets_bed_status_to_available() {
        UUID admissionId = UUID.randomUUID();
        UUID bedId = UUID.randomUUID();

        Bed bed = new Bed();
        bed.setId(bedId);
        bed.setStatus("OCCUPIED");

        Patient patient = new Patient();
        patient.setId(UUID.randomUUID());

        Admission admission = new Admission();
        admission.setId(admissionId);
        admission.setPatient(patient);
        admission.setBed(bed);
        admission.setAdmittedAt(Instant.now().minusSeconds(3600));

        when(admissionRepo.findById(admissionId)).thenReturn(Optional.of(admission));
        when(admissionRepo.save(any(Admission.class))).thenReturn(admission);
        when(bedRepo.save(any(Bed.class))).thenReturn(bed);

        AdmissionDto result = admissionService.discharge(admissionId, "nurse1");

        assertThat(result).isNotNull();
        assertThat(bed.getStatus()).isEqualTo("AVAILABLE");
        assertThat(admission.getDischargedAt()).isNotNull();
        verify(bedRepo).save(bed);
    }

    @Test
    void discharge_throws_when_admission_not_found() {
        UUID id = UUID.randomUUID();
        when(admissionRepo.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> admissionService.discharge(id, "nurse1"))
            .isInstanceOf(AccessDeniedException.class);

        verify(bedRepo, never()).save(any());
    }

    @Test
    void discharge_throws_conflict_when_already_discharged() {
        UUID admissionId = UUID.randomUUID();

        Admission admission = new Admission();
        admission.setId(admissionId);
        admission.setDischargedAt(Instant.now());

        when(admissionRepo.findById(admissionId)).thenReturn(Optional.of(admission));

        assertThatThrownBy(() -> admissionService.discharge(admissionId, "nurse1"))
            .isInstanceOf(ConflictException.class)
            .hasMessageContaining("already discharged");
    }

    @Test
    void list_returns_all_admissions() {
        Admission admission = new Admission();
        admission.setId(UUID.randomUUID());
        Patient p = new Patient();
        p.setId(UUID.randomUUID());
        Bed b = new Bed();
        b.setId(UUID.randomUUID());
        admission.setPatient(p);
        admission.setBed(b);
        admission.setAdmittedAt(Instant.now());

        when(admissionRepo.findAll()).thenReturn(List.of(admission));

        List<AdmissionDto> result = admissionService.list();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).patientId()).isEqualTo(p.getId());
    }

    @Test
    void addVitals_success() {
        UUID id = UUID.randomUUID();
        Admission admission = new Admission();
        admission.setId(id);
        admission.setPatient(new Patient());
        admission.setBed(new Bed());
        admission.setVitals("[]");

        com.mednex.web.request.AddVitalsRequest req = new com.mednex.web.request.AddVitalsRequest(
            "120/80", "72", "98.6", "99"
        );

        when(admissionRepo.findById(id)).thenReturn(Optional.of(admission));
        when(admissionRepo.save(any(Admission.class))).thenAnswer(inv -> inv.getArgument(0));

        AdmissionDto result = admissionService.addVitals(id, req, "nurse1");

        assertThat(result.vitals()).contains("120/80");
        assertThat(result.vitals()).contains("nurse1");
        verify(admissionRepo).save(any(Admission.class));
    }

    @Test
    void discharge_success() {
        // Redundant but verifying logic as per user request
        UUID id = UUID.randomUUID();
        Admission admission = new Admission();
        admission.setId(id);
        admission.setPatient(new Patient());
        admission.setBed(new Bed());
        
        when(admissionRepo.findById(id)).thenReturn(Optional.of(admission));
        when(admissionRepo.save(any(Admission.class))).thenAnswer(inv -> inv.getArgument(0));
        when(bedRepo.save(any(Bed.class))).thenAnswer(inv -> inv.getArgument(0));

        AdmissionDto result = admissionService.discharge(id, "nurse1");

        assertThat(result.dischargedAt()).isNotNull();
        verify(bedRepo).save(any(Bed.class));
    }
}
