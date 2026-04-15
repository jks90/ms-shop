package com.makingcleancode.shop.controller;

import com.makingcleancode.repository.entities.User;
import com.makingcleancode.service.JwtService;
import com.makingcleancode.shop.entity.SalesOrder;
import com.makingcleancode.shop.service.SelfOrderService;
import com.shop.v1.model.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Order Self", description = "Endpoints relacionados con la gestión de órdenes propias para clientes autenticados")
@RequestMapping("/api/v1/customers/self/orders")
public class SelfOrderController {

    private final SelfOrderService selfOrderService;
    private final JwtService jwtService;

    @Operation(summary = "LISTAR ORDENES PROPIAS")
    @GetMapping
    public ResponseEntity<OrderPageResponseDto> getSelfOrders(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = getAuthUserId();
        List<SalesOrder> orders = selfOrderService.getSelfOrderList(userId, page, size);
        List<OrderDto> data = orders.stream().map(selfOrderService::toDto).toList();
        return ResponseEntity.ok(OrderPageResponseDto.builder()
                .status(ok())
                .data(data)
                .page(PageInfoDto.builder()
                        .page(page).size(size)
                        .totalElements((long) data.size()).totalPages(1)
                        .build())
                .build());
    }

    @Operation(summary = "OBTENER DETALLE DE ORDEN PROPIA")
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDto> getSelfOrderDetail(@PathVariable Long orderId) {
        Long userId = getAuthUserId();
        SalesOrder order = selfOrderService.getSelfOrderDetail(userId, orderId);
        return ResponseEntity.ok(OrderResponseDto.builder()
                .status(ok()).data(selfOrderService.toDto(order)).build());
    }

    @Operation(summary = "OBTENER ESTADO DE PAGO DE ORDEN PROPIA")
    @GetMapping("/{orderId}/payment-status")
    public ResponseEntity<OrderPaymentStatusResponseDto> getSelfOrderPaymentStatus(@PathVariable Long orderId) {
        Long userId = getAuthUserId();
        SalesOrder order = selfOrderService.getSelfOrderDetail(userId, orderId);
        PaymentStatus ps;
        try {
            ps = PaymentStatus.fromValue(order.getPaymentStatus().name());
        } catch (Exception e) {
            ps = PaymentStatus.PENDING;
        }
        return ResponseEntity.ok(OrderPaymentStatusResponseDto.builder()
                .status(ok())
                .data(OrderPaymentStatusDataDto.builder()
                        .orderId(order.getId())
                        .orderNumber(order.getOrderNumber())
                        .paymentStatus(ps)
                        .build())
                .build());
    }

    @Operation(summary = "CANCELAR ORDEN PROPIA")
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponseDto> cancelSelfOrder(@PathVariable Long orderId) {
        Long userId = getAuthUserId();
        SalesOrder cancelled = selfOrderService.cancelSelfOrder(userId, orderId);
        return ResponseEntity.ok(OrderResponseDto.builder()
                .status(ok()).data(selfOrderService.toDto(cancelled)).build());
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
