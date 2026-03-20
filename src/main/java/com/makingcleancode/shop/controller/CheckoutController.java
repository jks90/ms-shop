package com.makingcleancode.shop.controller;

import com.makingcleancode.shop.dto.request.CheckoutRequestDto;
import com.makingcleancode.shop.dto.response.CheckoutResponseDto;
import com.makingcleancode.shop.security.AuthenticatedUser;
import com.makingcleancode.shop.service.CheckoutService;
import com.makingcleancode.shop.service.IdempotencyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ms-store/api/v1/customers/self")
public class CheckoutController {

    private static final String CHECKOUT_ENDPOINT = "POST:/ms-store/api/v1/customers/self/checkout";

    private final CheckoutService checkoutService;
    private final IdempotencyService idempotencyService;

    @PostMapping("/checkout")
    public ResponseEntity<CheckoutResponseDto> createCheckout(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody CheckoutRequestDto request,
            @RequestAttribute("authenticatedUser") AuthenticatedUser authenticatedUser
    ) {
        Long authUserId = authenticatedUser.userId();

        IdempotencyService.CachedResponse<CheckoutResponseDto> cached =
                idempotencyService.resolve(
                        authUserId,
                        CHECKOUT_ENDPOINT,
                        idempotencyKey,
                        request,
                        CheckoutResponseDto.class
                );

        if (cached.isReplayed()) {
            return ResponseEntity.status(cached.getStatusCode()).body(cached.getBody());
        }

        ResponseEntity<CheckoutResponseDto> response = checkoutService.createCheckout(authUserId, request);

        idempotencyService.storeResponse(
                cached.getRecord(),
                response,
                "ORDER",
                response.getBody() != null && response.getBody().getData() != null
                        ? response.getBody().getData().getOrderId()
                        : null
        );

        return response;
    }
}
