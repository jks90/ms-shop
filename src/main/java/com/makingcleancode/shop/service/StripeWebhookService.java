package com.makingcleancode.shop.service;

import com.makingcleancode.shop.config.StripeProperties;
import com.makingcleancode.shop.entity.ProductVariant;
import com.makingcleancode.shop.entity.SalesOrder;
import com.makingcleancode.shop.entity.SalesOrderItem;
import com.makingcleancode.shop.entity.StripeWebhookEvent;
import com.makingcleancode.shop.enums.OrderStatus;
import com.makingcleancode.shop.enums.PaymentStatus;
import com.makingcleancode.shop.exception.NotFoundException;
import com.makingcleancode.shop.repository.SalesOrderItemRepository;
import com.makingcleancode.shop.repository.SalesOrderRepository;
import com.makingcleancode.shop.repository.StripeWebhookEventRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StripeWebhookService {

    private final StripeProperties stripeProperties;
    private final StripeWebhookEventRepository stripeWebhookEventRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderItemRepository salesOrderItemRepository;
    private final InventoryService inventoryService;
    private final PaymentTransactionService paymentTransactionService;


    @Transactional
    public void handleWebhook(String payload, String signatureHeader) {
        Event event = constructVerifiedEvent(payload, signatureHeader);

        StripeWebhookEvent webhookRecord = getOrCreateWebhookRecord(event, payload);
        if (Boolean.TRUE.equals(webhookRecord.getProcessed())) {
            return;
        }

        try {
            switch (event.getType()) {
                case "checkout.session.completed" -> handleCheckoutSessionCompleted(event);
                case "checkout.session.expired" -> handleCheckoutSessionExpired(event);
                case "payment_intent.succeeded" -> handlePaymentIntentSucceeded(event);
                case "payment_intent.payment_failed" -> handlePaymentIntentFailed(event);
                case "charge.refunded" -> handleChargeRefunded(event);
                default -> {
                    // ignorado
                }
            }

            webhookRecord.setProcessed(true);
            webhookRecord.setProcessedAt(LocalDateTime.now());
            webhookRecord.setErrorMessage(null);
            stripeWebhookEventRepository.save(webhookRecord);

        } catch (RuntimeException ex) {
            webhookRecord.setErrorMessage(ex.getMessage());
            stripeWebhookEventRepository.save(webhookRecord);
            throw ex;
        }
    }

    private Event constructVerifiedEvent(String payload, String signatureHeader) {
        try {
            return Webhook.constructEvent(
                    payload,
                    signatureHeader,
                    stripeProperties.webhookSecret()
            );
        } catch (SignatureVerificationException e) {
            throw new IllegalArgumentException("Invalid Stripe signature", e);
        }
    }

    private StripeWebhookEvent getOrCreateWebhookRecord(Event event, String payload) {
        return stripeWebhookEventRepository.findByEventId(event.getId())
                .orElseGet(() -> {
                    try {
                        StripeWebhookEvent record = new StripeWebhookEvent();
                        record.setEventId(event.getId());
                        record.setEventType(event.getType());
                        record.setPayload(payload);
                        record.setProcessed(false);
                        return stripeWebhookEventRepository.save(record);
                    } catch (DataIntegrityViolationException ex) {
                        return stripeWebhookEventRepository.findByEventId(event.getId())
                                .orElseThrow(() -> ex);
                    }
                });
    }

    private void handleCheckoutSessionCompleted(Event event) {
        Session session = extractSession(event);

        SalesOrder order = findOrderFromSession(session);

        paymentTransactionService.recordCheckoutSession(order, session, event);

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            return;
        }

        order.setStripeCustomerId(session.getCustomer());
        order.setStripeCheckoutSessionId(session.getId());
        order.setStripePaymentIntentId(session.getPaymentIntent());

        String paymentStatus = session.getPaymentStatus();
        if ("paid".equalsIgnoreCase(paymentStatus)) {
            order.setPaymentStatus(PaymentStatus.PAID);
            order.setStatus(OrderStatus.CONFIRMED);
            order.setPaidAt(LocalDateTime.now());

            for (SalesOrderItem item : salesOrderItemRepository.findByOrderId(order.getId())) {
                ProductVariant variant = item.getVariant();
                if (variant != null) {
                    inventoryService.confirmReservedStock(
                            variant,
                            item.getQuantity(),
                            order.getOrderNumber()
                    );
                }
            }
        } else {
            order.setPaymentStatus(PaymentStatus.PENDING);
            order.setStatus(OrderStatus.PENDING_PAYMENT);
        }

        salesOrderRepository.save(order);
    }

    private void handleCheckoutSessionExpired(Event event) {
        Session session = extractSession(event);

        SalesOrder order = findOrderFromSession(session);

        paymentTransactionService.recordCheckoutSession(order, session, event);

        if (order.getPaymentStatus() == PaymentStatus.PAID ||
                order.getStatus() == OrderStatus.CANCELLED ||
                order.getStatus() == OrderStatus.REFUNDED) {
            return;
        }

        for (SalesOrderItem item : salesOrderItemRepository.findByOrderId(order.getId())) {
            ProductVariant variant = item.getVariant();
            if (variant != null) {
                inventoryService.releaseReservedStock(
                        variant,
                        item.getQuantity(),
                        order.getOrderNumber()
                );
            }
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setPaymentStatus(PaymentStatus.FAILED);
        order.setStripeCheckoutSessionId(session.getId());
        order.setStripePaymentIntentId(session.getPaymentIntent());

        salesOrderRepository.save(order);
    }

    private void handlePaymentIntentSucceeded(Event event) {
        StripeObject stripeObject = event.getDataObjectDeserializer()
                .getObject()
                .orElseThrow(() -> new IllegalStateException("Unable to deserialize payment_intent event"));

        if (!(stripeObject instanceof com.stripe.model.PaymentIntent pi)) {
            throw new IllegalStateException("Stripe event is not a payment_intent payload");
        }

        SalesOrder order = salesOrderRepository.findById(Long.valueOf(pi.getMetadata().get("orderId")))
                .orElseThrow(() -> new NotFoundException("Order not found for PaymentIntent metadata"));

        paymentTransactionService.recordPaymentIntent(
                order,
                pi.getId(),
                pi.getCustomer(),
                pi.getAmount(),
                pi.getCurrency(),
                pi.getStatus(),
                event,
                pi
        );
    }

    private void handlePaymentIntentFailed(Event event) {
        StripeObject stripeObject = event.getDataObjectDeserializer()
                .getObject()
                .orElseThrow(() -> new IllegalStateException("Unable to deserialize payment_intent event"));

        if (!(stripeObject instanceof com.stripe.model.PaymentIntent pi)) {
            throw new IllegalStateException("Stripe event is not a payment_intent payload");
        }

        SalesOrder order = salesOrderRepository.findById(Long.valueOf(pi.getMetadata().get("orderId")))
                .orElseThrow(() -> new NotFoundException("Order not found for PaymentIntent metadata"));

        paymentTransactionService.recordPaymentIntent(
                order,
                pi.getId(),
                pi.getCustomer(),
                pi.getAmount(),
                pi.getCurrency(),
                pi.getStatus(),
                event,
                pi
        );
    }

    private void handleChargeRefunded(Event event) {
        StripeObject stripeObject = event.getDataObjectDeserializer()
                .getObject()
                .orElseThrow(() -> new IllegalStateException("Unable to deserialize charge.refunded event"));

        if (!(stripeObject instanceof com.stripe.model.Charge charge)) {
            throw new IllegalStateException("Stripe event is not a charge payload");
        }

        SalesOrder order = salesOrderRepository.findByStripePaymentIntentId(charge.getPaymentIntent())
                .orElseThrow(() -> new NotFoundException("Order not found for refunded charge payment intent"));

        paymentTransactionService.recordCharge(order, charge, event);

        if (Boolean.TRUE.equals(charge.getRefunded())) {
            order.setPaymentStatus(PaymentStatus.REFUNDED);
            order.setStatus(OrderStatus.REFUNDED);
            salesOrderRepository.save(order);
        }
    }

    private Session extractSession(Event event) {
        StripeObject stripeObject = event.getDataObjectDeserializer()
                .getObject()
                .orElseThrow(() -> new IllegalStateException("Unable to deserialize Stripe event payload"));

        if (!(stripeObject instanceof Session session)) {
            throw new IllegalStateException("Stripe event is not a checkout.session payload");
        }

        return session;
    }

    private SalesOrder findOrderFromSession(Session session) {
        Map<String, String> metadata = session.getMetadata();

        if (metadata != null && metadata.get("orderId") != null) {
            Long orderId = Long.valueOf(metadata.get("orderId"));
            return salesOrderRepository.findById(orderId)
                    .orElseThrow(() -> new NotFoundException("Order not found for Stripe metadata.orderId=" + orderId));
        }

        if (metadata != null && metadata.get("orderNumber") != null) {
            return salesOrderRepository.findByOrderNumber(metadata.get("orderNumber"))
                    .orElseThrow(() -> new NotFoundException(
                            "Order not found for Stripe metadata.orderNumber=" + metadata.get("orderNumber")));
        }

        if (session.getId() != null) {
            return salesOrderRepository.findByStripeCheckoutSessionId(session.getId())
                    .orElseThrow(() -> new NotFoundException(
                            "Order not found for Stripe checkout session id=" + session.getId()));
        }

        throw new NotFoundException("Cannot resolve order from Stripe session");
    }
}
