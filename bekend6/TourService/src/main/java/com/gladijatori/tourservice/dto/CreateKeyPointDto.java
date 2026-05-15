package com.gladijatori.tourservice.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateKeyPointDto(
        @NotBlank(message = "Naziv kljucne tacke je obavezan.") String name,
        String description,
        @NotNull(message = "Geografska sirina je obavezna.")
        @DecimalMin(value = "-90.0", message = "Geografska sirina mora biti izmedju -90 i 90.")
        @DecimalMax(value = "90.0",  message = "Geografska sirina mora biti izmedju -90 i 90.")
        Double latitude,
        @NotNull(message = "Geografska duzina je obavezna.")
        @DecimalMin(value = "-180.0", message = "Geografska duzina mora biti izmedju -180 i 180.")
        @DecimalMax(value = "180.0",  message = "Geografska duzina mora biti izmedju -180 i 180.")
        Double longitude,
        String imageUrl,
        int order
) {}
