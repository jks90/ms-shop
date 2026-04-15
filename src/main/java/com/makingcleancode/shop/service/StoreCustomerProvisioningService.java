package com.makingcleancode.shop.service;

import com.makingcleancode.shop.entity.StoreCustomer;
import com.makingcleancode.shop.repository.StoreCustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StoreCustomerProvisioningService {

    private final StoreCustomerRepository storeCustomerRepository;

    @Transactional
    public void ensureCustomerForAuthUser(Long authUserId, String email, String name, String phone) {
        storeCustomerRepository.findByAuthUserId(authUserId)
                .orElseGet(() -> {
                    StoreCustomer customer = new StoreCustomer();
                    customer.setAuthUserId(authUserId);
                    customer.setEmailSnapshot(email != null ? email : ("user-" + authUserId + "@local"));
                    customer.setNameSnapshot(name);
                    customer.setPhoneSnapshot(phone);
                    return storeCustomerRepository.save(customer);
                });
    }
}
