package com.mednex.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mednex.domain.Appointment;
import com.mednex.domain.Doctor;
import com.mednex.domain.Patient;
import com.mednex.repo.AppointmentRepository;
import com.mednex.repo.DoctorRepository;
import com.mednex.repo.PatientRepository;
import com.mednex.tenant.TenantContext;
import com.mednex.audit.AuditService;
import com.mednex.web.dto.AppointmentDto;
import com.mednex.web.request.CreateAppointmentRequest;

@Service
public class AppointmentService {
	private static final Logger log = LoggerFactory.getLogger(AppointmentService.class);
	private final AppointmentRepository appointmentRepo;
	private final PatientRepository patientRepo;
	private final DoctorRepository doctorRepo;
	private final EmailService emailService;
	private final AuditService auditService;

	public AppointmentService(AppointmentRepository appointmentRepo, PatientRepository patientRepo,
			DoctorRepository doctorRepo, EmailService emailService, AuditService auditService) {
		this.appointmentRepo = appointmentRepo;
		this.patientRepo = patientRepo;
		this.doctorRepo = doctorRepo;
		this.emailService = emailService;
		this.auditService = auditService;
	}

	@PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_DOCTOR','ROLE_NURSE')")
	@Transactional(readOnly = true)
	public List<AppointmentDto> list() {
		return appointmentRepo.findAll().stream()
				.map(a -> new AppointmentDto(a.getId(), a.getPatient().getId(), a.getDoctor().getId(),
						a.getStartTime(), a.getEndTime(), a.getStatus(), a.getCreatedAt()))
				.toList();
	}

	@PreAuthorize("hasAnyAuthority('ROLE_DOCTOR','ROLE_NURSE')")
	@Transactional(isolation = org.springframework.transaction.annotation.Isolation.SERIALIZABLE)
	public AppointmentDto book(CreateAppointmentRequest req) {
		// Use pessimistic locking to avoid race conditions
		List<Appointment> conflicts = appointmentRepo.findOverlappingWithLock(req.doctorId(), req.startTime(), req.endTime());
		if (!conflicts.isEmpty()) {
			throw new ConflictException("Time slot unavailable: Conflict detected with an existing appointment.");
		}

		Patient patient = patientRepo.findById(req.patientId())
				.orElseThrow(() -> {
					log.warn("SECURITY_PROBE tenantId={} patientId={}",
						TenantContext.getTenantId(), req.patientId());
					return new AccessDeniedException(
						"Access denied: resource not available in current tenant context"
					);
				});
		Doctor doctor = doctorRepo.findById(req.doctorId())
				.orElseThrow(() -> {
					log.warn("SECURITY_PROBE tenantId={} doctorId={}",
						TenantContext.getTenantId(), req.doctorId());
					return new AccessDeniedException(
						"Access denied: resource not available in current tenant context"
					);
				});

		Appointment appt = new Appointment();
		appt.setPatient(patient);
		appt.setDoctor(doctor);
		appt.setStartTime(req.startTime());
		appt.setEndTime(req.endTime());
		appt = appointmentRepo.save(appt);

		auditService.log("BOOK_APPOINTMENT", "Appointment", appt.getId().toString(),
				patient.getId(), null, "Scheduled session for " + patient.getFirstName());

		if (patient.getEmail() != null && !patient.getEmail().isBlank()) {
			emailService.sendAppointmentConfirmation(patient.getEmail(), "MedNex: Appointment Confirmed",
					"Your medical session with " + doctor.getFullName() + " is confirmed for "
							+ req.startTime().toString() + ".");
		}

		return new AppointmentDto(appt.getId(), patient.getId(), doctor.getId(), appt.getStartTime(), appt.getEndTime(),
				appt.getStatus(),
				appt.getCreatedAt());
	}
}
