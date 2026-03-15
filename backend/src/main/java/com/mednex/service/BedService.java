package com.mednex.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mednex.domain.Bed;
import com.mednex.repo.BedRepository;
import com.mednex.web.dto.BedDto;

@Service
public class BedService {
	private final BedRepository repo;

	public BedService(BedRepository repo) {
		this.repo = repo;
	}

	@Transactional(readOnly = true)
	public List<BedDto> list() {
		return repo.findAll().stream().map(BedService::toDto).toList();
	}

	@Transactional(readOnly = true)
	public Bed getEntity(UUID id) {
		return repo.findById(id).orElseThrow(() -> new NotFoundException("Bed not found"));
	}

	@Transactional
	public BedDto updateStatus(UUID id, String status) {
		Bed bed = getEntity(id);
		bed.setStatus(status);
		return toDto(repo.save(bed));
	}

	static BedDto toDto(Bed b) {
		return new BedDto(b.getId(), b.getWard(), b.getRoom(), b.getBedNumber(), b.getStatus(), b.getCreatedAt());
	}
}
