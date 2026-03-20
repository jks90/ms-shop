package com.makingcleancode.shop.dto.response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CheckoutResponseDto extends BasicResponseModel {
    private CheckoutDataDto data;
}