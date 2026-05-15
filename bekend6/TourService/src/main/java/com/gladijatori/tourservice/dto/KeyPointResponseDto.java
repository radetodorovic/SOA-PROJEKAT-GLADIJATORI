package com.gladijatori.tourservice.dto;

public record KeyPointResponseDto(
        String id,
        String name,
        String description,
        double latitude,
        double longitude,
        String imageUrl,
        int order
) {}
