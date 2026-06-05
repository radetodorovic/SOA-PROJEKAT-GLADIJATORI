package com.gladijatori.tourservice.controller;

import com.gladijatori.tourservice.dto.PrepareExecutionStartResponseDto;
import com.gladijatori.tourservice.dto.TourExecutionResponseDto;
import com.gladijatori.tourservice.security.InternalEndpointGuard;
import com.gladijatori.tourservice.service.TourService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TourControllerTest {

    @Mock
    private TourService tourService;
    @Mock
    private InternalEndpointGuard internalEndpointGuard;

    @InjectMocks
    private TourController tourController;

    @Test
    void getActiveExecution_returnsNoContentWhenThereIsNoActiveSession() {
        when(tourService.getActiveExecution("tour-1", 22)).thenReturn(null);

        ResponseEntity<TourExecutionResponseDto> response = tourController.getActiveExecution("tour-1", 22);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void getActiveExecution_returnsOkWhenActiveSessionExists() {
        TourExecutionResponseDto execution = new TourExecutionResponseDto(
                "exec-1",
                "tour-1",
                22,
                "ACTIVE",
                45.0,
                19.0,
                null,
                null,
                null,
                null,
                List.of());
        when(tourService.getActiveExecution("tour-1", 22)).thenReturn(execution);

        ResponseEntity<TourExecutionResponseDto> response = tourController.getActiveExecution("tour-1", 22);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(execution, response.getBody());
    }

    @Test
    void compensateStartExecution_returnsNoContentWhenThereIsNoActiveSession() {
        when(tourService.compensateStartTourExecution("tour-1", 22)).thenReturn(null);

        ResponseEntity<TourExecutionResponseDto> response = tourController.compensateStartExecution("tour-1", 22, "internal-key");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void prepareStartExecution_returnsCreatedPreparationResponse() {
        TourExecutionResponseDto execution = new TourExecutionResponseDto(
                "exec-1",
                "tour-1",
                22,
                "ACTIVE",
                45.0,
                19.0,
                null,
                null,
                null,
                null,
                List.of());
        PrepareExecutionStartResponseDto prepared = new PrepareExecutionStartResponseDto(execution, true);
        when(tourService.prepareStartTourExecution("tour-1", 22)).thenReturn(prepared);

        ResponseEntity<PrepareExecutionStartResponseDto> response = tourController.prepareStartExecution("tour-1", 22, "internal-key");

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(prepared, response.getBody());
    }
}
