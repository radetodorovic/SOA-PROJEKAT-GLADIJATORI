package com.gladijatori.tourservice.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "tour_executions")
public class TourExecution {
    @Id
    private String id;
    private String tourId;
    private int touristId;
    private String status;
    private double startLatitude;
    private double startLongitude;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime abandonedAt;
    private LocalDateTime lastActivity;
    private List<CompletedKeyPoint> completedKeyPoints = new ArrayList<>();
}
