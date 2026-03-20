package com.makingcleancode.shop.dto.response;

import com.makingcleancode.shop.enums.PaymentMode;
import com.makingcleancode.shop.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CheckoutDataDto {
    private Long orderId;
    private String orderNumber;
    private PaymentMode paymentMode;
    private PaymentStatus paymentStatus;
    private String stripeCustomerId;
    private String stripePaymentIntentId;
    private String stripeCheckoutSessionId;
    private String clientSecret;
    private String checkoutUrl;
}