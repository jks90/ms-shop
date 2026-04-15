package com.makingcleancode.shop.service;

import com.makingcleancode.shop.dto.response.PaymentTransactionDto;
import com.makingcleancode.shop.entity.PaymentTransaction;
import com.makingcleancode.shop.exception.NotFoundException;
import com.makingcleancode.shop.repository.PaymentTransactionRepository;
import com.makingcleancode.shop.repository.SalesOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminPaymentQueryService {

    private final SalesOrderRepository salesOrderRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;

    @Transactional
    public List<PaymentTransactionDto> getAllPaymentTransactions() {
        return paymentTransactionRepository.findAllByOrderByCreatedAtAsc()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentTransactionDto> getOrderPaymentTransactions(Long orderId) {
        if (!salesOrderRepository.existsById(orderId)) {
            throw new NotFoundException("Order not found with id " + orderId);
        }

        return paymentTransactionRepository.findByOrderIdOrderByCreatedAtAsc(orderId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private PaymentTransactionDto toDto(PaymentTransaction entity) {
        return PaymentTransactionDto.builder()
                .id(entity.getId())
                .orderId(entity.getOrder().getId())
                .provider(entity.getProvider())
                .type(entity.getType())
                .status(entity.getStatus())
                .amount(entity.getAmount())
                .currency(entity.getCurrency())
                .providerCustomerId(entity.getProviderCustomerId())
                .providerPaymentIntentId(entity.getProviderPaymentIntentId())
                .providerChargeId(entity.getProviderChargeId())
                .providerSessionId(entity.getProviderSessionId())
                .providerEventId(entity.getProviderEventId())
                .responsePayload(entity.getResponsePayload())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
