package com.mednex.repo.projection;

public interface WardOccupancyProjection {
    String getWard();
    long getOccupied();
    long getTotal();
}
