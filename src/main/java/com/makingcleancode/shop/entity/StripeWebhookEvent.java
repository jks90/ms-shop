package com.makingcleancode.shop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "stripe_webhook_event",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_stripe_webhook_event_event_id", columnNames = "event_id")
        }
)
@Getter
@Setter
public class StripeWebhookEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, length = 255)
    private String eventId;

    @Column(name = "event_type", nullable = false, length = 120)
    private String eventType;

    @Column(name = "processed", nullable = false)
    private Boolean processed = false;

    @Lob
    @Column(name = "payload", nullable = false)
    private String payload;

    @Column(name = "received_at", nullable = false)
    private LocalDateTime receivedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Lob
    @Column(name = "error_message")
    private String errorMessage;

    @PrePersist
    void prePersist() {
        if (receivedAt == null) {
            receivedAt = LocalDateTime.now();
        }
    }
}