package com.mednex.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.mednex.repo.BedRepository;
import com.mednex.web.dto.BedOccupancyDto;
import com.mednex.web.dto.WardOccupancyDto;
import java.util.List;

@Service
public class AnalyticsService {
	private final BedRepository bedRepo;

	public AnalyticsService(BedRepository bedRepo) {
		this.bedRepo = bedRepo;
	}

	@Transactional(readOnly = true)
	public BedOccupancyDto bedOccupancy() {
		long total = bedRepo.count();
		long occupied = bedRepo.countByStatus("OCCUPIED");
		double rate = total == 0 ? 0.0 : (occupied * 1.0 / total);
		return new BedOccupancyDto(total, occupied, rate);
	}

	@Transactional(readOnly = true)
	public List<WardOccupancyDto> wardOccupancy() {
		return bedRepo.getWardOccupancy().stream()
			.map(p -> new WardOccupancyDto(
				p.getWard(), 
				p.getOccupied(), 
				p.getTotal(), 
				p.getTotal() == 0 ? 0.0 : (p.getOccupied() * 1.0 / p.getTotal())))
			.toList();
	}
}
