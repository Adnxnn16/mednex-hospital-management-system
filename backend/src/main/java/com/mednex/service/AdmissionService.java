package com.mednex.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mednex.audit.AuditService;
import com.mednex.domain.Admission;
import com.mednex.domain.Bed;
import com.mednex.domain.Patient;
import com.mednex.repo.AdmissionRepository;
import com.mednex.repo.BedRepository;
import com.mednex.repo.PatientRepository;
import com.mednex.web.dto.AdmissionDto;
import com.mednex.web.request.CreateAdmissionRequest;

@Service
public class AdmissionService {
	private final PatientRepository patientRepo;
	private final BedRepository bedRepo;
	private final AdmissionRepository admissionRepo;
	private final AuditService auditService;
	private final com.mednex.validation.MedicalHistoryValidator validator;

	public AdmissionService(PatientRepository patientRepo, BedRepository bedRepo, 
			AdmissionRepository admissionRepo, AuditService auditService, com.mednex.validation.MedicalHistoryValidator validator) {
		this.patientRepo = patientRepo;
		this.bedRepo = bedRepo;
		this.admissionRepo = admissionRepo;
		this.auditService = auditService;
		this.validator = validator;
	}

	public List<AdmissionDto> list() {
		return admissionRepo.findAll().stream()
				.map(a -> new AdmissionDto(
						a.getId(), 
						a.getPatient().getId(), 
						a.getBed().getId(),
						a.getAdmittedAt(), 
						a.getDischargedAt(), 
						a.getNotes(), 
						a.getVitals()))
				.collect(Collectors.toList());
	}

	@Transactional
	public AdmissionDto admit(CreateAdmissionRequest req, String createdBy) {
		Bed bed = bedRepo.findById(req.bedId()).orElseThrow(() -> new NotFoundException("Bed not found"));
		if ("OCCUPIED".equalsIgnoreCase(bed.getStatus())) {
			throw new ConflictException("Bed is already occupied");
		}

		Patient p = new Patient();
		p.setFirstName(req.patient().firstName());
		p.setLastName(req.patient().lastName());
		p.setDob(req.patient().dob());
		p.setGender(req.patient().gender());
		p.setEmail(req.patient().email());
		p.setPhone(req.patient().phone());
		p.setAddress(req.patient().address());
		p.setBloodGroup(req.patient().bloodGroup());
		p.setOccupation(req.patient().occupation());
		p.setEmergencyContactName(req.patient().emergencyContactName());
		p.setEmergencyContactPhone(req.patient().emergencyContactPhone());
		p.setInsuranceProvider(req.patient().insuranceProvider());
		p.setPolicyNumber(req.patient().policyNumber());
		
		String history = req.patient().medicalHistory();
		if (history == null || history.isBlank() || history.equals("{}")) {
			history = "{\"version\":\"1.0\",\"diagnosis\":\"Initial admission\"}";
		} else {
			// Ensure version is present for EMR-03
			try {
				com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
				com.fasterxml.jackson.databind.node.ObjectNode nodes = (com.fasterxml.jackson.databind.node.ObjectNode) mapper.readTree(history);
				if (!nodes.has("version")) {
					nodes.put("version", "1.0");
				}
				history = mapper.writeValueAsString(nodes);
			} catch (Exception ignored) {}
		}
		
		validator.validate(history);
		p.setMedicalHistory(history);
		
		Patient savedPatient = patientRepo.save(p);
		p = savedPatient;

		bed.setStatus("OCCUPIED");
		Bed savedBed = bedRepo.save(bed);
		bed = savedBed;

		Admission admission = new Admission();
		admission.setPatient(p);
		admission.setBed(bed);
		admission.setNotes(req.notes());
		admission.setCreatedBy(createdBy);
		Admission savedAdmission = admissionRepo.save(admission);
		admission = savedAdmission;

		auditService.log("ADMIT_PATIENT", "Admission", admission.getId().toString(), createdBy);

		return new AdmissionDto(admission.getId(), p.getId(), bed.getId(), admission.getAdmittedAt(), admission.getDischargedAt(),
				admission.getNotes(), admission.getVitals());
	}

	@Transactional
	public AdmissionDto discharge(UUID id, String dischargedBy) {
		Admission admission = admissionRepo.findById(id)
				.orElseThrow(() -> new NotFoundException("Admission not found"));
		
		if (admission.getDischargedAt() != null) {
			throw new ConflictException("Patient is already discharged");
		}

		admission.setDischargedAt(java.time.Instant.now());
		Admission savedAdmission2 = admissionRepo.save(admission);
		admission = savedAdmission2;

		Bed bed = admission.getBed();
		bed.setStatus("AVAILABLE");
		Bed savedBed2 = bedRepo.save(bed);
		bed = savedBed2;

		auditService.log("DISCHARGE_PATIENT", "Admission", admission.getId().toString(), dischargedBy);

		return new AdmissionDto(admission.getId(), admission.getPatient().getId(), bed.getId(),
				admission.getAdmittedAt(), admission.getDischargedAt(), admission.getNotes(), admission.getVitals());
	}

	@Transactional
	public AdmissionDto addVitals(UUID id, com.mednex.web.request.AddVitalsRequest req, String addedBy) {
		Admission admission = admissionRepo.findById(id)
				.orElseThrow(() -> new NotFoundException("Admission not found"));
		
		try {
			com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
			String currentVitals = admission.getVitals();
			if (currentVitals == null || currentVitals.isBlank()) {
				currentVitals = "[]";
			}
			com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(currentVitals);
			com.fasterxml.jackson.databind.node.ArrayNode vitalsArray;
			if (root.isArray()) {
				vitalsArray = (com.fasterxml.jackson.databind.node.ArrayNode) root;
			} else {
				vitalsArray = mapper.createArrayNode();
			}
			
			com.fasterxml.jackson.databind.node.ObjectNode newVitals = mapper.createObjectNode();
			newVitals.put("timestamp", java.time.Instant.now().toString());
			newVitals.put("bloodPressure", req.bloodPressure());
			newVitals.put("heartRate", req.heartRate());
			newVitals.put("temperature", req.temperature());
			newVitals.put("oxygenLevel", req.oxygenLevel());
			newVitals.put("recordedBy", addedBy);
			
			vitalsArray.add(newVitals);
			admission.setVitals(mapper.writeValueAsString(vitalsArray));
			Admission savedAdmission3 = admissionRepo.save(admission);
			admission = savedAdmission3;
			
			auditService.log("ADD_VITALS", "Admission", admission.getId().toString(), addedBy);
			
			return new AdmissionDto(admission.getId(), admission.getPatient().getId(), admission.getBed().getId(),
					admission.getAdmittedAt(), admission.getDischargedAt(), admission.getNotes(), admission.getVitals());
		} catch (Exception e) {
			throw new RuntimeException("Failed to save vitals", e);
		}
	}
}
