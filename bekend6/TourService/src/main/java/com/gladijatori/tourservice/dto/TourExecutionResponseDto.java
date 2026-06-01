package com.gladijatori.tourservice.dto;

import java.time.LocalDateTime;
import java.util.List;

public record TourExecutionResponseDto(
        String id,
        String tourId,
        int touristId,
        String status,
        double startLatitude,
        double startLongitude,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        LocalDateTime abandonedAt,
        LocalDateTime lastActivity,
        List<CompletedKeyPointResponseDto> completedKeyPoints
) {}
