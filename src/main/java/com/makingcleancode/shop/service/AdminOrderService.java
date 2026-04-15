package com.makingcleancode.shop.service;

import com.makingcleancode.shop.entity.OrderAddressSnapshot;
import com.makingcleancode.shop.entity.SalesOrder;
import com.makingcleancode.shop.entity.SalesOrderItem;
import com.makingcleancode.shop.enums.OrderStatus;
import com.makingcleancode.shop.enums.PaymentStatus;
import com.makingcleancode.shop.exception.NotFoundException;
import com.makingcleancode.shop.repository.OrderAddressSnapshotRepository;
import com.makingcleancode.shop.repository.SalesOrderItemRepository;
import com.makingcleancode.shop.repository.SalesOrderRepository;
import com.shop.v1.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminOrderService {

    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderItemRepository salesOrderItemRepository;
    private final OrderAddressSnapshotRepository orderAddressSnapshotRepository;

    @Transactional(readOnly = true)
    public SalesOrder getOrderDetail(Long orderId) {
        return salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
    }

    @Transactional(readOnly = true)
    public Page<SalesOrder> getOrders(OrderStatus status, PaymentStatus paymentStatus,
                                      Long authUserId, Integer page, Integer size) {
        return salesOrderRepository.findAllByFilters(
                status, paymentStatus, authUserId,
                PageRequest.of(page != null ? page : 0, size != null ? size : 20));
    }

    @Transactional
    public SalesOrder updateOrderStatus(Long orderId, UpdateOrderStatusRequestDto requestDto) {
        SalesOrder order = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));

        if (requestDto.getStatus() != null) {
            try {
                order.setStatus(OrderStatus.valueOf(requestDto.getStatus().getValue()));
            } catch (IllegalArgumentException ignored) {
                // mantener el status actual si el valor no mapea
            }
        }
        if (requestDto.getPaymentStatus() != null) {
            try {
                order.setPaymentStatus(PaymentStatus.valueOf(requestDto.getPaymentStatus().getValue()));
            } catch (IllegalArgumentException ignored) {
                // mantener el paymentStatus actual si el valor no mapea
            }
        }

        return salesOrderRepository.save(order);
    }

    // ---- Mappers ----

    public OrderDto toOrderDto(SalesOrder o) {
        com.shop.v1.model.OrderStatus orderStatus;
        com.shop.v1.model.PaymentStatus paymentStatus;
        try {
            orderStatus = com.shop.v1.model.OrderStatus.fromValue(o.getStatus().name());
        } catch (Exception e) {
            orderStatus = com.shop.v1.model.OrderStatus.PENDING_PAYMENT;
        }
        try {
            paymentStatus = com.shop.v1.model.PaymentStatus.fromValue(o.getPaymentStatus().name());
        } catch (Exception e) {
            paymentStatus = com.shop.v1.model.PaymentStatus.PENDING;
        }

        List<OrderItemDto> items = salesOrderItemRepository.findByOrderId(o.getId())
                .stream()
                .map(this::toItemDto)
                .toList();

        List<OrderAddressDto> addresses = orderAddressSnapshotRepository.findByOrderId(o.getId())
                .stream()
                .map(this::toAddressDto)
                .toList();

        return OrderDto.builder()
                .id(o.getId())
                .orderNumber(o.getOrderNumber())
                .customerId(o.getCustomer().getId())
                .authUserId(o.getAuthUserId())
                .status(orderStatus)
                .paymentStatus(paymentStatus)
                .currency(o.getCurrency())
                .subtotalAmount(o.getSubtotalAmount())
                .discountAmount(o.getDiscountAmount())
                .shippingAmount(o.getShippingAmount())
                .taxAmount(o.getTaxAmount())
                .totalAmount(o.getTotalAmount())
                .stripeCustomerId(o.getStripeCustomerId())
                .stripePaymentIntentId(o.getStripePaymentIntentId())
                .stripeCheckoutSessionId(o.getStripeCheckoutSessionId())
                .notes(o.getNotes())
                .createdAt(o.getCreatedAt() != null ? o.getCreatedAt().atOffset(ZoneOffset.UTC) : null)
                .placedAt(o.getPlacedAt() != null ? o.getPlacedAt().atOffset(ZoneOffset.UTC) : null)
                .paidAt(o.getPaidAt() != null ? o.getPaidAt().atOffset(ZoneOffset.UTC) : null)
                .items(items)
                .addresses(addresses)
                .build();
    }

    private OrderItemDto toItemDto(SalesOrderItem item) {
        return OrderItemDto.builder()
                .id(item.getId())
                .variantId(item.getVariant() != null ? item.getVariant().getId() : null)
                .sku(item.getSku())
                .productName(item.getProductName())
                .variantName(item.getVariantName())
                .quantity(item.getQuantity())
                .unitPriceAmount(item.getUnitPriceAmount())
                .subtotalAmount(item.getSubtotalAmount())
                .currency(item.getCurrency())
                .build();
    }

    private OrderAddressDto toAddressDto(OrderAddressSnapshot a) {
        AddressType type;
        try {
            type = AddressType.fromValue(a.getType());
        } catch (Exception e) {
            type = AddressType.SHIPPING;
        }
        return OrderAddressDto.builder()
                .type(type)
                .recipientName(a.getRecipientName())
                .line1(a.getLine1())
                .line2(a.getLine2())
                .postalCode(a.getPostalCode())
                .city(a.getCity())
                .stateRegion(a.getStateRegion())
                .countryCode(a.getCountryCode())
                .phone(a.getPhone())
                .build();
    }
}
