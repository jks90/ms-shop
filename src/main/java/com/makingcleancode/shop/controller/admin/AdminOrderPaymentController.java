package com.makingcleancode.shop.controller.admin;

import com.makingcleancode.shop.dto.response.PaymentTransactionDto;
import com.makingcleancode.shop.service.AdminOrderService;
import com.makingcleancode.shop.service.AdminPaymentQueryService;
import com.shop.v1.model.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.makingcleancode.shop.entity.SalesOrder;
import com.makingcleancode.shop.enums.OrderStatus;
import com.makingcleancode.shop.enums.PaymentStatus;

import java.time.ZoneOffset;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "[ADMIN] Ordenes y Pagos", description = "Endpoints para gestión de órdenes y pagos por parte del administrador")
@RequestMapping("/api/v1/admin/orders")
public class AdminOrderPaymentController {

    private final AdminOrderService adminOrderService;
    private final AdminPaymentQueryService adminPaymentQueryService;

    @Operation(summary = "OBTENER DETALLE DE ORDEN")
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDto> getOrderDetail(@PathVariable Long orderId) {
        SalesOrder order = adminOrderService.getOrderDetail(orderId);
        OrderDto dto = adminOrderService.toOrderDto(order);
        return ResponseEntity.ok(
                OrderResponseDto.builder()
                        .status(BasicStatusModel.builder().code(200).message("OK").build())
                        .data(dto)
                        .build());
    }

    @Operation(summary = "LISTAR TRANSACCIONES DE ORDEN")
    @GetMapping("/{orderId}/transactions")
    public ResponseEntity<PaymentTransactionListResponseDto> getOrderPaymentTransactions(@PathVariable Long orderId) {
        List<PaymentTransactionDto> internal = adminPaymentQueryService.getOrderPaymentTransactions(orderId);
        List<com.shop.v1.model.PaymentTransactionDto> apiList = internal.stream()
                .map(this::toApiPaymentTransactionDto)
                .toList();
        return ResponseEntity.ok(
                PaymentTransactionListResponseDto.builder()
                        .status(BasicStatusModel.builder().code(200).message("OK").build())
                        .data(apiList)
                        .build());
    }

    @Operation(summary = "LISTAR ORDENES")
    @GetMapping
    public ResponseEntity<OrderPageResponseDto> getOrders(
            @RequestParam(required = false) com.shop.v1.model.OrderStatus status,
            @RequestParam(required = false) com.shop.v1.model.PaymentStatus paymentStatus,
            @RequestParam(required = false) Long authUserId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        OrderStatus domainStatus = toInternalOrderStatus(status);
        PaymentStatus domainPaymentStatus = toInternalPaymentStatus(paymentStatus);

        Page<SalesOrder> orderPage = adminOrderService.getOrders(domainStatus, domainPaymentStatus, authUserId, page, size);

        List<OrderDto> data = orderPage.getContent().stream()
                .map(adminOrderService::toOrderDto)
                .toList();

        PageInfoDto pageInfo = PageInfoDto.builder()
                .page(orderPage.getNumber())
                .size(orderPage.getSize())
                .totalPages(orderPage.getTotalPages())
                .totalElements(orderPage.getTotalElements())
                .build();

        return ResponseEntity.ok(
                OrderPageResponseDto.builder()
                        .status(BasicStatusModel.builder().code(200).message("OK").build())
                        .data(data)
                        .page(pageInfo)
                        .build());
    }

    @Operation(summary = "ACTUALIZAR ESTADO DE ORDEN")
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderResponseDto> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody UpdateOrderStatusRequestDto updateOrderStatusRequestDto) {
        SalesOrder updated = adminOrderService.updateOrderStatus(orderId, updateOrderStatusRequestDto);
        OrderDto dto = adminOrderService.toOrderDto(updated);
        return ResponseEntity.ok(
                OrderResponseDto.builder()
                        .status(BasicStatusModel.builder().code(200).message("OK").build())
                        .data(dto)
                        .build());
    }

    // ---- Helpers ----

    private com.shop.v1.model.PaymentTransactionDto toApiPaymentTransactionDto(PaymentTransactionDto internal) {
        com.shop.v1.model.PaymentProvider provider = null;
        com.shop.v1.model.PaymentTransactionType type = null;
        try {
            if (internal.getProvider() != null) {
                provider = com.shop.v1.model.PaymentProvider.fromValue(internal.getProvider().name());
            }
        } catch (Exception ignored) {}
        try {
            if (internal.getType() != null) {
                type = com.shop.v1.model.PaymentTransactionType.fromValue(internal.getType().name());
            }
        } catch (Exception ignored) {}

        return com.shop.v1.model.PaymentTransactionDto.builder()
                .id(internal.getId())
                .orderId(internal.getOrderId())
                .provider(provider)
                .type(type)
                .status(internal.getStatus())
                .amount(internal.getAmount())
                .currency(internal.getCurrency())
                .providerCustomerId(internal.getProviderCustomerId())
                .providerPaymentIntentId(internal.getProviderPaymentIntentId())
                .providerChargeId(internal.getProviderChargeId())
                .providerSessionId(internal.getProviderSessionId())
                .providerEventId(internal.getProviderEventId())
                .responsePayload(internal.getResponsePayload())
                .createdAt(internal.getCreatedAt() != null ? internal.getCreatedAt().atOffset(ZoneOffset.UTC) : null)
                .updatedAt(internal.getUpdatedAt() != null ? internal.getUpdatedAt().atOffset(ZoneOffset.UTC) : null)
                .build();
    }

    private OrderStatus toInternalOrderStatus(com.shop.v1.model.OrderStatus apiStatus) {
        if (apiStatus == null) return null;
        try {
            return OrderStatus.valueOf(apiStatus.getValue());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private PaymentStatus toInternalPaymentStatus(com.shop.v1.model.PaymentStatus apiStatus) {
        if (apiStatus == null) return null;
        try {
            return PaymentStatus.valueOf(apiStatus.getValue());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
