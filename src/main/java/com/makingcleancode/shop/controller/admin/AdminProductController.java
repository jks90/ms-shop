package com.makingcleancode.shop.controller.admin;

import com.makingcleancode.shop.service.AdminProductService;
import com.shop.v1.model.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "[ADMIN] Productos", description = "Endpoints para gestión de productos por parte del administrador")
@RequestMapping("/api/v1/admin/products")
public class AdminProductController {

    private final AdminProductService adminProductService;

    @Operation(summary = "CREAR PRODUCTO")
    @PostMapping
    public ResponseEntity<ProductResponseDto> createProduct(@RequestBody ProductRequestDto productRequestDto) {
        ProductDto product = adminProductService.createProduct(productRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ProductResponseDto.builder()
                        .status(BasicStatusModel.builder().code(201).message("Created").build())
                        .data(product).build());
    }

    @Operation(summary = "CREAR VARIANTE DE PRODUCTO")
    @PostMapping("/{id}/variants")
    public ResponseEntity<ProductResponseDto> createProductVariant(
            @PathVariable Long id,
            @RequestBody ProductVariantRequestDto productVariantRequestDto) {
        ProductVariantDto variant = adminProductService.createVariant(id, productVariantRequestDto);
        ProductDto wrapper = ProductDto.builder()
                .id(id)
                .variants(java.util.List.of(variant))
                .build();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ProductResponseDto.builder()
                        .status(BasicStatusModel.builder().code(201).message("Created").build())
                        .data(wrapper).build());
    }

    @Operation(summary = "ELIMINAR PRODUCTO")
    @DeleteMapping("/{id}")
    public ResponseEntity<BasicResponseModel> deleteProduct(@PathVariable Long id) {
        adminProductService.deleteProduct(id);
        return ResponseEntity.ok(BasicResponseModel.builder()
                .status(BasicStatusModel.builder().code(200).message("Deleted").build())
                .build());
    }

    @Operation(summary = "ACTUALIZAR PRODUCTO")
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDto> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductRequestDto productRequestDto) {
        ProductDto product = adminProductService.updateProduct(id, productRequestDto);
        return ResponseEntity.ok(ProductResponseDto.builder()
                .status(BasicStatusModel.builder().code(200).message("OK").build())
                .data(product).build());
    }

    @Operation(summary = "LISTAR PRODUCTOS")
    @GetMapping
    public ResponseEntity<ProductPageResponseDto> getProductsAdmin(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) ProductStatus status) {
        Page<ProductDto> products = adminProductService.getProducts(page, size);
        return ResponseEntity.ok(ProductPageResponseDto.builder()
                .status(BasicStatusModel.builder().code(200).message("OK").build())
                .data(products.getContent())
                .page(PageInfoDto.builder()
                        .page(products.getNumber())
                        .size(products.getSize())
                        .totalElements(products.getTotalElements())
                        .totalPages(products.getTotalPages())
                        .build())
                .build());
    }
}
