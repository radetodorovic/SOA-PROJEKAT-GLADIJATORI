package com.gladijatori.tourservice.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class InternalEndpointGuard {
    private final String expectedApiKey;

    public InternalEndpointGuard(@Value("${tour.internal-api-key}") String expectedApiKey) {
        this.expectedApiKey = expectedApiKey;
    }

    public void requireInternalAccess(String providedApiKey) {
        if (providedApiKey == null || providedApiKey.isBlank() || !expectedApiKey.equals(providedApiKey)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ruta nije pronadjena.");
        }
    }
}
