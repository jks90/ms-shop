package com.makingcleancode.shop.repository;

import com.makingcleancode.shop.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
}