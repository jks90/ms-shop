package com.makingcleancode.shop.repository;

import com.makingcleancode.shop.entity.OrderAddressSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderAddressSnapshotRepository extends JpaRepository<OrderAddressSnapshot, Long> {
    List<OrderAddressSnapshot> findByOrderId(Long orderId);
}