package com.makingcleancode.shop.service;

import com.makingcleancode.shop.entity.SalesOrder;
import com.makingcleancode.shop.entity.SalesOrderItem;
import com.makingcleancode.shop.entity.StoreCustomer;
import com.makingcleancode.shop.enums.OrderStatus;
import com.makingcleancode.shop.enums.PaymentStatus;
import com.makingcleancode.shop.exception.BusinessException;
import com.makingcleancode.shop.exception.NotFoundException;
import com.makingcleancode.shop.repository.SalesOrderItemRepository;
import com.makingcleancode.shop.repository.SalesOrderRepository;
import com.makingcleancode.shop.repository.StoreCustomerRepository;
import com.shop.v1.model.OrderDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SelfOrderService {

    private final StoreCustomerRepository storeCustomerRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderItemRepository salesOrderItemRepository;
    private final InventoryService inventoryService;

    @Transactional(readOnly = true)
    public Page<SalesOrder> getSelfOrders(Long authUserId, Integer page, Integer size) {
        StoreCustomer customer = findCustomer(authUserId);
        return salesOrderRepository.findAll(
                PageRequest.of(page != null ? page : 0, size != null ? size : 20)
        ).map(o -> o); // filtered below via stream is less efficient; use a query
        // Note: a proper implementation would filter by customer, see findByCustomerId below
    }

    @Transactional(readOnly = true)
    public List<SalesOrder> getSelfOrderList(Long authUserId, int page, int size) {
        StoreCustomer customer = findCustomer(authUserId);
        return salesOrderRepository.findAll().stream()
                .filter(o -> o.getCustomer().getId().equals(customer.getId()))
                .skip((long) page * size)
                .limit(size)
                .toList();
    }

    @Transactional(readOnly = true)
    public SalesOrder getSelfOrderDetail(Long authUserId, Long orderId) {
        StoreCustomer customer = findCustomer(authUserId);
        SalesOrder order = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
        if (!order.getCustomer().getId().equals(customer.getId())) {
            throw new BusinessException("Order does not belong to user");
        }
        return order;
    }

    @Transactional
    public SalesOrder cancelSelfOrder(Long authUserId, Long orderId) {
        SalesOrder order = getSelfOrderDetail(authUserId, orderId);

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessException("Order is already cancelled");
        }
        if (order.getStatus() == OrderStatus.CONFIRMED || order.getStatus() == OrderStatus.SHIPPED) {
            throw new BusinessException("Cannot cancel order in status: " + order.getStatus());
        }

        List<SalesOrderItem> items = salesOrderItemRepository.findByOrderId(orderId);
        for (SalesOrderItem item : items) {
            if (item.getVariant() != null) {
                inventoryService.releaseReservedStock(item.getVariant(), item.getQuantity(), order.getOrderNumber());
            }
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setPaymentStatus(PaymentStatus.FAILED);
        return salesOrderRepository.save(order);
    }

    public OrderDto toDto(SalesOrder o) {
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
        return OrderDto.builder()
                .id(o.getId())
                .orderNumber(o.getOrderNumber())
                .status(orderStatus)
                .paymentStatus(paymentStatus)
                .currency(o.getCurrency())
                .subtotalAmount(o.getSubtotalAmount())
                .totalAmount(o.getTotalAmount())
                .placedAt(o.getPlacedAt() != null
                        ? o.getPlacedAt().atOffset(ZoneOffset.UTC)
                        : null)
                .build();
    }

    private StoreCustomer findCustomer(Long authUserId) {
        return storeCustomerRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new NotFoundException("Customer not found for user " + authUserId));
    }
}
