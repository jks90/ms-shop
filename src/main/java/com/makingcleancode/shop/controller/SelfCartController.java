package com.makingcleancode.shop.controller;

import com.makingcleancode.shop.service.SelfCartService;
import com.makingcleancode.shop.v1.model.*;
import com.makingcleancode.repository.entities.User;
import com.makingcleancode.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Tag(name = "Carrito Self", description = "Endpoints relacionados con la gestión del carrito propio para clientes autenticados")
@RequestMapping("/api/v1/customers/self/cart")
public class SelfCartController {

    private final JwtService jwtService;
    private final SelfCartService selfCartService;

    @Operation(summary = "AGREGAR ITEM AL CARRITO")
    @PostMapping("/items")
    public ResponseEntity<CartResponseDto> addItemToCart(@RequestBody CartItemRequestDto cartItemRequestDto) {
        User user = getAuthenticatedUser();
        CartDto cart = selfCartService.addItemToCart(user.getId(), cartItemRequestDto);
        return ResponseEntity.ok(CartResponseDto.builder().status(ok()).data(cart).build());
    }

    @Operation(summary = "VACIAR CARRITO")
    @DeleteMapping
    public ResponseEntity<BasicResponseModel> clearCart() {
        User user = getAuthenticatedUser();
        selfCartService.clearCart(user.getId());
        return ResponseEntity.ok(BasicResponseModel.builder().status(ok()).build());
    }

    @Operation(summary = "OBTENER CARRITO PROPIO")
    @GetMapping
    public ResponseEntity<CartResponseDto> getSelfCart() {
        User user = getAuthenticatedUser();
        CartDto cart = selfCartService.getSelfCart(user.getId());
        return ResponseEntity.ok(CartResponseDto.builder().status(ok()).data(cart).build());
    }

    @Operation(summary = "ELIMINAR ITEM DEL CARRITO")
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartResponseDto> removeCartItem(@PathVariable Long itemId) {
        User user = getAuthenticatedUser();
        CartDto cart = selfCartService.removeCartItem(user.getId(), itemId);
        return ResponseEntity.ok(CartResponseDto.builder().status(ok()).data(cart).build());
    }

    @Operation(summary = "ACTUALIZAR ITEM DEL CARRITO")
    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartResponseDto> updateCartItem(
            @PathVariable Long itemId,
            @RequestBody UpdateCartItemRequestDto updateCartItemRequestDto) {
        User user = getAuthenticatedUser();
        CartDto cart = selfCartService.updateCartItem(user.getId(), itemId, updateCartItemRequestDto);
        return ResponseEntity.ok(CartResponseDto.builder().status(ok()).data(cart).build());
    }

    private User getAuthenticatedUser() {
        Optional<User> userIdOpt = jwtService.getAuthenticatedUser();
        if (userIdOpt.isEmpty()) {
            throw new RuntimeException("Unauthorized");
        }
        return userIdOpt.get();
    }

    private BasicStatusModel ok() {
        return BasicStatusModel.builder().code(200).message("OK").build();
    }
}
