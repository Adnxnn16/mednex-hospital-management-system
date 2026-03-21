package com.mednex.integration;

import com.mednex.config.TestSecurityConfig;
import com.mednex.domain.AuditLogEntry;
import com.mednex.domain.Bed;
import com.mednex.repo.AuditLogRepository;
import com.mednex.repo.BedRepository;
import com.mednex.tenant.TenantContext;
import com.mednex.tenant.TenantFilter;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class AuditLogIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired BedRepository bedRepository;
    @Autowired AuditLogRepository auditLogRepository;
    @Autowired TenantFilter tenantFilter;
    @Autowired org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId("hospital_a");
        try {
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS hospital_a.beds AS SELECT * FROM public.beds WHERE 1=0");
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS hospital_a.patients AS SELECT * FROM public.patients WHERE 1=0");
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS hospital_a.admissions AS SELECT * FROM public.admissions WHERE 1=0");
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS hospital_a.audit_log AS SELECT * FROM public.audit_log WHERE 1=0");
        } catch (Exception ignored) {}
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        TenantContext.clear();
    }

    @Test
    void patientAdmission_shouldCreateAuditLogEntry() throws Exception {
        Bed bed = new Bed();
        bed.setWard("ICU");
        bed.setRoom("101");
        bed.setBedNumber("A1");
        bed.setStatus("AVAILABLE");
        UUID bedId = bedRepository.save(bed).getId();

        String body = """
            {
              "patient": {
                "firstName": "John",
                "lastName": "Doe",
                "medicalHistory": "{}"
              },
              "bedId": "%s",
              "notes": "admit"
            }
            """.formatted(bedId);

        mvc.perform(post("/api/admissions")
                .with(user("nurse").roles("NURSE"))
                .with(csrf())
                .header("X-Tenant", "hospital_a")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk());

        Thread.sleep(300);
        List<AuditLogEntry> admitLogs = auditLogRepository.findAll().stream()
            .filter(e -> "ADMIT".equals(e.getAction()))
            .toList();

        assertThat(admitLogs).isNotEmpty();
        AuditLogEntry latest = admitLogs.stream().max(Comparator.comparing(AuditLogEntry::getTimestamp)).orElseThrow();
        assertThat(latest.getUserId()).isNotBlank();
        assertThat(latest.getTenantId()).isEqualTo("hospital_a");
        assertThat(latest.getEntityType()).isEqualTo("Patient");
    }

    @Test
    void crossTenantAttempt_shouldLogSecurityViolation() throws Exception {
        Jwt jwt = Jwt.withTokenValue("token")
            .header("alg", "none")
            .claim("sub", "doctor-a")
            .claim("tenant", "hospital_a")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(600))
            .build();
        JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_DOCTOR")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/patients");
        request.addHeader(TenantFilter.TENANT_HEADER, "hospital_b");
        request.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> {};

        tenantFilter.doFilter(request, response, chain);

        Thread.sleep(300);
        List<AuditLogEntry> violations = auditLogRepository.findAll().stream()
            .filter(e -> "SECURITY_VIOLATION".equals(e.getAction()))
            .toList();

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(violations).isNotEmpty();
    }
}
