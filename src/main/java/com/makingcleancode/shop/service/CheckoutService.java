package com.makingcleancode.shop.service;

import com.makingcleancode.shop.dto.request.CheckoutRequestDto;
import com.makingcleancode.shop.dto.response.BasicStatusModel;
import com.makingcleancode.shop.dto.response.CheckoutDataDto;
import com.makingcleancode.shop.dto.response.CheckoutResponseDto;
import com.makingcleancode.shop.entity.*;
import com.makingcleancode.shop.repository.*;
import com.makingcleancode.shop.enums.OrderStatus;
import com.makingcleancode.shop.enums.PaymentMode;
import com.makingcleancode.shop.enums.PaymentStatus;
import com.makingcleancode.shop.exception.BusinessException;
import com.makingcleancode.shop.exception.NotFoundException;
import com.makingcleancode.shop.service.dto.CheckoutLineItem;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final StoreCustomerRepository storeCustomerRepository;
    private final CustomerAddressRepository customerAddressRepository;
    private final ShoppingCartRepository shoppingCartRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderItemRepository salesOrderItemRepository;
    private final OrderAddressSnapshotRepository orderAddressSnapshotRepository;
    private final InventoryService inventoryService;
    private final StripeGateway stripeGateway;

    private static final AtomicLong SEQ = new AtomicLong(1000);

    @Transactional
    public ResponseEntity<CheckoutResponseDto> createCheckout(Long authUserId, CheckoutRequestDto request) {
        if (request.getPaymentMode() != PaymentMode.CHECKOUT_SESSION) {
            throw new BusinessException("Only CHECKOUT_SESSION is supported in this implementation");
        }

        StoreCustomer customer = storeCustomerRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new NotFoundException("Store customer not found for auth user id " + authUserId));

        CustomerAddress shippingAddress = customerAddressRepository
                .findByIdAndCustomerId(request.getShippingAddressId(), customer.getId())
                .orElseThrow(() -> new NotFoundException("Shipping address not found"));

        CustomerAddress billingAddress = customerAddressRepository
                .findByIdAndCustomerId(request.getBillingAddressId(), customer.getId())
                .orElseThrow(() -> new NotFoundException("Billing address not found"));

        ShoppingCart cart = shoppingCartRepository.findByCustomerId(customer.getId())
                .orElseThrow(() -> new NotFoundException("Shopping cart not found"));

        if (!"ACTIVE".equals(cart.getStatus())) {
            throw new BusinessException("Cart is not active");
        }

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new BusinessException("Cart is empty");
        }

        String currency = null;
        long subtotal = 0L;
        List<CheckoutLineItem> stripeLineItems = new ArrayList<>();

        for (ShoppingCartItem cartItem : cart.getItems()) {
            ProductVariant variant = cartItem.getVariant();

            if (variant == null || !Boolean.TRUE.equals(variant.getIsActive())) {
                throw new BusinessException("Cart contains inactive or missing variant");
            }

            if (!"ACTIVE".equals(variant.getProduct().getStatus())) {
                throw new BusinessException("Cart contains product that is not active");
            }

            if (cartItem.getQuantity() == null || cartItem.getQuantity() <= 0) {
                throw new BusinessException("Cart contains invalid quantity");
            }

            if (currency == null) {
                currency = variant.getCurrency();
            } else if (!currency.equalsIgnoreCase(variant.getCurrency())) {
                throw new BusinessException("Cart contains mixed currencies");
            }

            int freeStock = variant.getStockAvailable() - variant.getStockReserved();
            if (freeStock < cartItem.getQuantity()) {
                throw new BusinessException("Insufficient stock for sku " + variant.getSku());
            }

            long unitPrice = variant.getPriceAmount();
            long lineSubtotal = unitPrice * cartItem.getQuantity();
            subtotal += lineSubtotal;

            stripeLineItems.add(
                    CheckoutLineItem.builder()
                            .name(variant.getProduct().getName())
                            .description(variant.getName() != null ? variant.getName() : variant.getSku())
                            .unitAmount(unitPrice)
                            .currency(variant.getCurrency())
                            .quantity(cartItem.getQuantity().longValue())
                            .build()
            );
        }

        long discountAmount = 0L;
        long shippingAmount = 0L;
        long taxAmount = 0L;
        long totalAmount = subtotal - discountAmount + shippingAmount + taxAmount;

        SalesOrder order = new SalesOrder();
        order.setOrderNumber(generateOrderNumber());
        order.setCustomer(customer);
        order.setAuthUserId(authUserId);
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setCurrency(currency);
        order.setSubtotalAmount(subtotal);
        order.setDiscountAmount(discountAmount);
        order.setShippingAmount(shippingAmount);
        order.setTaxAmount(taxAmount);
        order.setTotalAmount(totalAmount);
        order.setNotes(request.getNotes());
        order.setPlacedAt(LocalDateTime.now());

        order = salesOrderRepository.save(order);

        saveAddressSnapshot(order, shippingAddress, "SHIPPING");
        saveAddressSnapshot(order, billingAddress, "BILLING");

        for (ShoppingCartItem cartItem : cart.getItems()) {
            ProductVariant variant = cartItem.getVariant();
            long unitPrice = variant.getPriceAmount();
            long lineSubtotal = unitPrice * cartItem.getQuantity();

            SalesOrderItem orderItem = new SalesOrderItem();
            orderItem.setOrder(order);
            orderItem.setVariant(variant);
            orderItem.setSku(variant.getSku());
            orderItem.setProductName(variant.getProduct().getName());
            orderItem.setVariantName(variant.getName());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPriceAmount(unitPrice);
            orderItem.setSubtotalAmount(lineSubtotal);
            orderItem.setCurrency(variant.getCurrency());
            orderItem.setAttributesJson(null);
            salesOrderItemRepository.save(orderItem);

            inventoryService.reserveStock(variant, cartItem.getQuantity(), order.getOrderNumber());
        }

        StripeGateway.StripeCheckoutResult stripeResult = stripeGateway.createCheckout(
                customer,
                order,
                request.getPaymentMode(),
                stripeLineItems,
                request.getSuccessUrl(),
                request.getCancelUrl()
        );

        if ((customer.getStripeCustomerId() == null || customer.getStripeCustomerId().isBlank())
                && stripeResult.stripeCustomerId() != null) {
            customer.setStripeCustomerId(stripeResult.stripeCustomerId());
            storeCustomerRepository.save(customer);
        }

        order.setStripeCustomerId(stripeResult.stripeCustomerId());
        order.setStripePaymentIntentId(stripeResult.paymentIntentId());
        order.setStripeCheckoutSessionId(stripeResult.checkoutSessionId());
        salesOrderRepository.save(order);

        cart.setStatus("CHECKED_OUT");
        shoppingCartRepository.save(cart);

        CheckoutResponseDto response = CheckoutResponseDto.builder()
                .status(BasicStatusModel.builder()
                        .message("Created")
                        .code(HttpStatus.CREATED.value())
                        .build())
                .data(CheckoutDataDto.builder()
                        .orderId(order.getId())
                        .orderNumber(order.getOrderNumber())
                        .paymentMode(request.getPaymentMode())
                        .paymentStatus(order.getPaymentStatus())
                        .stripeCustomerId(order.getStripeCustomerId())
                        .stripePaymentIntentId(order.getStripePaymentIntentId())
                        .stripeCheckoutSessionId(order.getStripeCheckoutSessionId())
                        .clientSecret(null)
                        .checkoutUrl(stripeResult.checkoutUrl())
                        .build())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private void saveAddressSnapshot(SalesOrder order, CustomerAddress address, String type) {
        OrderAddressSnapshot snapshot = new OrderAddressSnapshot();
        snapshot.setOrder(order);
        snapshot.setType(type);
        snapshot.setRecipientName(address.getRecipientName());
        snapshot.setLine1(address.getLine1());
        snapshot.setLine2(address.getLine2());
        snapshot.setPostalCode(address.getPostalCode());
        snapshot.setCity(address.getCity());
        snapshot.setStateRegion(address.getStateRegion());
        snapshot.setCountryCode(address.getCountryCode());
        snapshot.setPhone(address.getPhone());
        orderAddressSnapshotRepository.save(snapshot);
    }

    private String generateOrderNumber() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long seq = SEQ.incrementAndGet();
        return "ORD-" + date + "-" + String.format("%06d", seq);
    }
}