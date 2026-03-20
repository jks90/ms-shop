package com.makingcleancode.shop.repository;

import com.makingcleancode.shop.entity.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {

    Optional<SalesOrder> findByOrderNumber(String orderNumber);

    Optional<SalesOrder> findByStripeCheckoutSessionId(String stripeCheckoutSessionId);

    Optional<SalesOrder> findByStripePaymentIntentId(String stripePaymentIntentId);

}