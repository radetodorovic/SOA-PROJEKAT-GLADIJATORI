package com.gladijatori.tourservice.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CompletedKeyPoint {
    private String keyPointId;
    private String keyPointName;
    private LocalDateTime reachedAt;
}
