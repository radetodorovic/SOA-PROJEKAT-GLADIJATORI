package com.gladijatori.tourservice.grpc;

import com.gladijatori.tourservice.dto.KeyPointResponseDto;
import com.gladijatori.tourservice.dto.TourResponseDto;

final class TourGrpcMapper {
    private TourGrpcMapper() {
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
                .setCreatedAt(tour.createdAt() != null ? tour.createdAt().toString() : "")
                .setPublishedAt(tour.publishedAt() != null ? tour.publishedAt().toString() : "")
                .setArchivedAt(tour.archivedAt() != null ? tour.archivedAt().toString() : "");

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

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
