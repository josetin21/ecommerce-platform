package com.ecommerce.paymentservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.razorpay")
public class RazorpayProperties {
    private String keyId;
    private String keySecret;
}
