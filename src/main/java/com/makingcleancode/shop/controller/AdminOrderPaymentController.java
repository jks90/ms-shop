package com.makingcleancode.shop.controller;

import com.makingcleancode.shop.dto.response.BasicStatusModel;
import com.makingcleancode.shop.dto.response.PaymentTransactionListResponseDto;
import com.makingcleancode.shop.service.AdminPaymentQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ms-store/api/v1/admin/orders")
public class AdminOrderPaymentController {

    private final AdminPaymentQueryService adminPaymentQueryService;

    @GetMapping("/{orderId}/payments")
    public ResponseEntity<PaymentTransactionListResponseDto> getOrderPaymentTransactions(
            @PathVariable Long orderId
    ) {
        return ResponseEntity.ok(
                PaymentTransactionListResponseDto.builder()
                        .status(BasicStatusModel.builder()
                                .message("OK")
                                .code(200)
                                .build())
                        .data(adminPaymentQueryService.getOrderPaymentTransactions(orderId))
                        .build()
        );
    }
}