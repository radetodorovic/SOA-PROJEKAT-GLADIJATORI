package com.gladijatori.tourservice.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "reviews")
public class Review {
    @Id
    private String id;
    private String tourId;
    private int userId;
    private String username;
    private int rating;
    private String comment;
    private LocalDate visitDate;
    private List<String> imageUrls;
    private LocalDateTime createdAt;
}
