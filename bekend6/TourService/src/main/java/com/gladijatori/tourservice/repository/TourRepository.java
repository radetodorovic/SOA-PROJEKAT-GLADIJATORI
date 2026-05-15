package com.gladijatori.tourservice.repository;

import com.gladijatori.tourservice.model.Tour;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TourRepository extends MongoRepository<Tour, String> {
    List<Tour> findByAuthorId(int authorId);
    List<Tour> findByStatus(String status);
}
