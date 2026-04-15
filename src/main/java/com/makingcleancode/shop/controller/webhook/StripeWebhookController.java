package com.makingcleancode.shop.controller.webhook;

import com.makingcleancode.shop.dto.response.BasicResponseModel;
import com.makingcleancode.shop.dto.response.BasicStatusModel;
import com.makingcleancode.shop.service.StripeWebhookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Implementa el endpoint de webhook de Stripe.
 *
 * NOTA: La firma del método acepta el payload como String raw (no como Map<String,Object>)
 * porque Stripe require verificar la firma HMAC sobre el cuerpo exacto en bytes.
 * La interfaz StripeWebhookControllerApi define Map<String,Object> (generado por OpenAPI),
 * pero aquí se trata como endpoint personalizado para mantener la integridad de la firma.
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "WebHooks", description = "Endpoints para recibir webhooks de terceros como Stripe")
@RequestMapping("/api/v1/payments/stripe")
public class StripeWebhookController {

    private final StripeWebhookService stripeWebhookService;

    @Operation(summary = "RECIBIR WEBHOOK DE STRIPE")
    @PostMapping("/webhook")
    public ResponseEntity<BasicResponseModel> handleStripeWebhook(
            @RequestHeader("Stripe-Signature") String stripeSignature,
            @RequestBody String payload) {

        stripeWebhookService.handleWebhook(payload, stripeSignature);

        return ResponseEntity.ok(
                BasicResponseModel.builder()
                        .status(BasicStatusModel.builder()
                                .message("Webhook processed")
                                .code(200)
                                .build())
                        .build());
    }
}
