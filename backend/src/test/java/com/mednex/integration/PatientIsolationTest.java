package com.mednex.integration;

import com.mednex.domain.Patient;
import com.mednex.repo.PatientRepository;
import com.mednex.tenant.TenantContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class PatientIsolationTest {

    @Autowired
    private PatientRepository patientRepo;

    @Test
    void testCrossTenantIsolation() {
        // Create patient in Tenant A
        TenantContext.setTenantId("tenant-a");
        Patient p1 = new Patient();
        p1.setFirstName("TenantA");
        p1.setLastName("Patient");
        p1 = patientRepo.save(p1);
        UUID p1Id = p1.getId();

        // Try to access from Tenant B
        TenantContext.setTenantId("tenant-b");
        Optional<Patient> foundAsB = patientRepo.findById(p1Id);
        assertThat(foundAsB).isEmpty();

        // Verify still accessible in Tenant A
        TenantContext.setTenantId("tenant-a");
        Optional<Patient> foundAsA = patientRepo.findById(p1Id);
        assertThat(foundAsA).isPresent();
    }
}
