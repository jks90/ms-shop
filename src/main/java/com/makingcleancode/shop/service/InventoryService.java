package com.makingcleancode.shop.service;

import com.makingcleancode.shop.entity.InventoryMovement;
import com.makingcleancode.shop.entity.ProductVariant;
import com.makingcleancode.shop.exception.BusinessException;
import com.makingcleancode.shop.repository.InventoryMovementRepository;
import com.makingcleancode.shop.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final ProductVariantRepository productVariantRepository;
    private final InventoryMovementRepository inventoryMovementRepository;

    @Transactional
    public void reserveStock(ProductVariant variant, int quantity, String referenceId) {
        int freeStock = variant.getStockAvailable() - variant.getStockReserved();
        if (freeStock < quantity) {
            throw new BusinessException("Insufficient stock for sku " + variant.getSku());
        }

        variant.setStockReserved(variant.getStockReserved() + quantity);
        productVariantRepository.save(variant);

        saveMovement(variant, "RESERVE", quantity, "ORDER", referenceId, "Stock reserved during checkout");
    }

    @Transactional
    public void confirmReservedStock(ProductVariant variant, int quantity, String referenceId) {
        if (variant.getStockReserved() < quantity) {
            throw new BusinessException("Reserved stock inconsistency for sku " + variant.getSku());
        }
        if (variant.getStockAvailable() < quantity) {
            throw new BusinessException("Available stock inconsistency for sku " + variant.getSku());
        }

        variant.setStockReserved(variant.getStockReserved() - quantity);
        variant.setStockAvailable(variant.getStockAvailable() - quantity);
        productVariantRepository.save(variant);

        saveMovement(variant, "OUT", quantity, "ORDER", referenceId, "Stock confirmed after successful payment");
    }

    @Transactional
    public void releaseReservedStock(ProductVariant variant, int quantity, String referenceId) {
        if (variant.getStockReserved() < quantity) {
            throw new BusinessException("Reserved stock inconsistency for sku " + variant.getSku());
        }

        variant.setStockReserved(variant.getStockReserved() - quantity);
        productVariantRepository.save(variant);

        saveMovement(variant, "RELEASE", quantity, "ORDER", referenceId, "Stock released after checkout expiration/failure");
    }

    private void saveMovement(
            ProductVariant variant,
            String type,
            int quantity,
            String referenceType,
            String referenceId,
            String notes
    ) {
        InventoryMovement movement = new InventoryMovement();
        movement.setVariant(variant);
        movement.setType(type);
        movement.setQuantity(quantity);
        movement.setReferenceType(referenceType);
        movement.setReferenceId(referenceId);
        movement.setNotes(notes);
        inventoryMovementRepository.save(movement);
    }
}