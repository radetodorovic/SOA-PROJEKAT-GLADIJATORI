package com.gladijatori.tourservice.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "tourist_positions")
public class TouristPosition {
    @Id
    private String id;
    private int userId;
    private double latitude;
    private double longitude;
    private LocalDateTime updatedAt;
}
