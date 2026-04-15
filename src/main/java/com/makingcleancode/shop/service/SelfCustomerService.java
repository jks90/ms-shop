package com.makingcleancode.shop.service;

import com.makingcleancode.shop.entity.StoreCustomer;
import com.makingcleancode.shop.exception.NotFoundException;
import com.makingcleancode.shop.repository.StoreCustomerRepository;
import com.shop.v1.model.StoreCustomerDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SelfCustomerService {

    private final StoreCustomerRepository storeCustomerRepository;

    @Transactional(readOnly = true)
    public StoreCustomerDto getSelfCustomer(Long authUserId) {
        StoreCustomer customer = storeCustomerRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new NotFoundException("Customer not found for user " + authUserId));

        return StoreCustomerDto.builder()
                .id(customer.getId())
                .authUserId(customer.getAuthUserId())
                .emailSnapshot(customer.getEmailSnapshot())
                .nameSnapshot(customer.getNameSnapshot())
                .phoneSnapshot(customer.getPhoneSnapshot())
                .stripeCustomerId(customer.getStripeCustomerId())
                .build();
    }
}
