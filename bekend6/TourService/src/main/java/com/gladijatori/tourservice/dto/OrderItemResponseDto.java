package com.gladijatori.tourservice.dto;

public record OrderItemResponseDto(
        String tourId,
        String tourName,
        double price
) {}
