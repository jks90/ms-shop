package com.makingcleancode.shop.repository;

import com.makingcleancode.shop.entity.RequestIdempotency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RequestIdempotencyRepository extends JpaRepository<RequestIdempotency, Long> {

    Optional<RequestIdempotency> findByAuthUserIdAndEndpointAndIdempotencyKey(
            Long authUserId,
            String endpoint,
            String idempotencyKey
    );
}