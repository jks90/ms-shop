package com.makingcleancode.shop.repository;

import com.makingcleancode.shop.entity.PaymentTransaction;
import com.makingcleancode.shop.enums.PaymentTransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    List<PaymentTransaction> findByOrderIdOrderByCreatedAtAsc(Long orderId);

    Optional<PaymentTransaction> findByProviderEventIdAndType(String providerEventId, PaymentTransactionType type);

}
