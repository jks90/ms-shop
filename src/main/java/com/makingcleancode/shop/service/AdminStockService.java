package com.makingcleancode.shop.service;

import com.makingcleancode.shop.entity.ProductVariant;
import com.makingcleancode.shop.exception.BusinessException;
import com.makingcleancode.shop.exception.NotFoundException;
import com.makingcleancode.shop.repository.ProductVariantRepository;
import com.shop.v1.model.ProductVariantDto;
import com.shop.v1.model.StockOperation;
import com.shop.v1.model.StockUpdateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminStockService {

    private final ProductVariantRepository productVariantRepository;
    private final InventoryService inventoryService;

    @Transactional
    public ProductVariantDto updateVariantStock(Long variantId, StockUpdateRequestDto request) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new NotFoundException("Variant not found: " + variantId));

        int quantity = request.getQuantity() != null ? request.getQuantity() : 0;
        StockOperation operation = request.getOperation();
        String notes = request.getNotes() != null ? request.getNotes() : "Admin stock update";

        if (operation == StockOperation.INCREMENT) {
            variant.setStockAvailable(variant.getStockAvailable() + quantity);
            inventoryService.saveStockIn(variant, quantity, "ADMIN", "admin-restock", notes);
        } else if (operation == StockOperation.DECREMENT) {
            if (variant.getStockAvailable() < quantity) {
                throw new BusinessException("Cannot reduce stock below 0 for sku " + variant.getSku());
            }
            variant.setStockAvailable(variant.getStockAvailable() - quantity);
            inventoryService.saveStockOut(variant, quantity, "ADMIN", "admin-adjustment", notes);
        } else if (operation == StockOperation.SET) {
            variant.setStockAvailable(quantity);
        }

        productVariantRepository.save(variant);
        return toDto(variant);
    }

    private ProductVariantDto toDto(ProductVariant v) {
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
