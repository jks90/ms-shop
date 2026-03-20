package com.makingcleancode.shop.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.makingcleancode.shop.entity.PaymentTransaction;
import com.makingcleancode.shop.entity.SalesOrder;
import com.makingcleancode.shop.enums.PaymentProvider;
import com.makingcleancode.shop.enums.PaymentTransactionType;
import com.makingcleancode.shop.repository.PaymentTransactionRepository;
import com.stripe.model.Charge;
import com.stripe.model.Event;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentTransactionService {

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void recordCheckoutSession(SalesOrder order, Session session, Event event) {
        PaymentTransaction tx = new PaymentTransaction();
        tx.setOrder(order);
        tx.setProvider(PaymentProvider.STRIPE);
        tx.setType(PaymentTransactionType.CHECKOUT_SESSION);
        tx.setStatus(session.getPaymentStatus() != null ? session.getPaymentStatus() : "unknown");
        tx.setAmount(order.getTotalAmount());
        tx.setCurrency(order.getCurrency());
        tx.setProviderCustomerId(session.getCustomer());
        tx.setProviderPaymentIntentId(session.getPaymentIntent());
        tx.setProviderSessionId(session.getId());
        tx.setProviderEventId(event.getId());
        tx.setResponsePayload(writeJson(session));

        paymentTransactionRepository.save(tx);
    }

    @Transactional
    public void recordPaymentIntent(
            SalesOrder order,
            String paymentIntentId,
            String customerId,
            Long amount,
            String currency,
            String status,
            Event event,
            Object payloadObject
    ) {
        PaymentTransaction tx = new PaymentTransaction();
        tx.setOrder(order);
        tx.setProvider(PaymentProvider.STRIPE);
        tx.setType(PaymentTransactionType.PAYMENT_INTENT);
        tx.setStatus(status);
        tx.setAmount(amount != null ? amount : 0L);
        tx.setCurrency(currency != null ? currency.toUpperCase() : order.getCurrency());
        tx.setProviderCustomerId(customerId);
        tx.setProviderPaymentIntentId(paymentIntentId);
        tx.setProviderSessionId(order.getStripeCheckoutSessionId());
        tx.setProviderEventId(event.getId());
        tx.setResponsePayload(writeJson(payloadObject));

        paymentTransactionRepository.save(tx);
    }

    @Transactional
    public void recordCharge(SalesOrder order, Charge charge, Event event) {
        PaymentTransaction tx = new PaymentTransaction();
        tx.setOrder(order);
        tx.setProvider(PaymentProvider.STRIPE);
        tx.setType(PaymentTransactionType.CHARGE);
        tx.setStatus(charge.getStatus() != null ? charge.getStatus() : "unknown");
        tx.setAmount(charge.getAmount() != null ? charge.getAmount() : 0L);
        tx.setCurrency(charge.getCurrency() != null ? charge.getCurrency().toUpperCase() : order.getCurrency());
        tx.setProviderCustomerId(charge.getCustomer());
        tx.setProviderPaymentIntentId(charge.getPaymentIntent());
        tx.setProviderChargeId(charge.getId());
        tx.setProviderSessionId(order.getStripeCheckoutSessionId());
        tx.setProviderEventId(event.getId());
        tx.setResponsePayload(writeJson(charge));

        paymentTransactionRepository.save(tx);
    }

    @Transactional
    public void recordRefund(SalesOrder order, Refund refund, Event event) {
        PaymentTransaction tx = new PaymentTransaction();
        tx.setOrder(order);
        tx.setProvider(PaymentProvider.STRIPE);
        tx.setType(PaymentTransactionType.REFUND);
        tx.setStatus(refund.getStatus() != null ? refund.getStatus() : "unknown");
        tx.setAmount(refund.getAmount() != null ? refund.getAmount() : 0L);
        tx.setCurrency(refund.getCurrency() != null ? refund.getCurrency().toUpperCase() : order.getCurrency());
        tx.setProviderPaymentIntentId(refund.getPaymentIntent());
        tx.setProviderChargeId(refund.getCharge());
        tx.setProviderSessionId(order.getStripeCheckoutSessionId());
        tx.setProviderEventId(event.getId());
        tx.setResponsePayload(writeJson(refund));

        paymentTransactionRepository.save(tx);
    }

    @Transactional
    public void recordGenericWebhookEvent(SalesOrder order, Event event, String status, long amount, String currency) {
        PaymentTransaction tx = new PaymentTransaction();
        tx.setOrder(order);
        tx.setProvider(PaymentProvider.STRIPE);
        tx.setType(PaymentTransactionType.WEBHOOK_EVENT);
        tx.setStatus(status);
        tx.setAmount(Math.max(amount, 0L));
        tx.setCurrency(currency != null ? currency.toUpperCase() : order.getCurrency());
        tx.setProviderCustomerId(order.getStripeCustomerId());
        tx.setProviderPaymentIntentId(order.getStripePaymentIntentId());
        tx.setProviderSessionId(order.getStripeCheckoutSessionId());
        tx.setProviderEventId(event.getId());
        tx.setResponsePayload(writeJson(event));

        paymentTransactionRepository.save(tx);
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize payment transaction payload", e);
        }
    }
}
