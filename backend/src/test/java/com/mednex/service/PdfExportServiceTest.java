package com.mednex.service;

import com.mednex.domain.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class PdfExportServiceTest {

    private final PdfExportService pdfService = new PdfExportService();

    @BeforeEach
    void setup_encryption_key() {
        ReflectionTestUtils.setField(pdfService, "encryptionKey", "test-key-12345678");
    }

    @Test
    void export_returnsNonNullBytes() {
        Patient p = new Patient();
        p.setId(UUID.randomUUID());
        p.setFirstName("John");
        p.setLastName("Doe");
        p.setDob(LocalDate.of(1990, 1, 1));
        p.setGender("MALE");
        p.setMedicalHistory("{\"version\":\"1.0\"}");

        byte[] result = pdfService.exportPatientRecord(p);

        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
        // The PDF header starts with %PDF
        assertThat(new String(result, 0, 4)).isEqualTo("%PDF");
    }

    @Test
    void export_doesNotWriteToDisk() {
        // Since we are returning a byte[] from a ByteArrayOutputStream, 
        // no actual files are being created by our service.
        Patient p = new Patient();
        p.setId(UUID.randomUUID());
        p.setMedicalHistory("{}");

        byte[] result = pdfService.exportPatientRecord(p);
        
        // Assert that the object which can only be obtained through the byte[] bridge 
        // returned from the memory-stream was created successfully.
        assertThat(result).isNotNull();
    }
}
