package com.makingcleancode.shop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "store_customer")
@Getter
@Setter
public class StoreCustomer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "auth_user_id", nullable = false, unique = true)
    private Long authUserId;

    @Column(name = "email_snapshot", nullable = false)
    private String emailSnapshot;

    @Column(name = "name_snapshot")
    private String nameSnapshot;

    @Column(name = "phone_snapshot")
    private String phoneSnapshot;

    @Column(name = "stripe_customer_id")
    private String stripeCustomerId;
}