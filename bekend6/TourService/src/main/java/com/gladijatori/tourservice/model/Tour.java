package com.gladijatori.tourservice.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Document(collection = "tours")
public class Tour {
    @Id
    private String id;
    private int authorId;
    private String name;
    private String description;
    private String status;
    private String difficulty;
    private double price;
    private double distanceKm;
    private Map<String, Integer> transportDurations = new HashMap<>();
    private List<KeyPoint> keyPoints = new ArrayList<>();
    private List<String> tags = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime publishedAt;
    private LocalDateTime archivedAt;
}
