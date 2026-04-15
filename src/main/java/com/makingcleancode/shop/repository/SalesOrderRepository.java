package com.makingcleancode.shop.repository;

import com.makingcleancode.shop.entity.SalesOrder;
import com.makingcleancode.shop.enums.OrderStatus;
import com.makingcleancode.shop.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {

    Optional<SalesOrder> findByOrderNumber(String orderNumber);

    Optional<SalesOrder> findByStripeCheckoutSessionId(String stripeCheckoutSessionId);

    Optional<SalesOrder> findByStripePaymentIntentId(String stripePaymentIntentId);

    @Query("SELECT o FROM SalesOrder o WHERE " +
           "(:status IS NULL OR o.status = :status) AND " +
           "(:paymentStatus IS NULL OR o.paymentStatus = :paymentStatus) AND " +
           "(:authUserId IS NULL OR o.authUserId = :authUserId)")
    Page<SalesOrder> findAllByFilters(
            @Param("status") OrderStatus status,
            @Param("paymentStatus") PaymentStatus paymentStatus,
            @Param("authUserId") Long authUserId,
            Pageable pageable);

}