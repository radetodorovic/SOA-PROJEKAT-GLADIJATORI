package com.gladijatori.tourservice.grpc;

import com.gladijatori.tourservice.service.TourService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class TourGrpcService extends TourRpcGrpc.TourRpcImplBase {
    private final TourService tourService;

    @Override
    public void getPublishedTours(GetPublishedToursRequest request, StreamObserver<TourListReply> responseObserver) {
        try {
            TourListReply.Builder reply = TourListReply.newBuilder();
            tourService.getPublishedTours().stream()
                    .map(TourGrpcMapper::toReply)
                    .forEach(reply::addTours);
            responseObserver.onNext(reply.build());
            responseObserver.onCompleted();
        } catch (RuntimeException ex) {
            responseObserver.onError(toGrpcError(ex));
        }
    }

    @Override
    public void getTourById(GetTourByIdRequest request, StreamObserver<TourReply> responseObserver) {
        try {
            responseObserver.onNext(TourGrpcMapper.toReply(
                    tourService.getTourById(request.getId(), request.getUserId())));
            responseObserver.onCompleted();
        } catch (RuntimeException ex) {
            responseObserver.onError(toGrpcError(ex));
        }
    }

    private RuntimeException toGrpcError(RuntimeException ex) {
        if (ex instanceof ResponseStatusException statusException) {
            return mapHttpStatus(statusException.getStatusCode().value())
                    .withDescription(statusException.getReason())
                    .asRuntimeException();
        }
        return Status.INTERNAL
                .withDescription(ex.getMessage())
                .asRuntimeException();
    }

    private Status mapHttpStatus(int httpStatus) {
        return switch (httpStatus) {
            case 400 -> Status.INVALID_ARGUMENT;
            case 401 -> Status.UNAUTHENTICATED;
            case 403 -> Status.PERMISSION_DENIED;
            case 404 -> Status.NOT_FOUND;
            default -> Status.INTERNAL;
        };
    }
}
