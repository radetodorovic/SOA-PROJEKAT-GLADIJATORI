package com.gladijatori.tourservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record CreateTourDto(
        @NotBlank(message = "Ime ture je obavezno.") String name,
        @NotBlank(message = "Opis ture je obavezan.") String description,
        @Min(value = 0, message = "Cijena ne moze biti negativna.") double price,
        String difficulty,
        List<String> tags
) {}
