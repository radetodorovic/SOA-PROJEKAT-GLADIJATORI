package com.gladijatori.tourservice.dto;

import java.util.List;

public record PrepareCheckoutResponseDto(
        String checkoutId,
        List<TourPurchaseTokenResponseDto> tokens
) {}
