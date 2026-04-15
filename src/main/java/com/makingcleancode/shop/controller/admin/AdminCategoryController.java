package com.makingcleancode.shop.controller.admin;

import com.makingcleancode.shop.service.AdminCategoryService;
import com.shop.v1.model.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "[ADMIN] Categorias", description = "Endpoints para gestión de categorías por parte del administrador")
@RequestMapping("/api/v1/admin/categories")
public class AdminCategoryController {

    private final AdminCategoryService adminCategoryService;

    @Operation(summary = "LISTAR CATEGORIAS")
    @GetMapping
    public ResponseEntity<CategoryListResponseDto> getCategoriesAdmin() {
        return ResponseEntity.ok(CategoryListResponseDto.builder()
                .status(ok())
                .data(adminCategoryService.getAllCategories())
                .build());
    }

    @Operation(summary = "CREAR CATEGORIA")
    @PostMapping
    public ResponseEntity<CategoryResponseDto> createCategory(@RequestBody CategoryRequestDto categoryRequestDto) {
        CategoryDto category = adminCategoryService.createCategory(categoryRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CategoryResponseDto.builder()
                        .status(BasicStatusModel.builder().code(201).message("Created").build())
                        .data(category).build());
    }

    @Operation(summary = "ELIMINAR CATEGORIA")
    @DeleteMapping("/{id}")
    public ResponseEntity<BasicResponseModel> deleteCategory(@PathVariable Long id) {
        adminCategoryService.deleteCategory(id);
        return ResponseEntity.ok(BasicResponseModel.builder().status(ok()).build());
    }

    @Operation(summary = "ACTUALIZAR CATEGORIA")
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> updateCategory(
            @PathVariable Long id,
            @RequestBody CategoryRequestDto categoryRequestDto) {
        CategoryDto category = adminCategoryService.updateCategory(id, categoryRequestDto);
        return ResponseEntity.ok(CategoryResponseDto.builder()
                .status(ok()).data(category).build());
    }

    private BasicStatusModel ok() {
        return BasicStatusModel.builder().code(200).message("OK").build();
    }
}
