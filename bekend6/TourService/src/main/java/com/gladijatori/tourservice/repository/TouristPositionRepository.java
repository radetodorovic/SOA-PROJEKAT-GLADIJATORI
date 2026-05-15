package com.gladijatori.tourservice.repository;

import com.gladijatori.tourservice.model.TouristPosition;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface TouristPositionRepository extends MongoRepository<TouristPosition, String> {
    Optional<TouristPosition> findByUserId(int userId);
}
