package com.ecommerce.cartorderservice.dto.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductClientResponse {
    private UUID id;
    private String name;
    private BigDecimal price;
    private Integer stockQuantity;
    private boolean active;
    private List<ProductImageClientResponse> images;

    public String getPrimaryImageUrl(){
        if (images == null || images.isEmpty()){
            return null;
        }
        return images.stream()
                .filter(ProductImageClientResponse::isPrimary)
                .findFirst()
                .orElse(images.get(0))
                .getS3Url();
    }
}
