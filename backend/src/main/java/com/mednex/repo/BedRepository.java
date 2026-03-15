package com.mednex.repo;

import java.util.UUID;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.mednex.domain.Bed;
import com.mednex.repo.projection.WardOccupancyProjection;

public interface BedRepository extends JpaRepository<Bed, UUID> {
	long countByStatus(String status);

	@Query("SELECT b.ward as ward, " +
		   "SUM(CASE WHEN b.status = 'OCCUPIED' THEN 1 ELSE 0 END) as occupied, " +
		   "COUNT(b) as total " +
		   "FROM Bed b GROUP BY b.ward")
	List<WardOccupancyProjection> getWardOccupancy();
}
