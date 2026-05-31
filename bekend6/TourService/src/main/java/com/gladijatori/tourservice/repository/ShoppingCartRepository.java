package com.gladijatori.tourservice.repository;

import com.gladijatori.tourservice.model.ShoppingCart;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ShoppingCartRepository extends MongoRepository<ShoppingCart, String> {
    Optional<ShoppingCart> findByTouristId(int touristId);
}
