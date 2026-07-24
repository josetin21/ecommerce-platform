package com.ecommerce.productservice.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockAdjustmentRequest {

    @NotEmpty(message = "Items list cannot be empty")
    @Valid
    private List<StockAdjustmentItem> items;
}
