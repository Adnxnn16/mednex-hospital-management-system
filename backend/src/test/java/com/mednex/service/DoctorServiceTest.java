package com.mednex.service;

import com.mednex.domain.Doctor;
import com.mednex.repo.DoctorRepository;
import com.mednex.tenant.TenantContext;
import com.mednex.web.dto.DoctorDto;
import com.mednex.web.request.CreateDoctorRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {

    @Mock DoctorRepository repo;
    @InjectMocks DoctorService doctorService;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId("hospital_a");
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void list_returns_all_doctors() {
        Doctor d1 = buildDoctor(UUID.randomUUID(), "John", "Adams", "Cardiology", "LIC-001");
        Doctor d2 = buildDoctor(UUID.randomUUID(), "Jane", "Baker", "Neurology", "LIC-002");

        when(repo.findAll()).thenReturn(List.of(d1, d2));

        List<DoctorDto> result = doctorService.list();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).firstName()).isEqualTo("John");
        assertThat(result.get(0).lastName()).isEqualTo("Adams");
        assertThat(result.get(0).specialisation()).isEqualTo("Cardiology");
    }

    @Test
    void getEntity_returns_doctor_when_exists() {
        UUID id = UUID.randomUUID();
        Doctor doctor = buildDoctor(id, "John", "Smith", "Ortho", "LIC-003");

        when(repo.findById(id)).thenReturn(Optional.of(doctor));

        Doctor result = doctorService.getEntity(id);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Smith");
    }

    @Test
    void getEntity_throws_AccessDenied_when_not_found() {
        UUID id = UUID.randomUUID();
        when(repo.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorService.getEntity(id))
            .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void create_saves_and_returns_dto() {
        CreateDoctorRequest req = new CreateDoctorRequest("Gregory", "House", "Diagnostics", "LIC-007", "house@test.com");

        when(repo.save(any(Doctor.class))).thenAnswer(inv -> {
            Doctor d = inv.getArgument(0);
            d.setId(UUID.randomUUID());
            d.setCreatedAt(Instant.now());
            return d;
        });

        DoctorDto result = doctorService.create(req);

        assertThat(result).isNotNull();
        assertThat(result.firstName()).isEqualTo("Gregory");
        assertThat(result.lastName()).isEqualTo("House");
        assertThat(result.specialisation()).isEqualTo("Diagnostics");
        assertThat(result.licenceNumber()).isEqualTo("LIC-007");
        verify(repo).save(any(Doctor.class));
    }

    private Doctor buildDoctor(UUID id, String firstName, String lastName, String specialisation, String licenceNumber) {
        Doctor d = new Doctor();
        d.setId(id);
        d.setFirstName(firstName);
        d.setLastName(lastName);
        d.setSpecialisation(specialisation);
        d.setLicenceNumber(licenceNumber);
        d.setTenantId("hospital_a");
        d.setEmail(firstName.toLowerCase() + "." + lastName.toLowerCase() + "@hospital.com");
        d.setCreatedAt(Instant.now());
        return d;
    }
}
