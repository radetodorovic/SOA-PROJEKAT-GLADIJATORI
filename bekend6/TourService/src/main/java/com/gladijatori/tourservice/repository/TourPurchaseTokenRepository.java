package com.gladijatori.tourservice.repository;

import com.gladijatori.tourservice.model.TourPurchaseToken;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface TourPurchaseTokenRepository extends MongoRepository<TourPurchaseToken, String> {
    boolean existsByTouristIdAndTourId(int touristId, String tourId);
    Optional<TourPurchaseToken> findByTouristIdAndTourId(int touristId, String tourId);
    List<TourPurchaseToken> findByTouristId(int touristId);
}
