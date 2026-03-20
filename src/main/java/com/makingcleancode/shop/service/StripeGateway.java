package com.makingcleancode.shop.service;


import com.makingcleancode.shop.entity.SalesOrder;
import com.makingcleancode.shop.entity.StoreCustomer;
import com.makingcleancode.shop.enums.PaymentMode;
import com.makingcleancode.shop.service.dto.CheckoutLineItem;

import java.util.List;

public interface StripeGateway {

    StripeCheckoutResult createCheckout(
            StoreCustomer customer,
            SalesOrder order,
            PaymentMode paymentMode,
            List<CheckoutLineItem> lineItems,
            String successUrl,
            String cancelUrl
    );

    record StripeCheckoutResult(
            String stripeCustomerId,
            String paymentIntentId,
            String checkoutSessionId,
            String clientSecret,
            String checkoutUrl
    ) {
    }
}