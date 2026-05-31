package com.gladijatori.tourservice.dto;

import java.util.List;
import java.util.Map;

public record UpdateTourDto(
        String name,
        String description,
        Double price,
        String difficulty,
        String status,
        Map<String, Integer> transportDurations,
        List<String> tags
) {}
