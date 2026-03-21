package com.mednex.integration;

import com.mednex.domain.Patient;
import com.mednex.repo.PatientRepository;
import com.mednex.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import org.springframework.jdbc.core.JdbcTemplate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.test.context.ActiveProfiles("test")
class PatientIsolationTest {

    @Autowired MockMvc mockMvc;
    @Autowired PatientRepository patientRepository;
    @Autowired JdbcTemplate jdbcTemplate;

    private UUID savedPatientId;

    @BeforeEach
    void setUp() {
        // H2 in tests starts with only public schema having tables.
        // We use INIT to create schemas, but we need to create the table structure in them.
        try {
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS hospital_a.patients AS SELECT * FROM public.patients WHERE 1=0");
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS hospital_b.patients AS SELECT * FROM public.patients WHERE 1=0");
        } catch (Exception ignored) {}

        // Step 1: set tenant to hospital_a
        TenantContext.setTenantId("hospital_a");

        // Step 2: save a patient in hospital_a schema
        Patient p = new Patient();
        p.setFirstName("IsolationTest");
        p.setLastName("Patient");
        Patient saved = patientRepository.save(p);
        savedPatientId = saved.getId();

        // Step 3: CRITICAL — clear context after save
        // Without this, Hibernate reuses the same schema
        // connection for the next operation
        TenantContext.clear();
    }

    @AfterEach
    void tearDown() {
        // Clean up so test data doesn't bleed into other tests
        TenantContext.setTenantId("hospital_a");
        try {
            patientRepository.deleteById(savedPatientId);
        } catch (Exception ignored) {}
        TenantContext.clear();
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void crossTenantIdGuess_returns403_notHospitalA() throws Exception {
        // Step 1: Ensure a patient exists in hospital_a
        TenantContext.setTenantId("hospital_a");
        Patient p = new Patient();
        p.setFirstName("HospitalA");
        p.setLastName("Patient");
        UUID hospitalAPatientId = patientRepository.save(p).getId();
        TenantContext.clear();

        // Step 2: Request that same ID as a hospital_b user
        // We use X-Tenant header as defined in TenantFilter.TENANT_HEADER
        mockMvc.perform(get("/api/patients/" + hospitalAPatientId)
            .header("X-Tenant", "hospital_b"))
            .andExpect(status().isForbidden())                    // 403
            .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    void testCrossTenantIsolation() {
        // Switch to hospital_b
        TenantContext.setTenantId("hospital_b");

        // Try to find hospital_a's patient from hospital_b
        Optional<Patient> result =
            patientRepository.findById(savedPatientId);

        // Must be empty — schemas are isolated
        assertThat(result)
            .as("hospital_b must NOT see hospital_a patient "
                + savedPatientId)
            .isEmpty();

        TenantContext.clear();
    }
}
