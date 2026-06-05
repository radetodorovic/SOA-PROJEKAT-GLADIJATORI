package com.gladijatori.tourservice.grpc;

import com.gladijatori.tourservice.dto.CreateKeyPointDto;
import com.gladijatori.tourservice.dto.CreateTourDto;
import com.gladijatori.tourservice.service.TourService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class TourGrpcService extends TourRpcGrpc.TourRpcImplBase {
    private final TourService tourService;
    private final Validator validator;

    @Override
    public void getPublishedTours(GetPublishedToursRequest request, StreamObserver<TourListReply> responseObserver) {
        handleUnary(responseObserver, () -> {
            TourListReply.Builder reply = TourListReply.newBuilder();
            tourService.getPublishedTours().stream()
                    .map(TourGrpcMapper::toReply)
                    .forEach(reply::addTours);
            return reply.build();
        });
    }

    @Override
    public void getTourById(GetTourByIdRequest request, StreamObserver<TourReply> responseObserver) {
        handleUnary(responseObserver, () -> TourGrpcMapper.toReply(
                tourService.getTourById(requireText(request.getId(), "Id ture je obavezan."), request.getUserId())));
    }

    @Override
    public void createTour(CreateTourRequest request, StreamObserver<TourReply> responseObserver) {
        handleUnary(responseObserver, () -> {
            CreateTourDto dto = validate(TourGrpcMapper.toCreateTourDto(request));
            return TourGrpcMapper.toReply(tourService.createTour(request.getUserId(), dto));
        });
    }

    @Override
    public void updateTour(UpdateTourRequest request, StreamObserver<TourReply> responseObserver) {
        handleUnary(responseObserver, () -> TourGrpcMapper.toReply(
                tourService.updateTour(
                        requireText(request.getId(), "Id ture je obavezan."),
                        request.getUserId(),
                        TourGrpcMapper.toUpdateTourDto(request))));
    }

    @Override
    public void addKeyPoint(AddKeyPointRequest request, StreamObserver<TourReply> responseObserver) {
        handleUnary(responseObserver, () -> {
            CreateKeyPointDto dto = validate(TourGrpcMapper.toCreateKeyPointDto(request));
            return TourGrpcMapper.toReply(
                    tourService.addKeyPoint(
                            requireText(request.getTourId(), "Id ture je obavezan."),
                            request.getUserId(),
                            dto));
        });
    }

    @Override
    public void addToCart(AddToCartRequest request, StreamObserver<ShoppingCartReply> responseObserver) {
        handleUnary(responseObserver, () -> TourGrpcMapper.toReply(
                tourService.addToCart(request.getUserId(), requireText(request.getTourId(), "Id ture je obavezan."))));
    }

    @Override
    public void checkout(CheckoutRequest request, StreamObserver<PurchaseTokenListReply> responseObserver) {
        handleUnary(responseObserver, () -> TourGrpcMapper.toReply(tourService.checkout(request.getUserId())));
    }

    @Override
    public void startTourExecution(TourExecutionActionRequest request, StreamObserver<TourExecutionReply> responseObserver) {
        handleUnary(responseObserver, () -> TourGrpcMapper.toReply(
                tourService.startTourExecution(requireText(request.getTourId(), "Id ture je obavezan."), request.getUserId())));
    }

    @Override
    public void checkExecutionProgress(TourExecutionActionRequest request, StreamObserver<TourExecutionReply> responseObserver) {
        handleUnary(responseObserver, () -> TourGrpcMapper.toReply(
                tourService.checkKeyPointProximity(requireText(request.getTourId(), "Id ture je obavezan."), request.getUserId())));
    }

    @Override
    public void completeExecution(TourExecutionActionRequest request, StreamObserver<TourExecutionReply> responseObserver) {
        handleUnary(responseObserver, () -> TourGrpcMapper.toReply(
                tourService.completeTourExecution(requireText(request.getTourId(), "Id ture je obavezan."), request.getUserId())));
    }

    @Override
    public void abandonExecution(TourExecutionActionRequest request, StreamObserver<TourExecutionReply> responseObserver) {
        handleUnary(responseObserver, () -> TourGrpcMapper.toReply(
                tourService.abandonTourExecution(requireText(request.getTourId(), "Id ture je obavezan."), request.getUserId())));
    }

    private <T> void handleUnary(StreamObserver<T> responseObserver, Supplier<T> action) {
        try {
            responseObserver.onNext(action.get());
            responseObserver.onCompleted();
        } catch (RuntimeException ex) {
            responseObserver.onError(toGrpcError(ex));
        }
    }

    private <T> T validate(T dto) {
        var violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .findFirst()
                    .orElse("Neispravan zahtev.");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
        return dto;
    }

    private String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
        return value;
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
