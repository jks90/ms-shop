package com.makingcleancode.shop.dto.request;

import com.makingcleancode.shop.enums.PaymentMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CheckoutRequestDto {

    @NotNull
    private Long shippingAddressId;

    @NotNull
    private Long billingAddressId;

    @NotNull
    private PaymentMode paymentMode;

    @NotBlank
    @Size(max = 1000)
    private String successUrl;

    @NotBlank
    @Size(max = 1000)
    private String cancelUrl;

    @Size(max = 1000)
    private String notes;
}
