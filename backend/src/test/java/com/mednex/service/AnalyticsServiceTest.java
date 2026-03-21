package com.mednex.service;

import com.mednex.repo.BedRepository;
import com.mednex.repo.projection.WardOccupancyProjection;
import com.mednex.tenant.TenantContext;
import com.mednex.web.dto.BedOccupancyResponseDto;
import com.mednex.web.dto.WardOccupancyItemDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock BedRepository bedRepo;
    @InjectMocks AnalyticsService analyticsService;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId("hospital_a");
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void bedOccupancy_returns_correct_rate_and_wards() {
        when(bedRepo.count()).thenReturn(10L);
        when(bedRepo.countByStatus("OCCUPIED")).thenReturn(7L);

        WardOccupancyProjection proj = new WardOccupancyProjection() {
            public String getWard() { return "ICU"; }
            public long getOccupied() { return 7; }
            public long getTotal() { return 10; }
        };
        when(bedRepo.getWardOccupancy()).thenReturn(List.of(proj));

        BedOccupancyResponseDto result = analyticsService.bedOccupancy();

        assertThat(result.tenantId()).isEqualTo("hospital_a");
        assertThat(result.totalBeds()).isEqualTo(10);
        assertThat(result.occupiedBeds()).isEqualTo(7);
        assertThat(result.occupancyRate()).isEqualTo(70.0);
        assertThat(result.wards()).hasSize(1);
        WardOccupancyItemDto ward = result.wards().get(0);
        assertThat(ward.wardName()).isEqualTo("ICU");
        assertThat(ward.totalBeds()).isEqualTo(10);
        assertThat(ward.occupiedBeds()).isEqualTo(7);
        assertThat(ward.occupancyRate()).isEqualTo(70.0);
    }

    @Test
    void bedOccupancy_handles_zero_total_beds_without_division_error() {
        when(bedRepo.count()).thenReturn(0L);
        when(bedRepo.countByStatus("OCCUPIED")).thenReturn(0L);
        when(bedRepo.getWardOccupancy()).thenReturn(List.of());

        BedOccupancyResponseDto result = analyticsService.bedOccupancy();

        assertThat(result.occupancyRate()).isEqualTo(0.0);
        assertThat(result.totalBeds()).isEqualTo(0);
        assertThat(result.wards()).isEmpty();
    }

    @Test
    void bedOccupancy_returns_empty_wards_when_no_beds() {
        when(bedRepo.count()).thenReturn(0L);
        when(bedRepo.countByStatus("OCCUPIED")).thenReturn(0L);
        when(bedRepo.getWardOccupancy()).thenReturn(List.of());

        BedOccupancyResponseDto result = analyticsService.bedOccupancy();

        assertThat(result.wards()).isEmpty();
    }
}
