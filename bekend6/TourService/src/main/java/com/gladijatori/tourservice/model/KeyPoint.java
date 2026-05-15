package com.gladijatori.tourservice.model;

import lombok.Data;

@Data
public class KeyPoint {
    private String id;
    private String name;
    private String description;
    private double latitude;
    private double longitude;
    private String imageUrl;
    private int order;
}
