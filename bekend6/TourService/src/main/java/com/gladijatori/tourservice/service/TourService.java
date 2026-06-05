package com.gladijatori.tourservice.service;

import com.gladijatori.tourservice.dto.*;
import com.gladijatori.tourservice.model.CompletedKeyPoint;
import com.gladijatori.tourservice.model.KeyPoint;
import com.gladijatori.tourservice.model.OrderItem;
import com.gladijatori.tourservice.model.PurchaseTokenStatus;
import com.gladijatori.tourservice.model.Review;
import com.gladijatori.tourservice.model.ShoppingCart;
import com.gladijatori.tourservice.model.Tour;
import com.gladijatori.tourservice.model.TourExecution;
import com.gladijatori.tourservice.model.TourPurchaseToken;
import com.gladijatori.tourservice.model.TouristPosition;
import com.gladijatori.tourservice.repository.ReviewRepository;
import com.gladijatori.tourservice.repository.ShoppingCartRepository;
import com.gladijatori.tourservice.repository.TourExecutionRepository;
import com.gladijatori.tourservice.repository.TourPurchaseTokenRepository;
import com.gladijatori.tourservice.repository.TourRepository;
import com.gladijatori.tourservice.repository.TouristPositionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TourService {
    private static final Logger log = LoggerFactory.getLogger(TourService.class);
    private static final String DRAFT = "DRAFT";
    private static final String PUBLISHED = "PUBLISHED";
    private static final String ARCHIVED = "ARCHIVED";
    private static final String EXECUTION_ACTIVE = "ACTIVE";
    private static final String EXECUTION_COMPLETED = "COMPLETED";
    private static final String EXECUTION_ABANDONED = "ABANDONED";
    private static final List<String> ALLOWED_TRANSPORT_TYPES = List.of("WALKING", "BICYCLE", "CAR");
    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final double KEY_POINT_PROXIMITY_KM = 0.1;

    private final TourRepository tourRepository;
    private final ReviewRepository reviewRepository;
    private final TouristPositionRepository positionRepository;
    private final ShoppingCartRepository cartRepository;
    private final TourPurchaseTokenRepository purchaseTokenRepository;
    private final TourExecutionRepository tourExecutionRepository;

    public TourResponseDto createTour(int authorId, CreateTourDto dto) {
        if (authorId <= 0) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "X-User-Id header je obavezan.");
        }

        Tour tour = new Tour();
        tour.setAuthorId(authorId);
        tour.setName(dto.name().trim());
        tour.setDescription(dto.description().trim());
        tour.setStatus(DRAFT);
        tour.setDifficulty(dto.difficulty() != null ? dto.difficulty() : "EASY");
        tour.setPrice(0);
        tour.setDistanceKm(0);
        tour.setTransportDurations(new HashMap<>());
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
        if (dto.transportDurations() != null) {
            tour.setTransportDurations(normalizeTransportDurations(dto.transportDurations()));
        }
        if (dto.status() != null && !dto.status().isBlank()) {
            applyStatusChange(tour, dto.status());
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
                .map(tour -> mapToResponse(tour, false))
                .toList();
    }

    public TourResponseDto getTourById(String id, int userId) {
        Tour tour = getTourOrThrow(id);
        boolean isAuthor = isAuthor(tour, userId);
        boolean hasPurchaseToken = hasConfirmedPurchaseToken(userId, tour.getId());

        if (!canAccessTour(tour, isAuthor, hasPurchaseToken)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tura nije pronadjena.");
        }

        boolean includeAllKeyPoints = isAuthor || hasPurchaseToken;
        return mapToResponse(tour, includeAllKeyPoints);
    }

    public ShoppingCartResponseDto getCart(int touristId) {
        requireUser(touristId);
        return mapCartToResponse(getOrCreateCart(touristId));
    }

    public ShoppingCartResponseDto addToCart(int touristId, String tourId) {
        requireUser(touristId);
        Tour tour = getTourOrThrow(tourId);
        if (!PUBLISHED.equals(tour.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Samo objavljena tura moze da se kupi.");
        }
        if (tour.getAuthorId() == touristId) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Autor ne moze kupiti svoju turu.");
        }
        if (hasConfirmedPurchaseToken(touristId, tourId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tura je vec kupljena.");
        }

        ShoppingCart cart = getOrCreateCart(touristId);
        boolean exists = cart.getItems().stream().anyMatch(item -> tourId.equals(item.getTourId()));
        if (!exists) {
            OrderItem item = new OrderItem();
            item.setTourId(tour.getId());
            item.setTourName(tour.getName());
            item.setPrice(tour.getPrice());
            cart.getItems().add(item);
            recalculateCartTotal(cart);
        }
        return mapCartToResponse(cartRepository.save(cart));
    }

    public ShoppingCartResponseDto removeFromCart(int touristId, String tourId) {
        requireUser(touristId);
        ShoppingCart cart = getOrCreateCart(touristId);
        cart.getItems().removeIf(item -> tourId.equals(item.getTourId()));
        recalculateCartTotal(cart);
        return mapCartToResponse(cartRepository.save(cart));
    }

    public List<TourPurchaseTokenResponseDto> checkout(int touristId) {
        String checkoutId = null;
        try {
            PrepareCheckoutResponseDto prepared = prepareCheckout(touristId);
            checkoutId = prepared.checkoutId();
            return confirmPendingCheckout(touristId, checkoutId);
        } catch (RuntimeException ex) {
            if (checkoutId != null) {
                cancelPendingCheckout(touristId, checkoutId);
            }
            throw ex;
        }
    }

    public ShoppingCartResponseDto validateCheckout(int touristId) {
        return mapCartToResponse(validateCheckoutState(touristId).cart());
    }

    public PrepareCheckoutResponseDto prepareCheckout(int touristId) {
        ValidatedCheckout validatedCheckout = validateCheckoutState(touristId);
        String checkoutId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();

        List<TourPurchaseToken> pendingTokens = validatedCheckout.tours().stream()
                .map(tour -> buildPendingToken(touristId, tour.getId(), checkoutId, now))
                .toList();

        try {
            List<TourPurchaseTokenResponseDto> tokens = purchaseTokenRepository.saveAll(pendingTokens).stream()
                    .map(this::mapPurchaseTokenToResponse)
                    .toList();
            return new PrepareCheckoutResponseDto(checkoutId, tokens);
        } catch (Exception ex) {
            cancelPendingCheckout(touristId, checkoutId);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Kreiranje PENDING tokena nije uspelo.", ex);
        }
    }

    public List<TourPurchaseTokenResponseDto> confirmPendingCheckout(int touristId, String checkoutId) {
        requireUser(touristId);
        List<TourPurchaseToken> pendingTokens = getPendingTokensOrThrow(touristId, checkoutId);
        ShoppingCart cart = getOrCreateCart(touristId);
        ShoppingCart originalCart = copyCart(cart);

        try {
            pendingTokens.forEach(token -> token.setStatus(PurchaseTokenStatus.CONFIRMED));
            List<TourPurchaseToken> confirmedTokens = purchaseTokenRepository.saveAll(pendingTokens);

            List<String> purchasedTourIds = confirmedTokens.stream()
                    .map(TourPurchaseToken::getTourId)
                    .toList();
            cart.getItems().removeIf(item -> purchasedTourIds.contains(item.getTourId()));
            recalculateCartTotal(cart);
            cartRepository.save(cart);

            return confirmedTokens.stream()
                    .map(this::mapPurchaseTokenToResponse)
                    .toList();
        } catch (Exception ex) {
            rollbackFailedCheckoutConfirmation(originalCart, pendingTokens);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Potvrda checkout-a nije uspela.", ex);
        }
    }

    public void cancelPendingCheckout(int touristId, String checkoutId) {
        requireUser(touristId);
        List<TourPurchaseToken> pendingTokens = purchaseTokenRepository.findByTouristIdAndCheckoutIdAndStatus(
                touristId, checkoutId, PurchaseTokenStatus.PENDING);
        if (pendingTokens.isEmpty()) {
            return;
        }

        pendingTokens.forEach(token -> token.setStatus(PurchaseTokenStatus.CANCELLED));
        purchaseTokenRepository.saveAll(pendingTokens);
    }

    private void rollbackFailedCheckoutConfirmation(ShoppingCart originalCart, List<TourPurchaseToken> tokens) {
        try {
            cartRepository.save(originalCart);
        } catch (Exception ignored) {
        }

        try {
            tokens.forEach(token -> token.setStatus(PurchaseTokenStatus.PENDING));
            purchaseTokenRepository.saveAll(tokens);
        } catch (Exception ignored) {
        }
    }

    public List<TourPurchaseTokenResponseDto> getPurchaseTokens(int touristId) {
        requireUser(touristId);
        return purchaseTokenRepository.findByTouristId(touristId).stream()
                .filter(this::isConfirmedToken)
                .map(this::mapPurchaseTokenToResponse)
                .toList();
    }

    public List<TourResponseDto> getPurchasedTours(int touristId) {
        requireUser(touristId);
        return purchaseTokenRepository.findByTouristId(touristId).stream()
                .filter(this::isConfirmedToken)
                .map(TourPurchaseToken::getTourId)
                .distinct()
                .map(tourRepository::findById)
                .flatMap(Optional::stream)
                .filter(tour -> PUBLISHED.equals(tour.getStatus()) || ARCHIVED.equals(tour.getStatus()))
                .map(tour -> mapToResponse(tour, true))
                .toList();
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
        recalculateDistance(tour);
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

        recalculateDistance(tour);
        return mapToResponse(tourRepository.save(tour));
    }

    public TourResponseDto deleteKeyPoint(String tourId, String keypointId, int userId) {
        Tour tour = getTourOrThrow(tourId);
        requireAuthor(tour, userId, "Samo autor ture moze brisati kljucne tacke.");

        boolean removed = tour.getKeyPoints().removeIf(k -> keypointId.equals(k.getId()));
        if (!removed) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Kljucna tacka nije pronadjena.");
        }

        recalculateDistance(tour);
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

    public TourExecutionResponseDto startTourExecution(String tourId, int touristId) {
        return prepareStartTourExecution(tourId, touristId).execution();
    }

    public PrepareExecutionStartResponseDto prepareStartTourExecution(String tourId, int touristId) {
        requireUser(touristId);
        Tour tour = getTourOrThrow(tourId);
        if (!PUBLISHED.equals(tour.getStatus()) && !ARCHIVED.equals(tour.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mogu se pokrenuti samo objavljene ili arhivirane ture.");
        }
        if (!hasConfirmedPurchaseToken(touristId, tourId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tura mora biti kupljena pre pokretanja.");
        }

        return tourExecutionRepository.findByTouristIdAndTourIdAndStatus(touristId, tourId, EXECUTION_ACTIVE)
                .map(existing -> {
                    log.info("StartTourExecutionSaga prepare reused existing ACTIVE execution. touristId={}, tourId={}, executionId={}",
                            touristId, tourId, existing.getId());
                    return new PrepareExecutionStartResponseDto(mapExecutionToResponse(existing), false);
                })
                .orElseGet(() -> {
                    TouristPosition position = getPosition(touristId);
                    LocalDateTime now = LocalDateTime.now();
                    TourExecution execution = new TourExecution();
                    execution.setTourId(tourId);
                    execution.setTouristId(touristId);
                    execution.setStatus(EXECUTION_ACTIVE);
                    execution.setStartLatitude(position.getLatitude());
                    execution.setStartLongitude(position.getLongitude());
                    execution.setStartedAt(now);
                    execution.setLastActivity(now);
                    execution.setCompletedKeyPoints(new ArrayList<>());
                    TourExecution savedExecution = tourExecutionRepository.save(execution);
                    log.info("StartTourExecutionSaga prepare created ACTIVE execution. touristId={}, tourId={}, executionId={}",
                            touristId, tourId, savedExecution.getId());
                    return new PrepareExecutionStartResponseDto(mapExecutionToResponse(savedExecution), true);
                });
    }

    public TourExecutionResponseDto compensateStartTourExecution(String tourId, int touristId) {
        requireUser(touristId);
        Optional<TourExecution> activeExecution = tourExecutionRepository.findByTouristIdAndTourIdAndStatus(
                touristId, tourId, EXECUTION_ACTIVE);
        if (activeExecution.isEmpty()) {
            log.info("StartTourExecutionSaga compensation skipped because ACTIVE execution does not exist. touristId={}, tourId={}",
                    touristId, tourId);
            return null;
        }

        TourExecution execution = activeExecution.get();
        LocalDateTime now = LocalDateTime.now();
        execution.setStatus(EXECUTION_ABANDONED);
        execution.setAbandonedAt(now);
        execution.setLastActivity(now);
        TourExecution savedExecution = tourExecutionRepository.save(execution);
        log.info("StartTourExecutionSaga compensation abandoned ACTIVE execution. touristId={}, tourId={}, executionId={}",
                touristId, tourId, savedExecution.getId());
        return mapExecutionToResponse(savedExecution);
    }

    public TourExecutionResponseDto getActiveExecution(String tourId, int touristId) {
        requireUser(touristId);
        return tourExecutionRepository.findByTouristIdAndTourIdAndStatus(touristId, tourId, EXECUTION_ACTIVE)
                .map(this::mapExecutionToResponse)
                .orElse(null);
    }

    public TourExecutionResponseDto checkKeyPointProximity(String tourId, int touristId) {
        requireUser(touristId);
        Tour tour = getTourOrThrow(tourId);
        TourExecution execution = getActiveExecutionOrThrow(touristId, tourId);
        TouristPosition position = getPosition(touristId);
        if (execution.getCompletedKeyPoints() == null) {
            execution.setCompletedKeyPoints(new ArrayList<>());
        }

        List<KeyPoint> keyPoints = tour.getKeyPoints() == null ? new ArrayList<>() : tour.getKeyPoints();
        for (KeyPoint keyPoint : keyPoints.stream().sorted(Comparator.comparingInt(KeyPoint::getOrder)).toList()) {
            boolean alreadyCompleted = execution.getCompletedKeyPoints().stream()
                    .anyMatch(completed -> keyPoint.getId().equals(completed.getKeyPointId()));
            if (alreadyCompleted) {
                continue;
            }

            double distance = calculateDistance(position.getLatitude(), position.getLongitude(),
                    keyPoint.getLatitude(), keyPoint.getLongitude());
            if (distance <= KEY_POINT_PROXIMITY_KM) {
                CompletedKeyPoint completed = new CompletedKeyPoint();
                completed.setKeyPointId(keyPoint.getId());
                completed.setKeyPointName(keyPoint.getName());
                completed.setReachedAt(LocalDateTime.now());
                execution.getCompletedKeyPoints().add(completed);
                break;
            }
        }

        execution.setLastActivity(LocalDateTime.now());
        return mapExecutionToResponse(tourExecutionRepository.save(execution));
    }

    public TourExecutionResponseDto completeTourExecution(String tourId, int touristId) {
        requireUser(touristId);
        Tour tour = getTourOrThrow(tourId);
        TourExecution execution = getActiveExecutionOrThrow(touristId, tourId);
        if (execution.getCompletedKeyPoints() == null) {
            execution.setCompletedKeyPoints(new ArrayList<>());
        }
        int keyPointCount = tour.getKeyPoints() == null ? 0 : tour.getKeyPoints().size();
        if (execution.getCompletedKeyPoints().size() < keyPointCount) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sve kljucne tacke moraju biti dostignute pre zavrsetka ture.");
        }
        LocalDateTime now = LocalDateTime.now();
        execution.setStatus(EXECUTION_COMPLETED);
        execution.setCompletedAt(now);
        execution.setLastActivity(now);
        return mapExecutionToResponse(tourExecutionRepository.save(execution));
    }

    public TourExecutionResponseDto abandonTourExecution(String tourId, int touristId) {
        requireUser(touristId);
        TourExecution execution = getActiveExecutionOrThrow(touristId, tourId);
        LocalDateTime now = LocalDateTime.now();
        execution.setStatus(EXECUTION_ABANDONED);
        execution.setAbandonedAt(now);
        execution.setLastActivity(now);
        return mapExecutionToResponse(tourExecutionRepository.save(execution));
    }

    private Tour getTourOrThrow(String id) {
        return tourRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tura nije pronadjena."));
    }

    private boolean canAccessTour(Tour tour, boolean isAuthor, boolean hasPurchaseToken) {
        if (isAuthor) {
            return true;
        }
        if (PUBLISHED.equals(tour.getStatus())) {
            return true;
        }
        return ARCHIVED.equals(tour.getStatus()) && hasPurchaseToken;
    }

    private boolean isAuthor(Tour tour, int userId) {
        return userId > 0 && tour.getAuthorId() == userId;
    }

    private void requireAuthor(Tour tour, int userId, String message) {
        if (userId <= 0) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "X-User-Id header je obavezan.");
        }
        if (tour.getAuthorId() != userId) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, message);
        }
    }

    private void requireUser(int userId) {
        if (userId <= 0) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "X-User-Id header je obavezan.");
        }
    }

    private boolean hasConfirmedPurchaseToken(int touristId, String tourId) {
        return touristId > 0 && purchaseTokenRepository.findByTouristIdAndTourId(touristId, tourId)
                .map(this::isConfirmedToken)
                .orElse(false);
    }

    private ShoppingCart getOrCreateCart(int touristId) {
        return cartRepository.findByTouristId(touristId)
                .orElseGet(() -> {
                    ShoppingCart cart = new ShoppingCart();
                    cart.setTouristId(touristId);
                    cart.setItems(new ArrayList<>());
                    cart.setTotalPrice(0);
                    return cartRepository.save(cart);
                });
    }

    private void recalculateCartTotal(ShoppingCart cart) {
        double total = cart.getItems().stream()
                .mapToDouble(OrderItem::getPrice)
                .sum();
        cart.setTotalPrice(Math.round(total * 100.0) / 100.0);
    }

    private ValidatedCheckout validateCheckoutState(int touristId) {
        requireUser(touristId);
        ShoppingCart cart = getOrCreateCart(touristId);
        if (cart.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Korpa je prazna.");
        }

        List<Tour> tours = new ArrayList<>();
        for (OrderItem item : cart.getItems()) {
            Tour tour = getTourOrThrow(item.getTourId());
            if (!PUBLISHED.equals(tour.getStatus())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tura vise nije dostupna za kupovinu: " + tour.getName());
            }
            if (tour.getAuthorId() == touristId) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Autor ne moze kupiti svoju turu.");
            }
            if (hasConfirmedPurchaseToken(touristId, tour.getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tura je vec kupljena: " + tour.getName());
            }
            tours.add(tour);
        }

        return new ValidatedCheckout(cart, tours);
    }

    private TourPurchaseToken buildPendingToken(int touristId, String tourId, String checkoutId, LocalDateTime createdAt) {
        Optional<TourPurchaseToken> existingToken = purchaseTokenRepository.findByTouristIdAndTourId(touristId, tourId);
        if (existingToken.isPresent() && isConfirmedToken(existingToken.get())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tura je vec kupljena.");
        }
        TourPurchaseToken token = existingToken.orElseGet(TourPurchaseToken::new);

        token.setTouristId(touristId);
        token.setTourId(tourId);
        token.setCheckoutId(checkoutId);
        token.setToken(UUID.randomUUID().toString());
        token.setStatus(PurchaseTokenStatus.PENDING);
        token.setCreatedAt(createdAt);
        return token;
    }

    private ShoppingCart copyCart(ShoppingCart source) {
        ShoppingCart copy = new ShoppingCart();
        copy.setId(source.getId());
        copy.setTouristId(source.getTouristId());
        copy.setItems(source.getItems().stream().map(this::copyOrderItem).toList());
        copy.setTotalPrice(source.getTotalPrice());
        return copy;
    }

    private OrderItem copyOrderItem(OrderItem source) {
        OrderItem copy = new OrderItem();
        copy.setTourId(source.getTourId());
        copy.setTourName(source.getTourName());
        copy.setPrice(source.getPrice());
        return copy;
    }

    private List<TourPurchaseToken> getPendingTokensOrThrow(int touristId, String checkoutId) {
        List<TourPurchaseToken> pendingTokens = purchaseTokenRepository.findByTouristIdAndCheckoutIdAndStatus(
                touristId, checkoutId, PurchaseTokenStatus.PENDING);
        if (pendingTokens.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "PENDING checkout tokeni nisu pronadjeni.");
        }
        return pendingTokens;
    }

    private boolean isConfirmedToken(TourPurchaseToken token) {
        return token.getStatus() == null || token.getStatus() == PurchaseTokenStatus.CONFIRMED;
    }

    private TourExecution getActiveExecutionOrThrow(int touristId, String tourId) {
        return tourExecutionRepository.findByTouristIdAndTourIdAndStatus(touristId, tourId, EXECUTION_ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aktivna sesija ture nije pronadjena."));
    }

    private void applyStatusChange(Tour tour, String requestedStatus) {
        String status = requestedStatus.trim().toUpperCase();
        if (status.equals(tour.getStatus())) {
            return;
        }

        if (PUBLISHED.equals(status)) {
            publishTour(tour);
            return;
        }

        if (ARCHIVED.equals(status)) {
            archiveTour(tour);
            return;
        }

        if (DRAFT.equals(status)) {
            if (!DRAFT.equals(tour.getStatus())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Objavljena ili arhivirana tura se ne moze vratiti u draft.");
            }
            return;
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nepoznat status ture.");
    }

    private void publishTour(Tour tour) {
        validatePublishRequirements(tour);
        tour.setStatus(PUBLISHED);
        tour.setPublishedAt(LocalDateTime.now());
        tour.setArchivedAt(null);
    }

    private void archiveTour(Tour tour) {
        if (!PUBLISHED.equals(tour.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Samo objavljena tura moze biti arhivirana.");
        }
        tour.setStatus(ARCHIVED);
        tour.setArchivedAt(LocalDateTime.now());
    }

    private void validatePublishRequirements(Tour tour) {
        if (isBlank(tour.getName()) || isBlank(tour.getDescription()) || isBlank(tour.getDifficulty())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tura mora imati naziv, opis i tezinu pre objave.");
        }
        if (tour.getKeyPoints() == null || tour.getKeyPoints().size() < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tura mora imati najmanje dve kljucne tacke pre objave.");
        }
        if (tour.getTransportDurations() == null || tour.getTransportDurations().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tura mora imati bar jedno vreme obilaska pre objave.");
        }
        recalculateDistance(tour);
    }

    private Map<String, Integer> normalizeTransportDurations(Map<String, Integer> durations) {
        Map<String, Integer> normalized = new HashMap<>();
        durations.forEach((type, minutes) -> {
            if (type == null || minutes == null) {
                return;
            }
            String normalizedType = type.trim().toUpperCase();
            if (!ALLOWED_TRANSPORT_TYPES.contains(normalizedType)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nepoznat tip prevoza: " + type);
            }
            if (minutes <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vreme obilaska mora biti vece od 0 minuta.");
            }
            normalized.put(normalizedType, minutes);
        });
        return normalized;
    }

    private void recalculateDistance(Tour tour) {
        List<KeyPoint> points = tour.getKeyPoints() == null ? new ArrayList<>() : tour.getKeyPoints().stream()
                .sorted(Comparator.comparingInt(KeyPoint::getOrder))
                .toList();

        double distance = 0;
        for (int i = 1; i < points.size(); i++) {
            distance += calculateDistance(points.get(i - 1), points.get(i));
        }
        tour.setDistanceKm(Math.round(distance * 1000.0) / 1000.0);
    }

    private double calculateDistance(KeyPoint first, KeyPoint second) {
        return calculateDistance(first.getLatitude(), first.getLongitude(), second.getLatitude(), second.getLongitude());
    }

    private double calculateDistance(double firstLatitude, double firstLongitude, double secondLatitude, double secondLongitude) {
        double latDistance = Math.toRadians(secondLatitude - firstLatitude);
        double lonDistance = Math.toRadians(secondLongitude - firstLongitude);
        double firstLat = Math.toRadians(firstLatitude);
        double secondLat = Math.toRadians(secondLatitude);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(firstLat) * Math.cos(secondLat)
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private TourResponseDto mapToResponse(Tour tour) {
        return mapToResponse(tour, true);
    }

    private TourResponseDto mapToResponse(Tour tour, boolean includeAllKeyPoints) {
        List<KeyPoint> sortedPoints = tour.getKeyPoints() == null ? new ArrayList<>() : tour.getKeyPoints().stream()
                .sorted(Comparator.comparingInt(KeyPoint::getOrder))
                .toList();
        if (!includeAllKeyPoints && sortedPoints.size() > 1) {
            sortedPoints = sortedPoints.subList(0, 1);
        }

        List<KeyPointResponseDto> keyPoints = sortedPoints.stream()
                .map(kp -> new KeyPointResponseDto(
                        kp.getId(), kp.getName(), kp.getDescription(),
                        kp.getLatitude(), kp.getLongitude(), kp.getImageUrl(), kp.getOrder()))
                .toList();

        return new TourResponseDto(
                tour.getId(), tour.getAuthorId(), tour.getName(), tour.getDescription(),
                tour.getStatus(), tour.getDifficulty(), tour.getPrice(), tour.getDistanceKm(),
                tour.getTransportDurations() != null ? tour.getTransportDurations() : new HashMap<>(), keyPoints,
                tour.getTags() != null ? tour.getTags() : new ArrayList<>(),
                tour.getCreatedAt(), tour.getPublishedAt(), tour.getArchivedAt());
    }

    private ShoppingCartResponseDto mapCartToResponse(ShoppingCart cart) {
        List<OrderItemResponseDto> items = cart.getItems().stream()
                .map(item -> new OrderItemResponseDto(item.getTourId(), item.getTourName(), item.getPrice()))
                .toList();
        return new ShoppingCartResponseDto(cart.getTouristId(), items, cart.getTotalPrice());
    }

    private TourPurchaseTokenResponseDto mapPurchaseTokenToResponse(TourPurchaseToken token) {
        String status = token.getStatus() != null
                ? token.getStatus().name()
                : PurchaseTokenStatus.CONFIRMED.name();
        return new TourPurchaseTokenResponseDto(
                token.getTourId(),
                token.getToken(),
                status,
                token.getCheckoutId(),
                token.getCreatedAt());
    }

    private TourExecutionResponseDto mapExecutionToResponse(TourExecution execution) {
        List<CompletedKeyPoint> completed = execution.getCompletedKeyPoints() != null
                ? execution.getCompletedKeyPoints()
                : new ArrayList<>();
        List<CompletedKeyPointResponseDto> completedKeyPoints = completed.stream()
                .map(keyPoint -> new CompletedKeyPointResponseDto(
                        keyPoint.getKeyPointId(), keyPoint.getKeyPointName(), keyPoint.getReachedAt()))
                .toList();

        return new TourExecutionResponseDto(
                execution.getId(), execution.getTourId(), execution.getTouristId(), execution.getStatus(),
                execution.getStartLatitude(), execution.getStartLongitude(), execution.getStartedAt(),
                execution.getCompletedAt(), execution.getAbandonedAt(), execution.getLastActivity(),
                completedKeyPoints);
    }

    private ReviewResponseDto mapReviewToResponse(Review review) {
        return new ReviewResponseDto(
                review.getId(), review.getTourId(), review.getUserId(),
                review.getUsername(), review.getRating(), review.getComment(),
                review.getVisitDate(), review.getImageUrls(), review.getCreatedAt());
    }

    private record ValidatedCheckout(ShoppingCart cart, List<Tour> tours) {
    }
}
