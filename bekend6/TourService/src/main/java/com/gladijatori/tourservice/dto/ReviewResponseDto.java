package com.gladijatori.tourservice.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ReviewResponseDto(
        String id,
        String tourId,
        int userId,
        String username,
        int rating,
        String comment,
        LocalDate visitDate,
        List<String> imageUrls,
        LocalDateTime createdAt
) {}
