package com.makingcleancode.shop.entity;

import com.makingcleancode.shop.enums.OrderStatus;
import com.makingcleancode.shop.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "sales_order")
@Getter
@Setter
public class SalesOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private StoreCustomer customer;

    @Column(name = "auth_user_id", nullable = false)
    private Long authUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 30)
    private PaymentStatus paymentStatus;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "subtotal_amount", nullable = false)
    private Long subtotalAmount;

    @Column(name = "discount_amount", nullable = false)
    private Long discountAmount;

    @Column(name = "shipping_amount", nullable = false)
    private Long shippingAmount;

    @Column(name = "tax_amount", nullable = false)
    private Long taxAmount;

    @Column(name = "total_amount", nullable = false)
    private Long totalAmount;

    @Column(name = "stripe_customer_id")
    private String stripeCustomerId;

    @Column(name = "stripe_payment_intent_id")
    private String stripePaymentIntentId;

    @Column(name = "stripe_checkout_session_id")
    private String stripeCheckoutSessionId;

    @Lob
    @Column(name = "notes")
    private String notes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "placed_at")
    private LocalDateTime placedAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
