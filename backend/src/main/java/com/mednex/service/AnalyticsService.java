package com.mednex.service;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mednex.repo.BedRepository;
import com.mednex.tenant.TenantContext;
import com.mednex.web.dto.BedOccupancyResponseDto;
import com.mednex.web.dto.WardOccupancyItemDto;
import com.mednex.repo.projection.WardOccupancyProjection;

import java.util.List;

@Service
public class AnalyticsService {
	private final BedRepository bedRepo;

	public AnalyticsService(BedRepository bedRepo) {
		this.bedRepo = bedRepo;
	}

	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	@Transactional(readOnly = true)
	public BedOccupancyResponseDto bedOccupancy() {
		String tenantId = TenantContext.getTenantId() != null ? TenantContext.getTenantId() : "";
		long total = bedRepo.count();
		long occupied = bedRepo.countByStatus("OCCUPIED");
		double rate = total == 0 ? 0.0 : (occupied * 100.0 / total);

		List<WardOccupancyItemDto> wards = bedRepo.getWardOccupancy().stream()
			.map(p -> new WardOccupancyItemDto(
				p.getWard(),
				p.getTotal(),
				p.getOccupied(),
				p.getTotal() == 0 ? 0.0 : (p.getOccupied() * 100.0 / p.getTotal())))
			.toList();

		return new BedOccupancyResponseDto(tenantId, total, occupied, rate, wards);
	}
}
