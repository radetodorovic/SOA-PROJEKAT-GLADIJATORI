package com.gladijatori.tourservice.repository;

import com.gladijatori.tourservice.model.TourPurchaseToken;
import com.gladijatori.tourservice.model.PurchaseTokenStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface TourPurchaseTokenRepository extends MongoRepository<TourPurchaseToken, String> {
    Optional<TourPurchaseToken> findByTouristIdAndTourId(int touristId, String tourId);
    List<TourPurchaseToken> findByTouristId(int touristId);
    List<TourPurchaseToken> findByTouristIdAndCheckoutIdAndStatus(int touristId, String checkoutId, PurchaseTokenStatus status);
}
