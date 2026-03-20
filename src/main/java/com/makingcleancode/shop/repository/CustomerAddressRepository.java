package com.makingcleancode.shop.repository;


import com.makingcleancode.shop.entity.CustomerAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, Long> {

    Optional<CustomerAddress> findByIdAndCustomerId(Long id, Long customerId);
}