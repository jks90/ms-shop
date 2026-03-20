package com.makingcleancode.shop.repository;

import com.makingcleancode.shop.entity.ShoppingCart;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {

    @EntityGraph(attributePaths = {
            "customer",
            "items",
            "items.variant",
            "items.variant.product"
    })
    Optional<ShoppingCart> findByCustomerId(Long customerId);
}