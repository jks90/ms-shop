package com.makingcleancode.shop.repository;


import com.makingcleancode.shop.entity.StoreCustomer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreCustomerRepository extends JpaRepository<StoreCustomer, Long> {
    Optional<StoreCustomer> findByAuthUserId(Long authUserId);
}
