package com.makingcleancode.shop.repository;

import com.makingcleancode.shop.entity.OrderAddressSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderAddressSnapshotRepository extends JpaRepository<OrderAddressSnapshot, Long> {
}