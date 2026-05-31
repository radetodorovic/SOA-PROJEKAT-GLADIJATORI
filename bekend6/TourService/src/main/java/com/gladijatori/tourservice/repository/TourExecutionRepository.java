package com.gladijatori.tourservice.repository;

import com.gladijatori.tourservice.model.TourExecution;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface TourExecutionRepository extends MongoRepository<TourExecution, String> {
    Optional<TourExecution> findByTouristIdAndTourIdAndStatus(int touristId, String tourId, String status);
    List<TourExecution> findByTouristIdAndTourId(int touristId, String tourId);
}
