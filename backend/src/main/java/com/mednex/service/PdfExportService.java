package com.mednex.service;

import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.mednex.domain.Patient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

@Service
public class PdfExportService {

    @Value("${app.pdf.encryption-key:change_me}")
    private String encryptionKey;

    public byte[] exportPatientRecord(Patient patient) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            WriterProperties writerProperties = new WriterProperties();
            
            // AES-256 Encryption
            byte[] userPass = encryptionKey.getBytes(StandardCharsets.UTF_8);
            writerProperties.setStandardEncryption(
                userPass, 
                userPass, 
                EncryptionConstants.ALLOW_PRINTING, 
                EncryptionConstants.ENCRYPTION_AES_256
            );

            PdfWriter writer = new PdfWriter(baos, writerProperties);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("Patient Medical Record").setBold().setFontSize(18));
            document.add(new Paragraph("Full Name: " + patient.getFirstName() + " " + patient.getLastName()));
            document.add(new Paragraph("Date of Birth: " + patient.getDob()));
            document.add(new Paragraph("Gender: " + patient.getGender()));
            document.add(new Paragraph("Email: " + patient.getEmail()));
            document.add(new Paragraph("Phone: " + patient.getPhone()));
            document.add(new Paragraph("Address: " + patient.getAddress()));
            document.add(new Paragraph("\nMedical History:"));
            document.add(new Paragraph(patient.getMedicalHistory()));

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate encrypted PDF", e);
        }
    }
}
