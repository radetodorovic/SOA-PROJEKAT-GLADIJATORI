package com.gladijatori.tourservice.controller;

import com.gladijatori.tourservice.dto.*;
import com.gladijatori.tourservice.model.TouristPosition;
import com.gladijatori.tourservice.security.InternalEndpointGuard;
import com.gladijatori.tourservice.service.TourService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tours")
@RequiredArgsConstructor
public class TourController {

    private final TourService tourService;
    private final InternalEndpointGuard internalEndpointGuard;

    // ── Tours ─────────────────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<TourResponseDto> createTour(
            @RequestHeader(value = "X-User-Id", defaultValue = "0") int userId,
            @Valid @RequestBody CreateTourDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tourService.createTour(userId, dto));
    }

    @GetMapping("/published")
    public ResponseEntity<List<TourResponseDto>> getPublishedTours() {
        return ResponseEntity.ok(tourService.getPublishedTours());
    }

    @GetMapping("/author/{authorId}")
    public ResponseEntity<List<TourResponseDto>> getToursByAuthor(@PathVariable int authorId) {
        return ResponseEntity.ok(tourService.getToursByAuthor(authorId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TourResponseDto> getTourById(
            @PathVariable String id,
            @RequestHeader(value = "X-User-Id", defaultValue = "0") int userId) {
        return ResponseEntity.ok(tourService.getTourById(id, userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TourResponseDto> updateTour(
            @PathVariable String id,
            @RequestHeader(value = "X-User-Id", defaultValue = "0") int userId,
            @RequestBody UpdateTourDto dto) {
        return ResponseEntity.ok(tourService.updateTour(id, userId, dto));
    }

    // ── Shopping cart / purchases ─────────────────────────────────────────────

    @GetMapping("/cart")
    public ResponseEntity<ShoppingCartResponseDto> getCart(
            @RequestHeader(value = "X-User-Id", defaultValue = "0") int userId) {
        return ResponseEntity.ok(tourService.getCart(userId));
    }

    @PostMapping("/cart/items/{tourId}")
    public ResponseEntity<ShoppingCartResponseDto> addToCart(
            @PathVariable String tourId,
            @RequestHeader(value = "X-User-Id", defaultValue = "0") int userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tourService.addToCart(userId, tourId));
    }

    @DeleteMapping("/cart/items/{tourId}")
    public ResponseEntity<ShoppingCartResponseDto> removeFromCart(
            @PathVariable String tourId,
            @RequestHeader(value = "X-User-Id", defaultValue = "0") int userId) {
        return ResponseEntity.ok(tourService.removeFromCart(userId, tourId));
    }

    @PostMapping("/cart/checkout/validate")
    public ResponseEntity<ShoppingCartResponseDto> validateCheckout(
            @RequestHeader(value = "X-User-Id", defaultValue = "0") int userId,
            @RequestHeader(value = "X-Internal-Api-Key", required = false) String internalApiKey) {
        internalEndpointGuard.requireInternalAccess(internalApiKey);
        return ResponseEntity.ok(tourService.validateCheckout(userId));
    }

    @PostMapping("/cart/checkout/prepare")
    public ResponseEntity<PrepareCheckoutResponseDto> prepareCheckout(
            @RequestHeader(value = "X-User-Id", defaultValue = "0") int userId,
            @RequestHeader(value = "X-Internal-Api-Key", required = false) String internalApiKey) {
        internalEndpointGuard.requireInternalAccess(internalApiKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(tourService.prepareCheckout(userId));
    }

    @PostMapping("/cart/checkout/{checkoutId}/confirm")
    public ResponseEntity<List<TourPurchaseTokenResponseDto>> confirmCheckout(
            @PathVariable String checkoutId,
            @RequestHeader(value = "X-User-Id", defaultValue = "0") int userId,
            @RequestHeader(value = "X-Internal-Api-Key", required = false) String internalApiKey) {
        internalEndpointGuard.requireInternalAccess(internalApiKey);
        return ResponseEntity.ok(tourService.confirmPendingCheckout(userId, checkoutId));
    }

    @PostMapping("/cart/checkout/{checkoutId}/cancel")
    public ResponseEntity<Void> cancelCheckout(
            @PathVariable String checkoutId,
            @RequestHeader(value = "X-User-Id", defaultValue = "0") int userId,
            @RequestHeader(value = "X-Internal-Api-Key", required = false) String internalApiKey) {
        internalEndpointGuard.requireInternalAccess(internalApiKey);
        tourService.cancelPendingCheckout(userId, checkoutId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/cart/checkout")
    public ResponseEntity<List<TourPurchaseTokenResponseDto>> checkout(
            @RequestHeader(value = "X-User-Id", defaultValue = "0") int userId,
            @RequestHeader(value = "X-Internal-Api-Key", required = false) String internalApiKey) {
        internalEndpointGuard.requireInternalAccess(internalApiKey);
        return ResponseEntity.ok(tourService.checkout(userId));
    }

    @GetMapping("/purchases")
    public ResponseEntity<List<TourPurchaseTokenResponseDto>> getPurchaseTokens(
            @RequestHeader(value = "X-User-Id", defaultValue = "0") int userId) {
        return ResponseEntity.ok(tourService.getPurchaseTokens(userId));
    }

    @GetMapping("/purchased")
    public ResponseEntity<List<TourResponseDto>> getPurchasedTours(
            @RequestHeader(value = "X-User-Id", defaultValue = "0") int userId,
            @RequestHeader(value = "X-Internal-Api-Key", required = false) String internalApiKey) {
        internalEndpointGuard.requireInternalAccess(internalApiKey);
        return ResponseEntity.ok(tourService.getPurchasedTours(userId));
    }

    // ── Tour execution ────────────────────────────────────────────────────────

    @PostMapping("/{tourId}/execution/start")
    public ResponseEntity<TourExecutionResponseDto> startExecution(
            @PathVariable String tourId,
            @RequestHeader(value = "X-User-Id", defaultValue = "0") int userId,
            @RequestHeader(value = "X-Internal-Api-Key", required = false) String internalApiKey) {
        internalEndpointGuard.requireInternalAccess(internalApiKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(tourService.startTourExecution(tourId, userId));
    }

    @PostMapping("/{tourId}/execution/start/prepare")
    public ResponseEntity<PrepareExecutionStartResponseDto> prepareStartExecution(
            @PathVariable String tourId,
            @RequestHeader(value = "X-User-Id", defaultValue = "0") int userId,
            @RequestHeader(value = "X-Internal-Api-Key", required = false) String internalApiKey) {
        internalEndpointGuard.requireInternalAccess(internalApiKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(tourService.prepareStartTourExecution(tourId, userId));
    }

    @PostMapping("/{tourId}/execution/start/cancel")
    public ResponseEntity<TourExecutionResponseDto> compensateStartExecution(
            @PathVariable String tourId,
            @RequestHeader(value = "X-User-Id", defaultValue = "0") int userId,
            @RequestHeader(value = "X-Internal-Api-Key", required = false) String internalApiKey) {
        internalEndpointGuard.requireInternalAccess(internalApiKey);
        TourExecutionResponseDto response = tourService.compensateStartTourExecution(tourId, userId);
        return response == null
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(response);
    }

    @GetMapping("/{tourId}/execution/active")
    public ResponseEntity<TourExecutionResponseDto> getActiveExecution(
            @PathVariable String tourId,
            @RequestHeader(value = "X-User-Id", defaultValue = "0") int userId) {
        TourExecutionResponseDto execution = tourService.getActiveExecution(tourId, userId);
        return execution == null
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(execution);
    }

    @PostMapping("/{tourId}/execution/check")
    public ResponseEntity<TourExecutionResponseDto> checkKeyPointProximity(
            @PathVariable String tourId,
            @RequestHeader(value = "X-User-Id", defaultValue = "0") int userId) {
        return ResponseEntity.ok(tourService.checkKeyPointProximity(tourId, userId));
    }

    @PostMapping("/{tourId}/execution/complete")
    public ResponseEntity<TourExecutionResponseDto> completeExecution(
            @PathVariable String tourId,
            @RequestHeader(value = "X-User-Id", defaultValue = "0") int userId) {
        return ResponseEntity.ok(tourService.completeTourExecution(tourId, userId));
    }

    @PostMapping("/{tourId}/execution/abandon")
    public ResponseEntity<TourExecutionResponseDto> abandonExecution(
            @PathVariable String tourId,
            @RequestHeader(value = "X-User-Id", defaultValue = "0") int userId) {
        return ResponseEntity.ok(tourService.abandonTourExecution(tourId, userId));
    }

    // ── Key points ────────────────────────────────────────────────────────────

    @PostMapping("/{tourId}/keypoints")
    public ResponseEntity<TourResponseDto> addKeyPoint(
            @PathVariable String tourId,
            @RequestHeader(value = "X-User-Id", defaultValue = "0") int userId,
            @Valid @RequestBody CreateKeyPointDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tourService.addKeyPoint(tourId, userId, dto));
    }

    @PutMapping("/{tourId}/keypoints/{keypointId}")
    public ResponseEntity<TourResponseDto> updateKeyPoint(
            @PathVariable String tourId,
            @PathVariable String keypointId,
            @RequestHeader(value = "X-User-Id", defaultValue = "0") int userId,
            @Valid @RequestBody CreateKeyPointDto dto) {
        return ResponseEntity.ok(tourService.updateKeyPoint(tourId, keypointId, userId, dto));
    }

    @DeleteMapping("/{tourId}/keypoints/{keypointId}")
    public ResponseEntity<TourResponseDto> deleteKeyPoint(
            @PathVariable String tourId,
            @PathVariable String keypointId,
            @RequestHeader(value = "X-User-Id", defaultValue = "0") int userId) {
        return ResponseEntity.ok(tourService.deleteKeyPoint(tourId, keypointId, userId));
    }

    // ── Reviews ───────────────────────────────────────────────────────────────

    @PostMapping("/{tourId}/reviews")
    public ResponseEntity<ReviewResponseDto> addReview(
            @PathVariable String tourId,
            @RequestHeader(value = "X-User-Id", defaultValue = "0") int userId,
            @Valid @RequestBody CreateReviewDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tourService.addReview(tourId, userId, dto));
    }

    @GetMapping("/{tourId}/reviews")
    public ResponseEntity<List<ReviewResponseDto>> getReviews(@PathVariable String tourId) {
        return ResponseEntity.ok(tourService.getReviewsByTour(tourId));
    }

    // ── Tourist position (simulator) ──────────────────────────────────────────

    @PutMapping("/position")
    public ResponseEntity<Void> updatePosition(
            @RequestHeader(value = "X-User-Id", defaultValue = "0") int userId,
            @Valid @RequestBody UpdatePositionDto dto) {
        tourService.updatePosition(userId, dto);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/position/{userId}")
    public ResponseEntity<TouristPosition> getPosition(@PathVariable int userId) {
        return ResponseEntity.ok(tourService.getPosition(userId));
    }

    // ── Health ────────────────────────────────────────────────────────────────

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("ok");
    }
}
