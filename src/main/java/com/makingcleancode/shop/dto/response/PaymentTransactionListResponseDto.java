package com.makingcleancode.shop.dto.response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PaymentTransactionListResponseDto extends BasicResponseModel {
    private List<PaymentTransactionDto> data;
}