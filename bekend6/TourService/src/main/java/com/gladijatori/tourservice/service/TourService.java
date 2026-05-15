package com.gladijatori.tourservice.service;

import com.gladijatori.tourservice.dto.*;
import com.gladijatori.tourservice.model.KeyPoint;
import com.gladijatori.tourservice.model.Review;
import com.gladijatori.tourservice.model.Tour;
import com.gladijatori.tourservice.model.TouristPosition;
import com.gladijatori.tourservice.repository.ReviewRepository;
import com.gladijatori.tourservice.repository.TourRepository;
import com.gladijatori.tourservice.repository.TouristPositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TourService {

    private final TourRepository tourRepository;
    private final ReviewRepository reviewRepository;
    private final TouristPositionRepository positionRepository;

    public TourResponseDto createTour(int authorId, CreateTourDto dto) {
        if (authorId <= 0) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "X-User-Id header je obavezan.");
        }

        Tour tour = new Tour();
        tour.setAuthorId(authorId);
        tour.setName(dto.name().trim());
        tour.setDescription(dto.description().trim());
        tour.setStatus("DRAFT");
        tour.setDifficulty(dto.difficulty() != null ? dto.difficulty() : "EASY");
        tour.setPrice(dto.price());
        tour.setKeyPoints(new ArrayList<>());
        tour.setTags(dto.tags() != null ? dto.tags() : new ArrayList<>());
        tour.setCreatedAt(LocalDateTime.now());

        return mapToResponse(tourRepository.save(tour));
    }

    public TourResponseDto updateTour(String id, int userId, UpdateTourDto dto) {
        Tour tour = getTourOrThrow(id);
        requireAuthor(tour, userId, "Samo autor moze menjati turu.");

        if (dto.name() != null && !dto.name().isBlank()) {
            tour.setName(dto.name().trim());
        }
        if (dto.description() != null && !dto.description().isBlank()) {
            tour.setDescription(dto.description().trim());
        }
        if (dto.price() != null && dto.price() >= 0) {
            tour.setPrice(dto.price());
        }
        if (dto.difficulty() != null && !dto.difficulty().isBlank()) {
            tour.setDifficulty(dto.difficulty());
        }
        if ("PUBLISHED".equals(dto.status()) || "DRAFT".equals(dto.status())) {
            tour.setStatus(dto.status());
        }
        if (dto.tags() != null) {
            tour.setTags(dto.tags());
        }

        return mapToResponse(tourRepository.save(tour));
    }

    public List<TourResponseDto> getToursByAuthor(int authorId) {
        return tourRepository.findByAuthorId(authorId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<TourResponseDto> getPublishedTours() {
        return tourRepository.findByStatus("PUBLISHED").stream()
                .map(this::mapToResponse)
                .toList();
    }

    public TourResponseDto getTourById(String id) {
        return mapToResponse(getTourOrThrow(id));
    }

    public TourResponseDto addKeyPoint(String tourId, int userId, CreateKeyPointDto dto) {
        Tour tour = getTourOrThrow(tourId);
        requireAuthor(tour, userId, "Samo autor ture moze dodavati kljucne tacke.");

        KeyPoint kp = new KeyPoint();
        kp.setId(UUID.randomUUID().toString());
        kp.setName(dto.name().trim());
        kp.setDescription(dto.description() != null ? dto.description().trim() : null);
        kp.setLatitude(dto.latitude());
        kp.setLongitude(dto.longitude());
        kp.setImageUrl(dto.imageUrl());
        kp.setOrder(dto.order());

        tour.getKeyPoints().add(kp);
        return mapToResponse(tourRepository.save(tour));
    }

    public TourResponseDto updateKeyPoint(String tourId, String keypointId, int userId, CreateKeyPointDto dto) {
        Tour tour = getTourOrThrow(tourId);
        requireAuthor(tour, userId, "Samo autor ture moze menjati kljucne tacke.");

        KeyPoint kp = tour.getKeyPoints().stream()
                .filter(k -> keypointId.equals(k.getId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kljucna tacka nije pronadjena."));

        kp.setName(dto.name().trim());
        kp.setDescription(dto.description() != null ? dto.description().trim() : null);
        kp.setLatitude(dto.latitude());
        kp.setLongitude(dto.longitude());
        kp.setImageUrl(dto.imageUrl());
        kp.setOrder(dto.order());

        return mapToResponse(tourRepository.save(tour));
    }

    public TourResponseDto deleteKeyPoint(String tourId, String keypointId, int userId) {
        Tour tour = getTourOrThrow(tourId);
        requireAuthor(tour, userId, "Samo autor ture moze brisati kljucne tacke.");

        boolean removed = tour.getKeyPoints().removeIf(k -> keypointId.equals(k.getId()));
        if (!removed) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Kljucna tacka nije pronadjena.");
        }

        return mapToResponse(tourRepository.save(tour));
    }

    public ReviewResponseDto addReview(String tourId, int userId, CreateReviewDto dto) {
        if (userId <= 0) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "X-User-Id header je obavezan.");
        }

        Tour tour = getTourOrThrow(tourId);

        if (tour.getAuthorId() == userId) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Autor ture ne moze ostaviti recenziju svoje ture.");
        }

        Review review = new Review();
        review.setTourId(tourId);
        review.setUserId(userId);
        review.setUsername(dto.username() != null ? dto.username() : "Anoniman");
        review.setRating(dto.rating());
        review.setComment(dto.comment());
        review.setVisitDate(dto.visitDate());
        review.setImageUrls(dto.imageUrls() != null ? dto.imageUrls() : new ArrayList<>());
        review.setCreatedAt(LocalDateTime.now());

        return mapReviewToResponse(reviewRepository.save(review));
    }

    public List<ReviewResponseDto> getReviewsByTour(String tourId) {
        getTourOrThrow(tourId);
        return reviewRepository.findByTourId(tourId).stream()
                .map(this::mapReviewToResponse)
                .toList();
    }

    public void updatePosition(int userId, UpdatePositionDto dto) {
        if (userId <= 0) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "X-User-Id header je obavezan.");
        }

        TouristPosition pos = positionRepository.findByUserId(userId)
                .orElse(new TouristPosition());
        pos.setUserId(userId);
        pos.setLatitude(dto.latitude());
        pos.setLongitude(dto.longitude());
        pos.setUpdatedAt(LocalDateTime.now());
        positionRepository.save(pos);
    }

    public TouristPosition getPosition(int userId) {
        return positionRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Pozicija za korisnika nije pronadjena."));
    }

    private Tour getTourOrThrow(String id) {
        return tourRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tura nije pronadjena."));
    }

    private void requireAuthor(Tour tour, int userId, String message) {
        if (userId <= 0) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "X-User-Id header je obavezan.");
        }
        if (tour.getAuthorId() != userId) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, message);
        }
    }

    private TourResponseDto mapToResponse(Tour tour) {
        List<KeyPointResponseDto> keyPoints = tour.getKeyPoints().stream()
                .sorted(Comparator.comparingInt(KeyPoint::getOrder))
                .map(kp -> new KeyPointResponseDto(
                        kp.getId(), kp.getName(), kp.getDescription(),
                        kp.getLatitude(), kp.getLongitude(), kp.getImageUrl(), kp.getOrder()))
                .toList();

        return new TourResponseDto(
                tour.getId(), tour.getAuthorId(), tour.getName(), tour.getDescription(),
                tour.getStatus(), tour.getDifficulty(), tour.getPrice(), keyPoints,
                tour.getTags() != null ? tour.getTags() : new ArrayList<>(),
                tour.getCreatedAt());
    }

    private ReviewResponseDto mapReviewToResponse(Review review) {
        return new ReviewResponseDto(
                review.getId(), review.getTourId(), review.getUserId(),
                review.getUsername(), review.getRating(), review.getComment(),
                review.getVisitDate(), review.getImageUrls(), review.getCreatedAt());
    }
}
