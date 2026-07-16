package com.ecommerce.paymentservice.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefundProcessedEvent {
    private UUID orderId;
    private UUID paymentId;
    private String razorpayRefundId;
    private BigDecimal amount;
    private String userEmail;
}
