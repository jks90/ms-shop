package com.makingcleancode.shop.service.dto;


import lombok.Builder;

@Builder
public record CheckoutLineItem(
        String name,
        String description,
        Long unitAmount,
        String currency,
        Long quantity
) {
}