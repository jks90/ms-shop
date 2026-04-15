package com.makingcleancode.shop.repository;

import com.makingcleancode.shop.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySlug(String slug);

    @Query("SELECT p FROM Product p WHERE " +
           "(:slug IS NULL OR p.slug = :slug) AND " +
           "(:categoryId IS NULL OR p.categoryId = :categoryId) AND " +
           "(:status = 'ACTIVE' AND p.status = 'ACTIVE' OR :status IS NULL)")
    Page<Product> findByFilters(
            @Param("slug") String slug,
            @Param("categoryId") Long categoryId,
            @Param("status") String status,
            Pageable pageable);
}
