package com.gladijatori.tourservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.time.LocalDate;
import java.util.List;

public record CreateReviewDto(
        @Min(value = 1, message = "Ocena mora biti izmedju 1 i 5.")
        @Max(value = 5, message = "Ocena mora biti izmedju 1 i 5.")
        int rating,
        String comment,
        String username,
        LocalDate visitDate,
        List<String> imageUrls
) {}
