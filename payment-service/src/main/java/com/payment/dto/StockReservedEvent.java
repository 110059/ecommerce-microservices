package com.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockReservedEvent {

    private Long orderId;
    private Long userId;
    private Long productId;
    private Integer quantity;
    private Double totalPrice;
}