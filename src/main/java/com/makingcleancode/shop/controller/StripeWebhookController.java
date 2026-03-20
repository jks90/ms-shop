package com.makingcleancode.shop.controller;

import com.makingcleancode.shop.dto.response.BasicResponseModel;
import com.makingcleancode.shop.dto.response.BasicStatusModel;
import com.makingcleancode.shop.service.PaymentTransactionService;
import com.makingcleancode.shop.service.StripeWebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ms-store/api/v1/payments/stripe")
public class StripeWebhookController {

    private final StripeWebhookService stripeWebhookService;

    @PostMapping("/webhook")
    public ResponseEntity<BasicResponseModel> handleStripeWebhook(
            @RequestHeader("Stripe-Signature") String stripeSignature,
            @RequestBody String payload
    ) {
        stripeWebhookService.handleWebhook(payload, stripeSignature);

        return ResponseEntity.ok(
                BasicResponseModel.builder()
                        .status(BasicStatusModel.builder()
                                .message("Webhook processed")
                                .code(200)
                                .build())
                        .build()
        );
    }
}