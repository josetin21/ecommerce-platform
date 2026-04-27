package com.ecommerce.productservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class UpdateProductRequest {

    @Size(min = 2, max = 200)
    private String name;

    private String description;

    @DecimalMin(value = "0.01")
    private BigDecimal price;

    @Min(value = 0)
    private Integer stockQuantity;

    private UUID categoryId;

    private Boolean isActive;
}
