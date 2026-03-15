package com.mednex.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mednex.domain.Doctor;
import com.mednex.repo.DoctorRepository;
import com.mednex.web.dto.DoctorDto;
import com.mednex.web.request.CreateDoctorRequest;

@Service
public class DoctorService {
	private final DoctorRepository repo;

	public DoctorService(DoctorRepository repo) {
		this.repo = repo;
	}

	@Transactional(readOnly = true)
	public List<DoctorDto> list() {
		return repo.findAll().stream().map(DoctorService::toDto).toList();
	}

	@Transactional(readOnly = true)
	public Doctor getEntity(UUID id) {
		return repo.findById(id).orElseThrow(() -> new NotFoundException("Doctor not found"));
	}

	@Transactional
	public DoctorDto create(CreateDoctorRequest req) {
		Doctor d = new Doctor();
		d.setFullName(req.fullName());
		d.setEmail(req.email());
		d.setSpecialty(req.specialty());
		return toDto(repo.save(d));
	}

	static DoctorDto toDto(Doctor d) {
		return new DoctorDto(d.getId(), d.getFullName(), d.getEmail(), d.getSpecialty(), d.getCreatedAt());
	}
}
