package com.gladijatori.tourservice.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "tour_purchase_tokens")
public class TourPurchaseToken {
    @Id
    private String id;
    private int touristId;
    private String tourId;
    private String checkoutId;
    private String token;
    private PurchaseTokenStatus status;
    private LocalDateTime createdAt;
}
