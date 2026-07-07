package com.ecommerce.paymentservice.config;

import com.razorpay.RazorpayClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RazorpayConfig {

    private final RazorpayProperties razorpayProperties;

    public RazorpayClient razorpayClient() throws Exception{
        return new RazorpayClient(razorpayProperties.getKeyId(), razorpayProperties.getKeySecret());
    }
}
