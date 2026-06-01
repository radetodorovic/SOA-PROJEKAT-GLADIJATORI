package com.gladijatori.tourservice.dto;

import java.time.LocalDateTime;

public record TourPurchaseTokenResponseDto(
        String tourId,
        String token,
        LocalDateTime createdAt
) {}
