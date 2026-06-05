package com.gladijatori.tourservice.security;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InternalEndpointGuardTest {

    @Test
    void allowsMatchingInternalApiKey() {
        InternalEndpointGuard guard = new InternalEndpointGuard("expected-key");

        assertDoesNotThrow(() -> guard.requireInternalAccess("expected-key"));
    }

    @Test
    void rejectsMissingOrInvalidInternalApiKey() {
        InternalEndpointGuard guard = new InternalEndpointGuard("expected-key");

        ResponseStatusException missing = assertThrows(
                ResponseStatusException.class,
                () -> guard.requireInternalAccess(null));
        ResponseStatusException invalid = assertThrows(
                ResponseStatusException.class,
                () -> guard.requireInternalAccess("wrong-key"));

        assertEquals(HttpStatus.NOT_FOUND, missing.getStatusCode());
        assertEquals("Ruta nije pronadjena.", missing.getReason());
        assertEquals(HttpStatus.NOT_FOUND, invalid.getStatusCode());
        assertEquals("Ruta nije pronadjena.", invalid.getReason());
    }
}
