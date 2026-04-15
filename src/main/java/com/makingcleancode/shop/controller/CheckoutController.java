package com.makingcleancode.shop.controller;

import com.makingcleancode.repository.entities.User;
import com.makingcleancode.service.JwtService;
import com.makingcleancode.shop.dto.request.CheckoutRequestDto;
import com.makingcleancode.shop.enums.PaymentMode;
import com.makingcleancode.shop.service.CheckoutService;
import com.makingcleancode.shop.service.IdempotencyService;
import com.shop.v1.model.CheckoutResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Checkout Self", description = "Endpoints relacionados con el proceso de checkout para clientes autenticados")
@RequestMapping("/api/v1/customers/self/checkout")
public class CheckoutController {

    private static final String CHECKOUT_ENDPOINT = "POST:/ms-store/api/v1/customers/self/checkout";

    private final CheckoutService checkoutService;
    private final IdempotencyService idempotencyService;
    private final JwtService jwtService;

    @Operation(summary = "CREAR CHECKOUT")
    @PostMapping
    public ResponseEntity<CheckoutResponseDto> createCheckout(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody com.shop.v1.model.CheckoutRequestDto apiRequest) {

        Long authUserId = jwtService.getAuthenticatedUser()
                .map(User::getId)
                .orElseThrow(() -> new IllegalStateException("Unauthorized"));

        // Mapear DTO OpenAPI → DTO interno
        CheckoutRequestDto request = new CheckoutRequestDto();
        request.setShippingAddressId(apiRequest.getShippingAddressId());
        request.setBillingAddressId(apiRequest.getBillingAddressId());
        request.setSuccessUrl(apiRequest.getSuccessUrl() != null ? apiRequest.getSuccessUrl().toString() : null);
        request.setCancelUrl(apiRequest.getCancelUrl() != null ? apiRequest.getCancelUrl().toString() : null);
        request.setNotes(apiRequest.getNotes());
        if (apiRequest.getPaymentMode() != null) {
            try {
                request.setPaymentMode(PaymentMode.valueOf(apiRequest.getPaymentMode().getValue()));
            } catch (IllegalArgumentException e) {
                request.setPaymentMode(PaymentMode.CHECKOUT_SESSION);
            }
        }

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

        // CheckoutService devuelve ResponseEntity<internal DTO> — adaptamos la respuesta
        var internalResponse = checkoutService.createCheckout(authUserId, request);

        // Construir respuesta OpenAPI desde el DTO interno
        CheckoutResponseDto apiResponse = mapToApiCheckoutResponse(internalResponse.getBody());

        idempotencyService.storeResponse(
                cached.getRecord(),
                ResponseEntity.status(internalResponse.getStatusCode()).body(apiResponse),
                "ORDER",
                apiResponse != null && apiResponse.getData() != null
                        ? apiResponse.getData().getOrderId()
                        : null
        );

        return ResponseEntity.status(internalResponse.getStatusCode()).body(apiResponse);
    }

    private CheckoutResponseDto mapToApiCheckoutResponse(
            com.makingcleancode.shop.dto.response.CheckoutResponseDto internal) {
        if (internal == null) return null;

        com.shop.v1.model.CheckoutDataDto data = null;
        if (internal.getData() != null) {
            var d = internal.getData();
            com.shop.v1.model.PaymentMode pm = null;
            com.shop.v1.model.PaymentStatus ps = null;
            try {
                if (d.getPaymentMode() != null) pm = com.shop.v1.model.PaymentMode.fromValue(d.getPaymentMode().name());
            } catch (Exception ignored) {}
            try {
                if (d.getPaymentStatus() != null) ps = com.shop.v1.model.PaymentStatus.fromValue(d.getPaymentStatus().name());
            } catch (Exception ignored) {}

            data = com.shop.v1.model.CheckoutDataDto.builder()
                    .orderId(d.getOrderId())
                    .orderNumber(d.getOrderNumber())
                    .paymentMode(pm)
                    .paymentStatus(ps)
                    .stripeCustomerId(d.getStripeCustomerId())
                    .stripePaymentIntentId(d.getStripePaymentIntentId())
                    .stripeCheckoutSessionId(d.getStripeCheckoutSessionId())
                    .clientSecret(d.getClientSecret())
                    .checkoutUrl(d.getCheckoutUrl() != null ? java.net.URI.create(d.getCheckoutUrl()) : null)
                    .build();
        }

        com.shop.v1.model.BasicStatusModel status = null;
        if (internal.getStatus() != null) {
            status = com.shop.v1.model.BasicStatusModel.builder()
                    .code(internal.getStatus().getCode())
                    .message(internal.getStatus().getMessage())
                    .build();
        }

        return CheckoutResponseDto.builder().status(status).data(data).build();
    }
}
