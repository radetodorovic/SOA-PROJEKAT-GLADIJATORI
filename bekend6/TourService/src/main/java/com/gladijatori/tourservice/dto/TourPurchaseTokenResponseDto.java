package com.gladijatori.tourservice.dto;

import java.time.LocalDateTime;

public record TourPurchaseTokenResponseDto(
        String tourId,
        String token,
        String status,
        String checkoutId,
        LocalDateTime createdAt
) {}
