package com.makingcleancode.shop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "product_variant")
@Getter
@Setter
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "sku", nullable = false, length = 100, unique = true)
    private String sku;

    @Column(name = "name", length = 255)
    private String name;

    @Column(name = "price_amount", nullable = false)
    private Long priceAmount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "stock_available", nullable = false)
    private Integer stockAvailable;

    @Column(name = "stock_reserved", nullable = false)
    private Integer stockReserved;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}