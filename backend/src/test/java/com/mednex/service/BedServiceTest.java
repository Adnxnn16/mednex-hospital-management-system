package com.mednex.service;

import com.mednex.domain.Bed;
import com.mednex.repo.BedRepository;
import com.mednex.web.dto.BedDto;
import org.springframework.security.access.AccessDeniedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BedServiceTest {

    @Mock BedRepository repo;
    @InjectMocks BedService bedService;

    @Test
    void list_returns_all_beds_as_dtos() {
        Bed b1 = buildBed(UUID.randomUUID(), "ICU", "AVAILABLE");
        Bed b2 = buildBed(UUID.randomUUID(), "General", "OCCUPIED");

        when(repo.findAll()).thenReturn(List.of(b1, b2));

        List<BedDto> result = bedService.list();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).ward()).isEqualTo("ICU");
        assertThat(result.get(1).ward()).isEqualTo("General");
    }

    @Test
    void updateStatus_persists_new_status() {
        UUID id = UUID.randomUUID();
        Bed bed = buildBed(id, "ICU", "AVAILABLE");

        when(repo.findById(id)).thenReturn(Optional.of(bed));
        when(repo.save(bed)).thenReturn(bed);

        BedDto result = bedService.updateStatus(id, "OCCUPIED");

        assertThat(bed.getStatus()).isEqualTo("OCCUPIED");
        assertThat(result).isNotNull();
        verify(repo).save(bed);
    }

    @Test
    void updateStatus_throws_when_bed_not_found() {
        UUID id = UUID.randomUUID();
        when(repo.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bedService.updateStatus(id, "OCCUPIED"))
            .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getEntity_returns_bed_when_exists() {
        UUID id = UUID.randomUUID();
        Bed bed = buildBed(id, "ICU", "AVAILABLE");

        when(repo.findById(id)).thenReturn(Optional.of(bed));

        Bed result = bedService.getEntity(id);

        assertThat(result.getId()).isEqualTo(id);
    }

    @Test
    void getEntity_throws_when_not_found() {
        UUID id = UUID.randomUUID();
        when(repo.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bedService.getEntity(id))
            .isInstanceOf(AccessDeniedException.class);
    }

    // --- helper ---
    private Bed buildBed(UUID id, String ward, String status) {
        Bed bed = new Bed();
        bed.setId(id);
        bed.setWard(ward);
        bed.setRoom("101");
        bed.setBedNumber("A");
        bed.setStatus(status);
        bed.setCreatedAt(Instant.now());
        return bed;
    }
}
