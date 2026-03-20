package com.makingcleancode.shop.service.impl;

import com.makingcleancode.shop.entity.SalesOrder;
import com.makingcleancode.shop.entity.StoreCustomer;
import com.makingcleancode.shop.enums.PaymentMode;
import com.makingcleancode.shop.service.StripeGateway;
import com.makingcleancode.shop.service.dto.CheckoutLineItem;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class StripeCheckoutGateway implements StripeGateway {

    @Override
    public StripeCheckoutResult createCheckout(
            StoreCustomer customer,
            SalesOrder order,
            PaymentMode paymentMode,
            List<CheckoutLineItem> lineItems,
            String successUrl,
            String cancelUrl
    ) {
        if (paymentMode != PaymentMode.CHECKOUT_SESSION) {
            throw new IllegalArgumentException("This gateway implementation supports CHECKOUT_SESSION only");
        }

        try {
            List<SessionCreateParams.LineItem> stripeLineItems = new ArrayList<>();
            for (CheckoutLineItem item : lineItems) {
                SessionCreateParams.LineItem.PriceData.ProductData productData =
                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                .setName(item.name())
                                .setDescription(item.description())
                                .build();

                SessionCreateParams.LineItem.PriceData priceData =
                        SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency(item.currency().toLowerCase())
                                .setUnitAmount(item.unitAmount())
                                .setProductData(productData)
                                .build();

                stripeLineItems.add(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(item.quantity())
                                .setPriceData(priceData)
                                .build()
                );
            }

            SessionCreateParams.Builder builder = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl)
                    .setCancelUrl(cancelUrl)
                    .putMetadata("orderId", String.valueOf(order.getId()))
                    .putMetadata("orderNumber", order.getOrderNumber())
                    .putMetadata("authUserId", String.valueOf(order.getAuthUserId()))
                    .setPaymentIntentData(
                            SessionCreateParams.PaymentIntentData.builder()
                                    .putMetadata("orderId", String.valueOf(order.getId()))
                                    .putMetadata("orderNumber", order.getOrderNumber())
                                    .build());

            if (customer.getStripeCustomerId() != null && !customer.getStripeCustomerId().isBlank()) {
                builder.setCustomer(customer.getStripeCustomerId());
            } else {
                builder.setCustomerEmail(customer.getEmailSnapshot());
            }

            stripeLineItems.forEach(builder::addLineItem);

            Session session = Session.create(builder.build());

            return new StripeCheckoutResult(
                    session.getCustomer(),
                    session.getPaymentIntent(),
                    session.getId(),
                    null,
                    session.getUrl()
            );
        } catch (StripeException e) {
            throw new IllegalStateException("Stripe checkout session creation failed", e);
        }
    }
}