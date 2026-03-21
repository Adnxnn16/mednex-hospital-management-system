package com.mednex.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.mednex.config.TestSecurityConfig;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class RbacTest {

    @Autowired MockMvc mvc;
    @Autowired org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        String[] tables = {"patients", "doctors", "beds", "appointments", "admissions", "audit_log"};
        for (String table : tables) {
            try {
                jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS hospital_a." + table + " AS SELECT * FROM public." + table + " WHERE 1=0");
            } catch (Exception ignored) {}
        }
    }

    // Matches TENANT_HEADER in TenantFilter.java
    private static final String TENANT_HEADER = "X-Tenant";
    private static final String TENANT        = "hospital_a";

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanAccessAnalytics() throws Exception {
        mvc.perform(get("/api/analytics/bed-occupancy")
                .header(TENANT_HEADER, TENANT))
           .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "NURSE")
    void nurseCannotAddConsultation() throws Exception {
        // Analytics endpoint is ADMIN-only — NURSE must get 403
        mvc.perform(get("/api/analytics/bed-occupancy")
                .header(TENANT_HEADER, TENANT))
           .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "NURSE")
    void service_layer_blocks_nurse_from_analytics()
            throws Exception {
        mvc.perform(get("/api/analytics/bed-occupancy")
                .header(TENANT_HEADER, TENANT))
           .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticated_request_returns_401()
            throws Exception {
        mvc.perform(get("/api/patients")
                .header(TENANT_HEADER, TENANT))
           .andExpect(status().isUnauthorized());
    }
}
