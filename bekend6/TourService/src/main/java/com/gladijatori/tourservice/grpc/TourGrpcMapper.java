package com.gladijatori.tourservice.grpc;

import com.gladijatori.tourservice.dto.CompletedKeyPointResponseDto;
import com.gladijatori.tourservice.dto.CreateKeyPointDto;
import com.gladijatori.tourservice.dto.CreateTourDto;
import com.gladijatori.tourservice.dto.KeyPointResponseDto;
import com.gladijatori.tourservice.dto.OrderItemResponseDto;
import com.gladijatori.tourservice.dto.ShoppingCartResponseDto;
import com.gladijatori.tourservice.dto.TourExecutionResponseDto;
import com.gladijatori.tourservice.dto.TourPurchaseTokenResponseDto;
import com.gladijatori.tourservice.dto.TourResponseDto;
import com.gladijatori.tourservice.dto.UpdateTourDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

final class TourGrpcMapper {
    private TourGrpcMapper() {
    }

    static CreateTourDto toCreateTourDto(CreateTourRequest request) {
        return new CreateTourDto(
                request.getName(),
                request.getDescription(),
                request.getPrice(),
                emptyToNull(request.getDifficulty()),
                new ArrayList<>(request.getTagsList()));
    }

    static UpdateTourDto toUpdateTourDto(UpdateTourRequest request) {
        return new UpdateTourDto(
                request.hasName() ? request.getName().getValue() : null,
                request.hasDescription() ? request.getDescription().getValue() : null,
                request.hasPrice() ? request.getPrice().getValue() : null,
                request.hasDifficulty() ? request.getDifficulty().getValue() : null,
                request.hasStatus() ? request.getStatus().getValue() : null,
                request.hasTransportDurations() ? new HashMap<>(request.getTransportDurations().getValuesMap()) : null,
                request.hasTags() ? new ArrayList<>(request.getTags().getValuesList()) : null);
    }

    static CreateKeyPointDto toCreateKeyPointDto(AddKeyPointRequest request) {
        return new CreateKeyPointDto(
                request.getName(),
                emptyToNull(request.getDescription()),
                request.hasLatitude() ? request.getLatitude().getValue() : null,
                request.hasLongitude() ? request.getLongitude().getValue() : null,
                emptyToNull(request.getImageUrl()),
                request.getOrder());
    }

    static TourReply toReply(TourResponseDto tour) {
        TourReply.Builder builder = TourReply.newBuilder()
                .setId(nullToEmpty(tour.id()))
                .setAuthorId(tour.authorId())
                .setName(nullToEmpty(tour.name()))
                .setDescription(nullToEmpty(tour.description()))
                .setStatus(nullToEmpty(tour.status()))
                .setDifficulty(nullToEmpty(tour.difficulty()))
                .setPrice(tour.price())
                .setDistanceKm(tour.distanceKm())
                .setCreatedAt(formatDateTime(tour.createdAt()))
                .setPublishedAt(formatDateTime(tour.publishedAt()))
                .setArchivedAt(formatDateTime(tour.archivedAt()));

        if (tour.transportDurations() != null) {
            builder.putAllTransportDurations(tour.transportDurations());
        }
        if (tour.tags() != null) {
            builder.addAllTags(tour.tags());
        }
        if (tour.keyPoints() != null) {
            tour.keyPoints().stream()
                    .map(TourGrpcMapper::toKeyPointReply)
                    .forEach(builder::addKeyPoints);
        }

        return builder.build();
    }

    static ShoppingCartReply toReply(ShoppingCartResponseDto cart) {
        ShoppingCartReply.Builder builder = ShoppingCartReply.newBuilder()
                .setTouristId(cart.touristId())
                .setTotalPrice(cart.totalPrice());

        if (cart.items() != null) {
            cart.items().stream()
                    .map(TourGrpcMapper::toOrderItemReply)
                    .forEach(builder::addItems);
        }

        return builder.build();
    }

    static PurchaseTokenListReply toReply(List<TourPurchaseTokenResponseDto> tokens) {
        PurchaseTokenListReply.Builder builder = PurchaseTokenListReply.newBuilder();
        if (tokens != null) {
            tokens.stream()
                    .map(TourGrpcMapper::toPurchaseTokenReply)
                    .forEach(builder::addTokens);
        }
        return builder.build();
    }

    static TourExecutionReply toReply(TourExecutionResponseDto execution) {
        TourExecutionReply.Builder builder = TourExecutionReply.newBuilder()
                .setId(nullToEmpty(execution.id()))
                .setTourId(nullToEmpty(execution.tourId()))
                .setTouristId(execution.touristId())
                .setStatus(nullToEmpty(execution.status()))
                .setStartLatitude(execution.startLatitude())
                .setStartLongitude(execution.startLongitude())
                .setStartedAt(formatDateTime(execution.startedAt()))
                .setCompletedAt(formatDateTime(execution.completedAt()))
                .setAbandonedAt(formatDateTime(execution.abandonedAt()))
                .setLastActivity(formatDateTime(execution.lastActivity()));

        if (execution.completedKeyPoints() != null) {
            execution.completedKeyPoints().stream()
                    .map(TourGrpcMapper::toCompletedKeyPointReply)
                    .forEach(builder::addCompletedKeyPoints);
        }

        return builder.build();
    }

    private static KeyPointReply toKeyPointReply(KeyPointResponseDto keyPoint) {
        return KeyPointReply.newBuilder()
                .setId(nullToEmpty(keyPoint.id()))
                .setName(nullToEmpty(keyPoint.name()))
                .setDescription(nullToEmpty(keyPoint.description()))
                .setLatitude(keyPoint.latitude())
                .setLongitude(keyPoint.longitude())
                .setImageUrl(nullToEmpty(keyPoint.imageUrl()))
                .setOrder(keyPoint.order())
                .build();
    }

    private static OrderItemReply toOrderItemReply(OrderItemResponseDto item) {
        return OrderItemReply.newBuilder()
                .setTourId(nullToEmpty(item.tourId()))
                .setTourName(nullToEmpty(item.tourName()))
                .setPrice(item.price())
                .build();
    }

    private static TourPurchaseTokenReply toPurchaseTokenReply(TourPurchaseTokenResponseDto token) {
        return TourPurchaseTokenReply.newBuilder()
                .setTourId(nullToEmpty(token.tourId()))
                .setToken(nullToEmpty(token.token()))
                .setStatus(nullToEmpty(token.status()))
                .setCheckoutId(nullToEmpty(token.checkoutId()))
                .setCreatedAt(formatDateTime(token.createdAt()))
                .build();
    }

    private static CompletedKeyPointReply toCompletedKeyPointReply(CompletedKeyPointResponseDto keyPoint) {
        return CompletedKeyPointReply.newBuilder()
                .setKeyPointId(nullToEmpty(keyPoint.keyPointId()))
                .setKeyPointName(nullToEmpty(keyPoint.keyPointName()))
                .setReachedAt(formatDateTime(keyPoint.reachedAt()))
                .build();
    }

    private static String formatDateTime(LocalDateTime value) {
        return value != null ? value.toString() : "";
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
