package com.gladijatori.tourservice.dto;

public record PrepareExecutionStartResponseDto(
        TourExecutionResponseDto execution,
        boolean created
) {}
