package com.ecommerce.cartorderservice.dto.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageClientResponse {
    private String s3Url;
    private boolean isPrimary;
}
