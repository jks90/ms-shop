package com.makingcleancode.shop.service;

import com.makingcleancode.shop.v1.model.*;
import com.makingcleancode.shop.entity.ProductVariant;
import com.makingcleancode.shop.entity.ShoppingCart;
import com.makingcleancode.shop.entity.ShoppingCartItem;
import com.makingcleancode.shop.entity.StoreCustomer;
import com.makingcleancode.shop.exception.BusinessException;
import com.makingcleancode.shop.exception.NotFoundException;
import com.makingcleancode.shop.repository.ProductVariantRepository;
import com.makingcleancode.shop.repository.ShoppingCartRepository;
import com.makingcleancode.shop.repository.StoreCustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SelfCartService {

    private final StoreCustomerRepository storeCustomerRepository;
    private final ShoppingCartRepository shoppingCartRepository;
    private final ProductVariantRepository productVariantRepository;

    @Transactional(readOnly = true)
    public CartDto getSelfCart(Long authUserId) {
        StoreCustomer customer = findCustomer(authUserId);
        ShoppingCart cart = getOrCreateCart(customer);
        return toDto(cart);
    }

    @Transactional
    public CartDto addItemToCart(Long authUserId, CartItemRequestDto request) {
        StoreCustomer customer = findCustomer(authUserId);
        ProductVariant variant = productVariantRepository.findById(request.getVariantId())
                .orElseThrow(() -> new NotFoundException("Variant not found: " + request.getVariantId()));

        if (!Boolean.TRUE.equals(variant.getIsActive())) {
            throw new BusinessException("Variant is not active: " + variant.getSku());
        }

        ShoppingCart cart = getOrCreateCart(customer);

        ShoppingCartItem existing = cart.getItems().stream()
                .filter(i -> i.getVariant().getId().equals(variant.getId()))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + request.getQuantity());
        } else {
            ShoppingCartItem item = new ShoppingCartItem();
            item.setCart(cart);
            item.setVariant(variant);
            item.setQuantity(request.getQuantity());
            item.setUnitPriceAmount(variant.getPriceAmount());
            item.setCurrency(variant.getCurrency());
            cart.getItems().add(item);
        }

        return toDto(shoppingCartRepository.save(cart));
    }

    @Transactional
    public CartDto updateCartItem(Long authUserId, Long itemId, UpdateCartItemRequestDto request) {
        StoreCustomer customer = findCustomer(authUserId);
        ShoppingCart cart = getOrCreateCart(customer);

        ShoppingCartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Cart item not found: " + itemId));

        if (request.getQuantity() <= 0) {
            cart.getItems().remove(item);
        } else {
            item.setQuantity(request.getQuantity());
        }
        return toDto(shoppingCartRepository.save(cart));
    }

    @Transactional
    public CartDto removeCartItem(Long authUserId, Long itemId) {
        StoreCustomer customer = findCustomer(authUserId);
        ShoppingCart cart = getOrCreateCart(customer);

        cart.getItems().removeIf(i -> i.getId().equals(itemId));
        return toDto(shoppingCartRepository.save(cart));
    }

    @Transactional
    public void clearCart(Long authUserId) {
        StoreCustomer customer = findCustomer(authUserId);
        ShoppingCart cart = getOrCreateCart(customer);
        cart.getItems().clear();
        shoppingCartRepository.save(cart);
    }

    private StoreCustomer findCustomer(Long authUserId) {
        return storeCustomerRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new NotFoundException("Customer not found for user " + authUserId));
    }

    private ShoppingCart getOrCreateCart(StoreCustomer customer) {
        return shoppingCartRepository.findByCustomerId(customer.getId())
                .orElseGet(() -> {
                    ShoppingCart cart = new ShoppingCart();
                    cart.setCustomer(customer);
                    cart.setStatus("ACTIVE");
                    return shoppingCartRepository.save(cart);
                });
    }

    public CartDto toDto(ShoppingCart cart) {
        List<CartItemDto> items = cart.getItems().stream().map(i ->
                CartItemDto.builder()
                        .id(i.getId())
                        .variantId(i.getVariant().getId())
                        .sku(i.getVariant().getSku())
                        .productName(i.getVariant().getProduct().getName())
                        .variantName(i.getVariant().getName())
                        .quantity(i.getQuantity())
                        .unitPriceAmount(i.getUnitPriceAmount())
                        .currency(i.getCurrency())
                        .build()
        ).toList();

        CartStatus cartStatus;
        try {
            cartStatus = CartStatus.fromValue(cart.getStatus());
        } catch (Exception e) {
            cartStatus = CartStatus.ACTIVE;
        }

        return CartDto.builder()
                .id(cart.getId())
                .status(cartStatus)
                .items(items)
                .build();
    }
}
