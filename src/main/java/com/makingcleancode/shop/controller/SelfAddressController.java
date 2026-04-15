package com.makingcleancode.shop.controller;

import com.makingcleancode.repository.entities.User;
import com.makingcleancode.service.JwtService;
import com.makingcleancode.shop.service.SelfAddressService;
import com.shop.v1.model.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Address Self", description = "Endpoints relacionados con la gestión de direcciones propias para clientes autenticados")
@RequestMapping("/api/v1/customers/self/addresses")
public class SelfAddressController {

    private final SelfAddressService selfAddressService;
    private final JwtService jwtService;

    @Operation(summary = "LISTAR DIRECCIONES PROPIAS")
    @GetMapping
    public ResponseEntity<AddressListResponseDto> getSelfAddresses() {
        Long userId = getAuthUserId();
        return ResponseEntity.ok(AddressListResponseDto.builder()
                .status(ok()).data(selfAddressService.getSelfAddresses(userId)).build());
    }

    @Operation(summary = "CREAR DIRECCION PROPIA")
    @PostMapping
    public ResponseEntity<AddressResponseDto> createSelfAddress(@RequestBody AddressRequestDto addressRequestDto) {
        Long userId = getAuthUserId();
        AddressDto address = selfAddressService.createSelfAddress(userId, addressRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AddressResponseDto.builder()
                        .status(BasicStatusModel.builder().code(201).message("Created").build())
                        .data(address).build());
    }

    @Operation(summary = "ACTUALIZAR DIRECCION PROPIA")
    @PutMapping("/{addressId}")
    public ResponseEntity<AddressResponseDto> updateSelfAddress(
            @PathVariable Long addressId,
            @RequestBody AddressRequestDto addressRequestDto) {
        Long userId = getAuthUserId();
        AddressDto address = selfAddressService.updateSelfAddress(userId, addressId, addressRequestDto);
        return ResponseEntity.ok(AddressResponseDto.builder().status(ok()).data(address).build());
    }

    @Operation(summary = "ELIMINAR DIRECCION PROPIA")
    @DeleteMapping("/{addressId}")
    public ResponseEntity<BasicResponseModel> deleteSelfAddress(@PathVariable Long addressId) {
        Long userId = getAuthUserId();
        selfAddressService.deleteSelfAddress(userId, addressId);
        return ResponseEntity.ok(BasicResponseModel.builder().status(ok()).build());
    }

    private Long getAuthUserId() {
        return jwtService.getAuthenticatedUser()
                .map(User::getId)
                .orElseThrow(() -> new IllegalStateException("Unauthorized"));
    }

    private BasicStatusModel ok() {
        return BasicStatusModel.builder().code(200).message("OK").build();
    }
}
