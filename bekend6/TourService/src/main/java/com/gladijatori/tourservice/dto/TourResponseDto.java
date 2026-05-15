package com.gladijatori.tourservice.dto;

import java.time.LocalDateTime;
import java.util.List;

public record TourResponseDto(
        String id,
        int authorId,
        String name,
        String description,
        String status,
        String difficulty,
        double price,
        List<KeyPointResponseDto> keyPoints,
        List<String> tags,
        LocalDateTime createdAt
) {}
