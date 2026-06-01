package com.gladijatori.tourservice.dto;

import java.util.List;

public record ShoppingCartResponseDto(
        int touristId,
        List<OrderItemResponseDto> items,
        double totalPrice
) {}
