package com.mednex.integration;

import com.mednex.domain.Doctor;
import com.mednex.domain.Patient;
import com.mednex.web.request.CreateAppointmentRequest;
import com.mednex.repo.DoctorRepository;
import com.mednex.repo.PatientRepository;
import com.mednex.service.AppointmentService;
import com.mednex.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import com.mednex.config.TestSecurityConfig;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class AppointmentConcurrencyTest {

    @Autowired AppointmentService appointmentService;
    @Autowired DoctorRepository   doctorRepository;
    @Autowired PatientRepository  patientRepository;
    @Autowired org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    private UUID doctorId;
    private UUID patientId;

    // Far-future date — never conflicts with real data
    private static final OffsetDateTime SLOT_START =
        OffsetDateTime.of(2099, 12, 1, 10, 0, 0, 0, ZoneOffset.UTC);
    private static final OffsetDateTime SLOT_END =
        OffsetDateTime.of(2099, 12, 1, 10, 30, 0, 0, ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        // Initialize schema for H2
        try {
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS hospital_a.doctors AS SELECT * FROM public.doctors WHERE 1=0");
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS hospital_a.patients AS SELECT * FROM public.patients WHERE 1=0");
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS hospital_a.appointments AS SELECT * FROM public.appointments WHERE 1=0");
            jdbcTemplate.execute("ALTER TABLE hospital_a.appointments ADD CONSTRAINT unique_appt UNIQUE (doctor_id, start_time)");
        } catch (Exception ignored) {}

        TenantContext.setTenantId("hospital_a");

        Doctor doctor = new Doctor();
        doctor.setFirstName("Concurrent");
        doctor.setLastName("Doctor");
        doctor.setSpecialisation("General");
        doctor.setLicenceNumber("LIC-CONC");
        doctor.setTenantId("hospital_a");
        doctorId = doctorRepository.save(doctor).getId();

        Patient patient = new Patient();
        patient.setFirstName("Concurrent");
        patient.setLastName("Patient");
        patientId = patientRepository.save(patient).getId();

        TenantContext.clear();
    }

    @AfterEach
    void tearDown() {
        TenantContext.setTenantId("hospital_a");
        try { doctorRepository.deleteById(doctorId); }
        catch (Exception ignored) {}
        try { patientRepository.deleteById(patientId); }
        catch (Exception ignored) {}
        TenantContext.clear();
    }

    @Test
    void testConcurrentBookingConflict()
            throws InterruptedException {

        int threadCount = 50;
        AtomicInteger successCount  = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch  = new CountDownLatch(threadCount);
        ExecutorService pool =
            Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            pool.submit(() -> {
                try {
                    startLatch.await();

                    // 1. Set tenant on this thread
                    TenantContext.setTenantId("hospital_a");

                    // 2. Set security context on this thread
                    SecurityContext ctx =
                        SecurityContextHolder.createEmptyContext();
                    ctx.setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                            "test-doctor",
                            null,
                            List.of(new SimpleGrantedAuthority(
                                "ROLE_DOCTOR"))
                        )
                    );
                    SecurityContextHolder.setContext(ctx);

                    // 3. Build the request (constructor for record)
                    CreateAppointmentRequest req =
                        new CreateAppointmentRequest(patientId, doctorId, SLOT_START, SLOT_END);

                    // 4. Call service book()
                    appointmentService.book(req);
                    successCount.incrementAndGet();

                } catch (Exception e) {
                    conflictCount.incrementAndGet();
                } finally {
                    TenantContext.clear();
                    SecurityContextHolder.clearContext();
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // fire all 50 at once
        boolean finished = doneLatch.await(30, TimeUnit.SECONDS);
        pool.shutdown();

        assertThat(finished)
            .as("All 50 threads must complete within 30s")
            .isTrue();

        assertThat(successCount.get())
            .as("Exactly 1 booking must succeed")
            .isEqualTo(1);

        assertThat(conflictCount.get())
            .as("49 must be rejected as conflicts")
            .isEqualTo(49);
    }
}
