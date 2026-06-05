package com.gladijatori.tourservice.service;

import com.gladijatori.tourservice.dto.TourResponseDto;
import com.gladijatori.tourservice.dto.PrepareExecutionStartResponseDto;
import com.gladijatori.tourservice.model.KeyPoint;
import com.gladijatori.tourservice.model.OrderItem;
import com.gladijatori.tourservice.model.PurchaseTokenStatus;
import com.gladijatori.tourservice.model.ShoppingCart;
import com.gladijatori.tourservice.model.Tour;
import com.gladijatori.tourservice.model.TourExecution;
import com.gladijatori.tourservice.model.TourPurchaseToken;
import com.gladijatori.tourservice.repository.ReviewRepository;
import com.gladijatori.tourservice.repository.ShoppingCartRepository;
import com.gladijatori.tourservice.repository.TourExecutionRepository;
import com.gladijatori.tourservice.repository.TourPurchaseTokenRepository;
import com.gladijatori.tourservice.repository.TourRepository;
import com.gladijatori.tourservice.repository.TouristPositionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TourServiceTest {

    private static final String TOUR_ID = "507f1f77bcf86cd799439011";
    private static final int AUTHOR_ID = 11;
    private static final int TOURIST_ID = 22;

    @Mock
    private TourRepository tourRepository;
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private TouristPositionRepository positionRepository;
    @Mock
    private ShoppingCartRepository cartRepository;
    @Mock
    private TourPurchaseTokenRepository purchaseTokenRepository;
    @Mock
    private TourExecutionRepository tourExecutionRepository;

    @InjectMocks
    private TourService tourService;

    @Test
    void getTourById_allowsAuthorToViewDraftTour() {
        Tour draftTour = createTour("DRAFT");
        when(tourRepository.findById(TOUR_ID)).thenReturn(Optional.of(draftTour));

        TourResponseDto response = tourService.getTourById(TOUR_ID, AUTHOR_ID);

        assertEquals("DRAFT", response.status());
        assertEquals(2, response.keyPoints().size());
    }

    @Test
    void getTourById_allowsAnonymousUserToViewPublishedTour() {
        Tour publishedTour = createTour("PUBLISHED");
        when(tourRepository.findById(TOUR_ID)).thenReturn(Optional.of(publishedTour));

        TourResponseDto response = tourService.getTourById(TOUR_ID, 0);

        assertEquals("PUBLISHED", response.status());
        assertEquals(1, response.keyPoints().size());
    }

    @Test
    void getTourById_rejectsDraftTourForNonAuthor() {
        Tour draftTour = createTour("DRAFT");
        when(tourRepository.findById(TOUR_ID)).thenReturn(Optional.of(draftTour));
        when(purchaseTokenRepository.findByTouristIdAndTourId(TOURIST_ID, TOUR_ID)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> tourService.getTourById(TOUR_ID, TOURIST_ID));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Tura nije pronadjena.", exception.getReason());
    }

    @Test
    void getTourById_allowsArchivedTourForBuyer() {
        Tour archivedTour = createTour("ARCHIVED");
        when(tourRepository.findById(TOUR_ID)).thenReturn(Optional.of(archivedTour));
        when(purchaseTokenRepository.findByTouristIdAndTourId(TOURIST_ID, TOUR_ID))
                .thenReturn(Optional.of(createToken(TOUR_ID, PurchaseTokenStatus.CONFIRMED)));

        TourResponseDto response = tourService.getTourById(TOUR_ID, TOURIST_ID);

        assertEquals("ARCHIVED", response.status());
        assertEquals(2, response.keyPoints().size());
    }

    @Test
    void getTourById_rejectsArchivedTourWithoutPurchase() {
        Tour archivedTour = createTour("ARCHIVED");
        when(tourRepository.findById(TOUR_ID)).thenReturn(Optional.of(archivedTour));
        when(purchaseTokenRepository.findByTouristIdAndTourId(TOURIST_ID, TOUR_ID)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> tourService.getTourById(TOUR_ID, TOURIST_ID));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Tura nije pronadjena.", exception.getReason());
    }

    @Test
    void getTourById_rejectsArchivedTourForPendingPurchaseToken() {
        Tour archivedTour = createTour("ARCHIVED");
        when(tourRepository.findById(TOUR_ID)).thenReturn(Optional.of(archivedTour));
        when(purchaseTokenRepository.findByTouristIdAndTourId(TOURIST_ID, TOUR_ID))
                .thenReturn(Optional.of(createToken(TOUR_ID, PurchaseTokenStatus.PENDING)));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> tourService.getTourById(TOUR_ID, TOURIST_ID));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Tura nije pronadjena.", exception.getReason());
    }

    @Test
    void getPublishedTours_keepsPublishedListAvailableForUi() {
        when(tourRepository.findByStatus("PUBLISHED")).thenReturn(List.of(createTour("PUBLISHED")));

        List<TourResponseDto> response = tourService.getPublishedTours();

        assertEquals(1, response.size());
        assertEquals("PUBLISHED", response.getFirst().status());
        assertEquals(1, response.getFirst().keyPoints().size());
    }

    @Test
    void getToursByAuthor_keepsAuthorPreviewAcrossStatuses() {
        when(tourRepository.findByAuthorId(AUTHOR_ID)).thenReturn(List.of(
                createTour("DRAFT"),
                createTour("PUBLISHED"),
                createTour("ARCHIVED")));

        List<TourResponseDto> response = tourService.getToursByAuthor(AUTHOR_ID);

        assertEquals(3, response.size());
        assertEquals(List.of("DRAFT", "PUBLISHED", "ARCHIVED"), response.stream().map(TourResponseDto::status).toList());
        assertEquals(2, response.getFirst().keyPoints().size());
    }

    @Test
    void getPurchasedTours_returnsPublishedAndArchivedToursForTourist() {
        Tour publishedTour = createTour(TOUR_ID, "PUBLISHED");
        Tour archivedTour = createTour("507f1f77bcf86cd799439012", "ARCHIVED");
        when(purchaseTokenRepository.findByTouristId(TOURIST_ID)).thenReturn(List.of(
                createToken(TOUR_ID, PurchaseTokenStatus.CONFIRMED),
                createToken(archivedTour.getId(), PurchaseTokenStatus.CONFIRMED)));
        when(tourRepository.findById(TOUR_ID)).thenReturn(Optional.of(publishedTour));
        when(tourRepository.findById(archivedTour.getId())).thenReturn(Optional.of(archivedTour));

        List<TourResponseDto> response = tourService.getPurchasedTours(TOURIST_ID);

        assertEquals(2, response.size());
        assertEquals(List.of("PUBLISHED", "ARCHIVED"), response.stream().map(TourResponseDto::status).toList());
        assertEquals(2, response.getFirst().keyPoints().size());
        assertEquals(2, response.get(1).keyPoints().size());
    }

    @Test
    void getPurchasedTours_excludesDraftToursEvenIfTokenExists() {
        Tour draftTour = createTour("507f1f77bcf86cd799439013", "DRAFT");
        when(purchaseTokenRepository.findByTouristId(TOURIST_ID))
                .thenReturn(List.of(createToken(draftTour.getId(), PurchaseTokenStatus.CONFIRMED)));
        when(tourRepository.findById(draftTour.getId())).thenReturn(Optional.of(draftTour));

        List<TourResponseDto> response = tourService.getPurchasedTours(TOURIST_ID);

        assertEquals(0, response.size());
    }

    @Test
    void getPurchasedTours_doesNotReturnArchivedTourWithoutPurchaseToken() {
        Tour publishedTour = createTour("507f1f77bcf86cd799439015", "PUBLISHED");
        when(purchaseTokenRepository.findByTouristId(TOURIST_ID))
                .thenReturn(List.of(createToken(publishedTour.getId(), PurchaseTokenStatus.CONFIRMED)));
        when(tourRepository.findById(publishedTour.getId())).thenReturn(Optional.of(publishedTour));

        List<TourResponseDto> response = tourService.getPurchasedTours(TOURIST_ID);

        assertEquals(1, response.size());
        assertEquals(List.of(publishedTour.getId()), response.stream().map(TourResponseDto::id).toList());
    }

    @Test
    void prepareCheckout_createsPendingTokensWithoutClearingCart() {
        Tour publishedTour = createTour("PUBLISHED");
        ShoppingCart cart = createCart(TOUR_ID);
        when(cartRepository.findByTouristId(TOURIST_ID)).thenReturn(Optional.of(cart));
        when(tourRepository.findById(TOUR_ID)).thenReturn(Optional.of(publishedTour));
        when(purchaseTokenRepository.findByTouristIdAndTourId(TOURIST_ID, TOUR_ID)).thenReturn(Optional.empty());
        when(purchaseTokenRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = tourService.prepareCheckout(TOURIST_ID);

        assertNotNull(response.checkoutId());
        assertEquals(1, response.tokens().size());
        assertEquals("PENDING", response.tokens().getFirst().status());
        assertEquals(1, cart.getItems().size());
        verify(cartRepository, never()).save(any(ShoppingCart.class));
    }

    @Test
    void prepareCheckout_cancelsPersistedPendingTokensWhenSaveFails() {
        Tour publishedTour = createTour("PUBLISHED");
        ShoppingCart cart = createCart(TOUR_ID);
        AtomicReference<TourPurchaseToken> cancelledToken = new AtomicReference<>();
        when(cartRepository.findByTouristId(TOURIST_ID)).thenReturn(Optional.of(cart));
        when(tourRepository.findById(TOUR_ID)).thenReturn(Optional.of(publishedTour));
        when(purchaseTokenRepository.findByTouristIdAndTourId(TOURIST_ID, TOUR_ID)).thenReturn(Optional.empty());
        when(purchaseTokenRepository.saveAll(anyList()))
                .thenThrow(new RuntimeException("mongo failure"))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(purchaseTokenRepository.findByTouristIdAndCheckoutIdAndStatus(eq(TOURIST_ID), anyString(), eq(PurchaseTokenStatus.PENDING)))
                .thenAnswer(invocation -> {
                    TourPurchaseToken token = createToken(TOUR_ID, PurchaseTokenStatus.PENDING, invocation.getArgument(1));
                    cancelledToken.set(token);
                    return List.of(token);
                });

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> tourService.prepareCheckout(TOURIST_ID));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        assertEquals("Kreiranje PENDING tokena nije uspelo.", exception.getReason());
        assertEquals(PurchaseTokenStatus.CANCELLED, cancelledToken.get().getStatus());
        verify(purchaseTokenRepository, times(2)).saveAll(anyList());
    }

    @Test
    void confirmPendingCheckout_confirmsTokensAndClearsCart() {
        ShoppingCart cart = createCart(TOUR_ID);
        TourPurchaseToken pendingToken = createToken(TOUR_ID, PurchaseTokenStatus.PENDING, "checkout-1");
        when(purchaseTokenRepository.findByTouristIdAndCheckoutIdAndStatus(TOURIST_ID, "checkout-1", PurchaseTokenStatus.PENDING))
                .thenReturn(List.of(pendingToken));
        when(purchaseTokenRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(cartRepository.findByTouristId(TOURIST_ID)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(ShoppingCart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = tourService.confirmPendingCheckout(TOURIST_ID, "checkout-1");

        assertEquals(1, response.size());
        assertEquals("CONFIRMED", response.getFirst().status());
        assertEquals(PurchaseTokenStatus.CONFIRMED, pendingToken.getStatus());
        assertTrue(cart.getItems().isEmpty());
        assertEquals(0.0, cart.getTotalPrice());
    }

    @Test
    void confirmPendingCheckout_restoresPendingTokensWhenCartSaveFails() {
        ShoppingCart cart = createCart(TOUR_ID);
        TourPurchaseToken pendingToken = createToken(TOUR_ID, PurchaseTokenStatus.PENDING, "checkout-rollback");
        when(purchaseTokenRepository.findByTouristIdAndCheckoutIdAndStatus(TOURIST_ID, "checkout-rollback", PurchaseTokenStatus.PENDING))
                .thenReturn(List.of(pendingToken));
        when(purchaseTokenRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(cartRepository.findByTouristId(TOURIST_ID)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(ShoppingCart.class)))
                .thenThrow(new RuntimeException("mongo failure"))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> tourService.confirmPendingCheckout(TOURIST_ID, "checkout-rollback"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        assertEquals("Potvrda checkout-a nije uspela.", exception.getReason());
        assertEquals(PurchaseTokenStatus.PENDING, pendingToken.getStatus());
        verify(cartRepository, times(2)).save(any(ShoppingCart.class));
        verify(purchaseTokenRepository, times(2)).saveAll(anyList());
    }

    @Test
    void cancelPendingCheckout_marksTokensCancelledAndKeepsCartUntouched() {
        TourPurchaseToken pendingToken = createToken(TOUR_ID, PurchaseTokenStatus.PENDING, "checkout-2");
        when(purchaseTokenRepository.findByTouristIdAndCheckoutIdAndStatus(TOURIST_ID, "checkout-2", PurchaseTokenStatus.PENDING))
                .thenReturn(List.of(pendingToken));
        when(purchaseTokenRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        tourService.cancelPendingCheckout(TOURIST_ID, "checkout-2");

        assertEquals(PurchaseTokenStatus.CANCELLED, pendingToken.getStatus());
        verify(cartRepository, never()).save(any(ShoppingCart.class));
    }

    @Test
    void prepareStartTourExecution_createsActiveExecutionForConfirmedArchivedPurchase() {
        Tour archivedTour = createTour("ARCHIVED");
        TourExecution savedExecution = createExecution("ACTIVE");
        when(tourRepository.findById(TOUR_ID)).thenReturn(Optional.of(archivedTour));
        when(purchaseTokenRepository.findByTouristIdAndTourId(TOURIST_ID, TOUR_ID))
                .thenReturn(Optional.of(createToken(TOUR_ID, PurchaseTokenStatus.CONFIRMED)));
        when(tourExecutionRepository.findByTouristIdAndTourIdAndStatus(TOURIST_ID, TOUR_ID, "ACTIVE"))
                .thenReturn(Optional.empty());
        when(positionRepository.findByUserId(TOURIST_ID)).thenReturn(Optional.of(createPosition()));
        when(tourExecutionRepository.save(any(TourExecution.class))).thenReturn(savedExecution);

        PrepareExecutionStartResponseDto response = tourService.prepareStartTourExecution(TOUR_ID, TOURIST_ID);

        assertTrue(response.created());
        assertEquals("ACTIVE", response.execution().status());
        assertEquals("exec-1", response.execution().id());
    }

    @Test
    void prepareStartTourExecution_reusesExistingActiveExecutionWithoutCreatingNewOne() {
        Tour publishedTour = createTour("PUBLISHED");
        TourExecution existingExecution = createExecution("ACTIVE");
        when(tourRepository.findById(TOUR_ID)).thenReturn(Optional.of(publishedTour));
        when(purchaseTokenRepository.findByTouristIdAndTourId(TOURIST_ID, TOUR_ID))
                .thenReturn(Optional.of(createToken(TOUR_ID, PurchaseTokenStatus.CONFIRMED)));
        when(tourExecutionRepository.findByTouristIdAndTourIdAndStatus(TOURIST_ID, TOUR_ID, "ACTIVE"))
                .thenReturn(Optional.of(existingExecution));

        PrepareExecutionStartResponseDto response = tourService.prepareStartTourExecution(TOUR_ID, TOURIST_ID);

        assertTrue(!response.created());
        assertEquals(existingExecution.getId(), response.execution().id());
        verify(tourExecutionRepository, never()).save(any(TourExecution.class));
    }

    @Test
    void prepareStartTourExecution_rejectsPendingPurchaseToken() {
        Tour publishedTour = createTour("PUBLISHED");
        when(tourRepository.findById(TOUR_ID)).thenReturn(Optional.of(publishedTour));
        when(purchaseTokenRepository.findByTouristIdAndTourId(TOURIST_ID, TOUR_ID))
                .thenReturn(Optional.of(createToken(TOUR_ID, PurchaseTokenStatus.PENDING)));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> tourService.prepareStartTourExecution(TOUR_ID, TOURIST_ID));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals("Tura mora biti kupljena pre pokretanja.", exception.getReason());
    }

    @Test
    void compensateStartTourExecution_abandonsActiveExecution() {
        TourExecution activeExecution = createExecution("ACTIVE");
        when(tourExecutionRepository.findByTouristIdAndTourIdAndStatus(TOURIST_ID, TOUR_ID, "ACTIVE"))
                .thenReturn(Optional.of(activeExecution));
        when(tourExecutionRepository.save(any(TourExecution.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = tourService.compensateStartTourExecution(TOUR_ID, TOURIST_ID);

        assertEquals("ABANDONED", response.status());
        assertEquals("ABANDONED", activeExecution.getStatus());
        assertNotNull(activeExecution.getAbandonedAt());
    }

    private Tour createTour(String status) {
        return createTour(TOUR_ID, status);
    }

    private Tour createTour(String id, String status) {
        Tour tour = new Tour();
        tour.setId(id);
        tour.setAuthorId(AUTHOR_ID);
        tour.setName("Petrovaradin");
        tour.setDescription("Opis ture");
        tour.setStatus(status);
        tour.setDifficulty("EASY");
        tour.setKeyPoints(List.of(
                createKeyPoint("kp-1", "Prva", 1),
                createKeyPoint("kp-2", "Druga", 2)));
        return tour;
    }

    private KeyPoint createKeyPoint(String id, String name, int order) {
        KeyPoint keyPoint = new KeyPoint();
        keyPoint.setId(id);
        keyPoint.setName(name);
        keyPoint.setDescription(name + " opis");
        keyPoint.setLatitude(45.0 + order);
        keyPoint.setLongitude(19.0 + order);
        keyPoint.setOrder(order);
        return keyPoint;
    }

    private ShoppingCart createCart(String... tourIds) {
        ShoppingCart cart = new ShoppingCart();
        cart.setTouristId(TOURIST_ID);
        cart.setItems(new java.util.ArrayList<>());
        for (String tourId : tourIds) {
            OrderItem item = new OrderItem();
            item.setTourId(tourId);
            item.setTourName("Tura " + tourId);
            item.setPrice(30.0);
            cart.getItems().add(item);
        }
        cart.setTotalPrice(cart.getItems().stream().mapToDouble(OrderItem::getPrice).sum());
        return cart;
    }

    private TourPurchaseToken createToken(String tourId, PurchaseTokenStatus status) {
        return createToken(tourId, status, null);
    }

    private TourPurchaseToken createToken(String tourId, PurchaseTokenStatus status, String checkoutId) {
        TourPurchaseToken token = new TourPurchaseToken();
        token.setTourId(tourId);
        token.setTouristId(TOURIST_ID);
        token.setCheckoutId(checkoutId);
        token.setToken("token-" + tourId);
        token.setStatus(status);
        token.setCreatedAt(LocalDateTime.now());
        return token;
    }

    private TourExecution createExecution(String status) {
        TourExecution execution = new TourExecution();
        execution.setId("exec-1");
        execution.setTourId(TOUR_ID);
        execution.setTouristId(TOURIST_ID);
        execution.setStatus(status);
        execution.setStartLatitude(45.0);
        execution.setStartLongitude(19.0);
        execution.setStartedAt(LocalDateTime.now());
        execution.setLastActivity(LocalDateTime.now());
        execution.setCompletedKeyPoints(List.of());
        return execution;
    }

    private com.gladijatori.tourservice.model.TouristPosition createPosition() {
        com.gladijatori.tourservice.model.TouristPosition position = new com.gladijatori.tourservice.model.TouristPosition();
        position.setUserId(TOURIST_ID);
        position.setLatitude(45.0);
        position.setLongitude(19.0);
        position.setUpdatedAt(LocalDateTime.now());
        return position;
    }
}
