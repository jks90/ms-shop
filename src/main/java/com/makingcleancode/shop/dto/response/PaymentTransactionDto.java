package com.makingcleancode.shop.dto.response;

import com.makingcleancode.shop.enums.PaymentProvider;
import com.makingcleancode.shop.enums.PaymentTransactionType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PaymentTransactionDto {
    private Long id;
    private Long orderId;
    private PaymentProvider provider;
    private PaymentTransactionType type;
    private String status;
    private Long amount;
    private String currency;
    private String providerCustomerId;
    private String providerPaymentIntentId;
    private String providerChargeId;
    private String providerSessionId;
    private String providerEventId;
    private String responsePayload;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
