package com.gladijatori.tourservice.model;

import lombok.Data;

@Data
public class OrderItem {
    private String tourId;
    private String tourName;
    private double price;
}
