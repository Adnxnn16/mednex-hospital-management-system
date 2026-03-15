package com.mednex.integration;

import com.mednex.domain.Doctor;
import com.mednex.domain.Patient;
import com.mednex.repo.DoctorRepository;
import com.mednex.repo.PatientRepository;
import com.mednex.service.AppointmentService;
import com.mednex.web.request.CreateAppointmentRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class AppointmentConcurrencyTest {

    @Autowired
    private AppointmentService appointmentService;
    
    @Autowired
    private PatientRepository patientRepo;
    
    @Autowired
    private DoctorRepository doctorRepo;

    @Test
    void testConcurrentBookingConflict() throws InterruptedException {
        Patient p = new Patient();
        p.setFirstName("C"); p.setLastName("P");
        p = patientRepo.save(p);
        
        Doctor d = new Doctor();
        d.setFullName("Concurrent Doctor");
        d.setSpecialty("General");
        d = doctorRepo.save(d);
        
        UUID patientId = p.getId();
        UUID doctorId = d.getId();
        OffsetDateTime start = OffsetDateTime.now().plusDays(1);
        OffsetDateTime end = start.plusHours(1);

        int threads = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    latch.await();
                    appointmentService.book(new CreateAppointmentRequest(patientId, doctorId, start, end));
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                }
            });
        }

        latch.countDown();
        Thread.sleep(2000);
        executor.shutdown();

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failureCount.get()).isEqualTo(threads - 1);
    }
}
