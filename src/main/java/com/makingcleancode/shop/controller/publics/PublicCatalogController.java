package com.makingcleancode.shop.controller.publics;

import com.makingcleancode.shop.entity.Product;
import com.makingcleancode.shop.service.PublicCatalogService;
import com.shop.v1.model.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "[PUBLIC] Catalogo", description = "Endpoints públicos para consulta de categorías y productos")
@RequestMapping("/api/v1/public")
public class PublicCatalogController {

    private final PublicCatalogService publicCatalogService;

    @Operation(summary = "LISTAR CATEGORIAS PUBLICAS")
    @GetMapping("/categories")
    public ResponseEntity<CategoryListResponseDto> getCategories() {
        List<CategoryDto> categories = publicCatalogService.getCategories();
        return ResponseEntity.ok(CategoryListResponseDto.builder()
                .status(ok())
                .data(categories)
                .build());
    }

    @Operation(summary = "OBTENER DETALLE DE PRODUCTO PUBLICO")
    @GetMapping("/products/{productId}")
    public ResponseEntity<ProductResponseDto> getProductDetail(@PathVariable Long productId) {
        ProductDto product = publicCatalogService.getProductDetail(productId);
        return ResponseEntity.ok(ProductResponseDto.builder()
                .status(ok())
                .data(product)
                .build());
    }

    @Operation(summary = "LISTAR PRODUCTOS PUBLICOS")
    @GetMapping("/products")
    public ResponseEntity<ProductPageResponseDto> getProducts(
            @RequestParam(required = false) String categorySlug,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Boolean activeOnly) {
        Page<Product> productPage = publicCatalogService.getProducts(categorySlug, null, page, size, activeOnly);
        List<ProductDto> data = productPage.getContent().stream()
                .map(publicCatalogService::toProductDto)
                .toList();
        return ResponseEntity.ok(ProductPageResponseDto.builder()
                .status(ok())
                .data(data)
                .page(PageInfoDto.builder()
                        .page(productPage.getNumber())
                        .size(productPage.getSize())
                        .totalElements(productPage.getTotalElements())
                        .totalPages(productPage.getTotalPages())
                        .build())
                .build());
    }

    private BasicStatusModel ok() {
        return BasicStatusModel.builder().code(200).message("OK").build();
    }
}
