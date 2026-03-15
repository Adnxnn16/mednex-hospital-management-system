package com.mednex.web;

import java.util.List;
import java.util.UUID;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.mednex.service.PatientService;
import com.mednex.service.PdfExportService;
import com.mednex.service.NotFoundException;
import com.mednex.repo.PatientRepository;
import com.mednex.domain.Patient;
import com.mednex.web.dto.PatientDto;
import com.mednex.web.request.CreatePatientRequest;
import com.mednex.web.request.AddConsultationRequest;

@RestController
@RequestMapping("/api/patients")
public class PatientController {
	private final PatientService service;
	private final PdfExportService pdfService;
	private final PatientRepository patientRepo;

	public PatientController(PatientService service, PdfExportService pdfService, PatientRepository patientRepo) {
		this.service = service;
		this.pdfService = pdfService;
		this.patientRepo = patientRepo;
	}

	@GetMapping
	@PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_DOCTOR','ROLE_NURSE')")
	public List<PatientDto> list() {
		return service.list();
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_DOCTOR','ROLE_NURSE')")
	public PatientDto get(@PathVariable UUID id) {
		return service.get(id);
	}

	@PostMapping
	@PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_NURSE')")
	public PatientDto create(@Valid @RequestBody CreatePatientRequest req) {
		return service.create(req);
	}

	@PostMapping("/{id}/consultations")
	@PreAuthorize("hasAuthority('ROLE_DOCTOR')")
	public PatientDto addConsultation(@PathVariable UUID id, @Valid @RequestBody AddConsultationRequest req) {
		return service.addConsultation(id, req);
	}

	@GetMapping("/{id}/export")
	@PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_DOCTOR')")
	public ResponseEntity<byte[]> export(@PathVariable UUID id) {
		Patient patient = patientRepo.findById(id)
			.orElseThrow(() -> new NotFoundException("Patient not found"));
		
		byte[] pdfBytes = pdfService.exportPatientRecord(patient);
		
		return ResponseEntity.ok()
			.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"patient_record_" + id + ".pdf\"")
			.contentType(MediaType.APPLICATION_PDF)
			.body(pdfBytes);
	}
}
