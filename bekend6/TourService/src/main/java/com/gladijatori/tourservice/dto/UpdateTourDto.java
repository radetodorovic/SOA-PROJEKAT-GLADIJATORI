package com.gladijatori.tourservice.dto;

import java.util.List;

public record UpdateTourDto(
        String name,
        String description,
        Double price,
        String difficulty,
        String status,
        List<String> tags
) {}
