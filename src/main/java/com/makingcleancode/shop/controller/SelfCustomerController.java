package com.makingcleancode.shop.controller;

import com.makingcleancode.shop.service.SelfCustomerService;
import com.shop.v1.model.BasicStatusModel;
import com.shop.v1.model.StoreCustomerResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "User Self", description = "Endpoints relacionados con la gestión del perfil propio para clientes autenticados")
@RequestMapping("/api/v1/customers/self")
public class SelfCustomerController {

    private final SelfCustomerService selfCustomerService;

    @Operation(summary = "OBTENER PERFIL PROPIO")
    @GetMapping
    public ResponseEntity<StoreCustomerResponseDto> getSelfCustomer() {
        return ResponseEntity.ok(StoreCustomerResponseDto.builder()
                .status(BasicStatusModel.builder().code(200).message("OK").build())
                .data(null)
                .build());
    }
}
