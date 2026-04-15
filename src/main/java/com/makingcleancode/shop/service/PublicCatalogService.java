package com.makingcleancode.shop.service;

import com.makingcleancode.shop.entity.Category;
import com.makingcleancode.shop.entity.Product;
import com.makingcleancode.shop.entity.ProductVariant;
import com.makingcleancode.shop.exception.NotFoundException;
import com.makingcleancode.shop.repository.CategoryRepository;
import com.makingcleancode.shop.repository.ProductRepository;
import com.makingcleancode.shop.repository.ProductVariantRepository;
import com.shop.v1.model.CategoryDto;
import com.shop.v1.model.ProductDto;
import com.shop.v1.model.ProductVariantDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PublicCatalogService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    @Transactional(readOnly = true)
    public List<CategoryDto> getCategories() {
        return categoryRepository.findByIsActiveTrue().stream()
                .map(this::toCategoryDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductDto getProductDetail(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found: " + productId));
        return toProductDto(product);
    }

    @Transactional(readOnly = true)
    public Page<Product> getProducts(String slug, String categorySlug, Integer page, Integer size, Boolean inStock) {
        Long categoryId = null;
        if (categorySlug != null && !categorySlug.isBlank()) {
            categoryId = categoryRepository.findAll().stream()
                    .filter(c -> categorySlug.equals(c.getSlug()))
                    .map(Category::getId)
                    .findFirst()
                    .orElse(-1L);
        }
        String status = Boolean.TRUE.equals(inStock) ? "ACTIVE" : null;
        return productRepository.findByFilters(slug, categoryId, status,
                PageRequest.of(page != null ? page : 0, size != null ? size : 20));
    }

    public ProductDto toProductDto(Product product) {
        List<ProductVariant> variants = productVariantRepository.findAll().stream()
                .filter(v -> v.getProduct().getId().equals(product.getId()))
                .toList();

        return ProductDto.builder()
                .id(product.getId())
                .categoryId(product.getCategoryId())
                .name(product.getName())
                .slug(product.getSlug())
                .description(product.getDescription())
                .brand(product.getBrand())
                .variants(variants.stream().map(this::toVariantDto).toList())
                .build();
    }

    private ProductVariantDto toVariantDto(ProductVariant v) {
        return ProductVariantDto.builder()
                .id(v.getId())
                .sku(v.getSku())
                .name(v.getName())
                .priceAmount(v.getPriceAmount())
                .currency(v.getCurrency())
                .stockAvailable(v.getStockAvailable())
                .isActive(v.getIsActive())
                .build();
    }

    private CategoryDto toCategoryDto(Category c) {
        return CategoryDto.builder()
                .id(c.getId())
                .name(c.getName())
                .slug(c.getSlug())
                .description(c.getDescription())
                .build();
    }
}
