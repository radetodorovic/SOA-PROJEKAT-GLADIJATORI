package com.gladijatori.tourservice.dto;

import java.time.LocalDateTime;

public record CompletedKeyPointResponseDto(
        String keyPointId,
        String keyPointName,
        LocalDateTime reachedAt
) {}
