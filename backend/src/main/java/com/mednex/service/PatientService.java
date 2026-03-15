package com.mednex.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mednex.domain.Patient;
import com.mednex.repo.PatientRepository;
import com.mednex.web.dto.PatientDto;
import com.mednex.web.request.CreatePatientRequest;

@Service
public class PatientService {
	private final PatientRepository repo;
	private final com.mednex.validation.MedicalHistoryValidator validator;

	public PatientService(PatientRepository repo, com.mednex.validation.MedicalHistoryValidator validator) {
		this.repo = repo;
		this.validator = validator;
	}

	@Transactional(readOnly = true)
	public List<PatientDto> list() {
		return repo.findAll().stream().map(PatientService::toDto).toList();
	}

	@Transactional(readOnly = true)
	public PatientDto get(UUID id) {
		Patient p = repo.findById(id).orElseThrow(() -> new NotFoundException("Patient not found"));
		return toDto(p);
	}

	@Transactional
	public PatientDto create(CreatePatientRequest req) {
		Patient p = new Patient();
		p.setFirstName(req.firstName());
		p.setLastName(req.lastName());
		p.setDob(req.dob());
		p.setGender(req.gender());
		p.setEmail(req.email());
		p.setPhone(req.phone());
		p.setAddress(req.address());
		p.setBloodGroup(req.bloodGroup());
		p.setOccupation(req.occupation());
		p.setEmergencyContactName(req.emergencyContactName());
		p.setEmergencyContactPhone(req.emergencyContactPhone());
		p.setInsuranceProvider(req.insuranceProvider());
		p.setPolicyNumber(req.policyNumber());
		
		String history = req.medicalHistory();
		if (history == null || history.isBlank() || history.equals("{}")) {
			history = "{\"version\":\"1.0\",\"diagnosis\":\"New patient registration\"}";
		} else {
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
		return toDto(repo.save(p));
	}

	@Transactional
	public PatientDto addConsultation(UUID id, com.mednex.web.request.AddConsultationRequest req) {
		Patient p = repo.findById(id).orElseThrow(() -> new NotFoundException("Patient not found"));
		try {
			com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
			String currentHistory = p.getMedicalHistory();
			if (currentHistory == null || currentHistory.isBlank()) {
				currentHistory = "{}";
			}
			com.fasterxml.jackson.databind.node.ObjectNode root = (com.fasterxml.jackson.databind.node.ObjectNode) mapper.readTree(currentHistory);
			
			com.fasterxml.jackson.databind.node.ArrayNode consultations;
			if (root.has("consultations") && root.get("consultations").isArray()) {
				consultations = (com.fasterxml.jackson.databind.node.ArrayNode) root.get("consultations");
			} else {
				consultations = root.putArray("consultations");
			}
			
			com.fasterxml.jackson.databind.node.ObjectNode newConsultation = mapper.createObjectNode();
			newConsultation.put("date", req.date());
			newConsultation.put("doctorName", req.doctorName());
			newConsultation.put("symptoms", req.symptoms());
			newConsultation.put("diagnosis", req.diagnosis());
			newConsultation.put("treatment", req.treatment());
			newConsultation.put("notes", req.notes());
			newConsultation.put("id", UUID.randomUUID().toString());
			
			consultations.add(newConsultation);
			p.setMedicalHistory(mapper.writeValueAsString(root));
			return toDto(repo.save(p));
		} catch (Exception e) {
			throw new RuntimeException("Failed to update medical history", e);
		}
	}

	static PatientDto toDto(Patient p) {
		return new PatientDto(
				p.getId(), p.getFirstName(), p.getLastName(), p.getDob(), p.getGender(), 
				p.getEmail(), p.getPhone(), p.getAddress(), p.getBloodGroup(), p.getOccupation(),
				p.getEmergencyContactName(), p.getEmergencyContactPhone(), p.getInsuranceProvider(),
				p.getPolicyNumber(), p.getMedicalHistory(), p.getCreatedAt(), p.getUpdatedAt());
	}
}
