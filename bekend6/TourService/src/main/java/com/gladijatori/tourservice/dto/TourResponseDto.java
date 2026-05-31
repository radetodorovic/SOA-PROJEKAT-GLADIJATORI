package com.gladijatori.tourservice.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record TourResponseDto(
        String id,
        int authorId,
        String name,
        String description,
        String status,
        String difficulty,
        double price,
        double distanceKm,
        Map<String, Integer> transportDurations,
        List<KeyPointResponseDto> keyPoints,
        List<String> tags,
        LocalDateTime createdAt,
        LocalDateTime publishedAt,
        LocalDateTime archivedAt
) {}
