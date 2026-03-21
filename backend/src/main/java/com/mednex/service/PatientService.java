package com.mednex.service;

import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mednex.domain.Patient;
import com.mednex.repo.PatientRepository;
import com.mednex.tenant.TenantContext;
import com.mednex.web.dto.PatientDto;
import com.mednex.web.request.CreatePatientRequest;
import com.mednex.audit.AuditService;

@Service
public class PatientService {
	private static final Logger log = LoggerFactory.getLogger(PatientService.class);
	private final PatientRepository repo;
	private final com.mednex.validation.MedicalHistoryValidator validator;
	private final AuditService auditService;

	public PatientService(PatientRepository repo, com.mednex.validation.MedicalHistoryValidator validator, AuditService auditService) {
		this.repo = repo;
		this.validator = validator;
		this.auditService = auditService;
	}

	@PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_DOCTOR','ROLE_NURSE')")
	@Transactional(readOnly = true)
	public List<PatientDto> list() {
		return repo.findAll().stream().map(PatientService::toDto).toList();
	}

	@PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_DOCTOR','ROLE_NURSE')")
	@Transactional(readOnly = true)
	public PatientDto get(UUID id) {
		Patient p = repo.findById(id).orElseThrow(() -> {
			log.warn("SECURITY_PROBE tenantId={} patientId={}",
				TenantContext.getTenantId(), id);
			return new AccessDeniedException(
				"Access denied: resource not available in current tenant context"
			);
		});
		auditService.log("READ_PATIENT", "Patient", p.getId().toString(), p.getId(), null, "{}");
		return toDto(p);
	}

	@PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_NURSE')")
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

		// New fields mapping
		p.setMiddleName(req.middleName());
		p.setSuffix(req.suffix());
		p.setPreferredName(req.preferredName());
		p.setSsn(req.ssn());
		p.setNationality(req.nationality());
		p.setPrimaryLanguage(req.primaryLanguage());
		p.setReligion(req.religion());
		p.setMaritalStatus(req.maritalStatus());
		p.setWeight(req.weight());
		p.setHeight(req.height());
		p.setBmi(req.bmi());
		p.setTobaccoUse(req.tobaccoUse());
		p.setAlcoholUse(req.alcoholUse());
		p.setDrugUse(req.drugUse());
		p.setExerciseFrequency(req.exerciseFrequency());
		p.setDietaryPreference(req.dietaryPreference());
		p.setOrganDonor(req.organDonor());
		p.setAdvancedDirective(req.advancedDirective());
		p.setPreferredPharmacy(req.preferredPharmacy());
		p.setPharmacyPhone(req.pharmacyPhone());
		p.setEmployerName(req.employerName());
		p.setEmployerPhone(req.employerPhone());
		p.setEmployerAddress(req.employerAddress());
		p.setEmergencyContactRelation(req.emergencyContactRelation());
		p.setEmergencyContactEmail(req.emergencyContactEmail());
		p.setEmergencyContactAddress(req.emergencyContactAddress());
		p.setPrimaryPhysicianName(req.primaryPhysicianName());
		p.setPrimaryPhysicianPhone(req.primaryPhysicianPhone());
		p.setReferringPhysicianName(req.referringPhysicianName());
		p.setReasonForAdmission(req.reasonForAdmission());
		p.setKnownAllergies(req.knownAllergies());
		p.setPastMedicalConditions(req.pastMedicalConditions());
		p.setPastSurgeries(req.pastSurgeries());
		p.setCurrentMedications(req.currentMedications());
		p.setFamilyMedicalHistory(req.familyMedicalHistory());
		p.setComments(req.comments());
		
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

	@PreAuthorize("hasAuthority('ROLE_DOCTOR')")
	@Transactional
	public PatientDto addConsultation(UUID id, com.mednex.web.request.AddConsultationRequest req) {
		Patient p = repo.findById(id).orElseThrow(() -> {
			log.warn("SECURITY_PROBE tenantId={} patientId={}",
				TenantContext.getTenantId(), id);
			return new AccessDeniedException(
				"Access denied: resource not available in current tenant context"
			);
		});
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
			Patient saved = repo.save(p);
			auditService.log("UPDATE_PATIENT", "Patient", saved.getId().toString(), saved.getId(), null, "{}");
			return toDto(saved);
		} catch (Exception e) {
			throw new RuntimeException("Failed to update medical history", e);
		}
	}

	static PatientDto toDto(Patient p) {
		return new PatientDto(
				p.getId(), p.getFirstName(), p.getLastName(), p.getDob(), p.getGender(), 
				p.getEmail(), p.getPhone(), p.getAddress(), p.getBloodGroup(), p.getOccupation(),
				p.getEmergencyContactName(), p.getEmergencyContactPhone(), p.getInsuranceProvider(),
				p.getPolicyNumber(), p.getMedicalHistory(), p.getCreatedAt(), p.getUpdatedAt(),
				p.getMiddleName(), p.getSuffix(), p.getPreferredName(), p.getSsn(), p.getNationality(),
				p.getPrimaryLanguage(), p.getReligion(), p.getMaritalStatus(), p.getWeight(), p.getHeight(),
				p.getBmi(), p.getTobaccoUse(), p.getAlcoholUse(), p.getDrugUse(), p.getExerciseFrequency(),
				p.getDietaryPreference(), p.getOrganDonor(), p.getAdvancedDirective(), p.getPreferredPharmacy(),
				p.getPharmacyPhone(), p.getEmployerName(), p.getEmployerPhone(), p.getEmployerAddress(),
				p.getEmergencyContactRelation(), p.getEmergencyContactEmail(), p.getEmergencyContactAddress(),
				p.getPrimaryPhysicianName(), p.getPrimaryPhysicianPhone(), p.getReferringPhysicianName(),
				p.getReasonForAdmission(), p.getKnownAllergies(), p.getPastMedicalConditions(), p.getPastSurgeries(),
				p.getCurrentMedications(), p.getFamilyMedicalHistory(), p.getComments());
	}
}
