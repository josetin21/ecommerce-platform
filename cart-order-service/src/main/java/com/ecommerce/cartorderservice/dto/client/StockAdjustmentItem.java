package com.ecommerce.cartorderservice.dto.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockAdjustmentItem {
    private UUID id;
    private Integer quantity;
}
