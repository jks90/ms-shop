package com.makingcleancode.shop.controller.admin;

import com.makingcleancode.shop.service.AdminStockService;
import com.shop.v1.model.BasicResponseModel;
import com.shop.v1.model.BasicStatusModel;
import com.shop.v1.model.StockUpdateRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "[ADMIN] Stock", description = "Endpoints para gestión de stock por parte del administrador")
@RequestMapping("/api/v1/admin/stock")
public class AdminStockController {

    private final AdminStockService adminStockService;

    @Operation(summary = "ACTUALIZAR STOCK DE VARIANTE")
    @PatchMapping("/variants/{variantId}")
    public ResponseEntity<BasicResponseModel> updateVariantStock(
            @PathVariable Long variantId,
            @RequestBody StockUpdateRequestDto stockUpdateRequestDto) {
        adminStockService.updateVariantStock(variantId, stockUpdateRequestDto);
        return ResponseEntity.ok(BasicResponseModel.builder()
                .status(BasicStatusModel.builder().code(200).message("Stock updated").build())
                .build());
    }
}
