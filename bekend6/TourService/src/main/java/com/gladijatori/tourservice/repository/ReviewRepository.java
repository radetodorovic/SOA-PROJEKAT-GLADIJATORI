package com.gladijatori.tourservice.repository;

import com.gladijatori.tourservice.model.Review;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ReviewRepository extends MongoRepository<Review, String> {
    List<Review> findByTourId(String tourId);
}
