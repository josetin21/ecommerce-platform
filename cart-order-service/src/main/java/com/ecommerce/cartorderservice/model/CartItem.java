package com.ecommerce.cartorderservice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CartItem implements Serializable {

    private UUID productId;
    private String productName;
    private BigDecimal unitPrice;
    private Integer quantity;
    private String imageUrl;

    public BigDecimal getSubtotal(){
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
