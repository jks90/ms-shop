package com.makingcleancode.shop.service;

import com.makingcleancode.shop.entity.Product;
import com.makingcleancode.shop.entity.ProductVariant;
import com.makingcleancode.shop.exception.NotFoundException;
import com.makingcleancode.shop.repository.ProductRepository;
import com.makingcleancode.shop.repository.ProductVariantRepository;
import com.shop.v1.model.ProductDto;
import com.shop.v1.model.ProductRequestDto;
import com.shop.v1.model.ProductVariantDto;
import com.shop.v1.model.ProductVariantRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    @Transactional
    public ProductDto createProduct(ProductRequestDto request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setSlug(request.getSlug());
        product.setStatus("ACTIVE");
        product.setBrand(request.getBrand());
        product.setDescription(request.getDescription());
        product.setCategoryId(request.getCategoryId());
        return toDto(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public Page<ProductDto> getProducts(Integer page, Integer size) {
        return productRepository.findAll(PageRequest.of(page != null ? page : 0, size != null ? size : 20))
                .map(this::toDto);
    }

    @Transactional(readOnly = true)
    public ProductDto getProductDetail(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found: " + id));
        return toDto(product);
    }

    @Transactional
    public ProductDto updateProduct(Long id, ProductRequestDto request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found: " + id));
        product.setName(request.getName());
        if (request.getSlug() != null) product.setSlug(request.getSlug());
        if (request.getBrand() != null) product.setBrand(request.getBrand());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getCategoryId() != null) product.setCategoryId(request.getCategoryId());
        if (request.getStatus() != null) product.setStatus(request.getStatus().getValue());
        return toDto(productRepository.save(product));
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found: " + id));
        product.setStatus("INACTIVE");
        productRepository.save(product);
    }

    @Transactional
    public ProductVariantDto createVariant(Long productId, ProductVariantRequestDto request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found: " + productId));

        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setSku(request.getSku());
        variant.setName(request.getName());
        variant.setPriceAmount(request.getPriceAmount());
        variant.setCurrency(request.getCurrency());
        variant.setStockAvailable(request.getStockAvailable() != null ? request.getStockAvailable() : 0);
        variant.setStockReserved(0);
        variant.setIsActive(true);

        return toVariantDto(productVariantRepository.save(variant));
    }

    private ProductDto toDto(Product p) {
        return ProductDto.builder()
                .id(p.getId())
                .categoryId(p.getCategoryId())
                .name(p.getName())
                .slug(p.getSlug())
                .description(p.getDescription())
                .brand(p.getBrand())
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
}
