package com.ecommerce.paymentservice.controller;

import com.ecommerce.paymentservice.dto.request.CreatePaymentOrderRequest;
import com.ecommerce.paymentservice.dto.request.VerifyPaymentRequest;
import com.ecommerce.paymentservice.dto.response.PaymentOrderResponse;
import com.ecommerce.paymentservice.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Payments", description = "Payment processing endpoints")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @Operation(summary = "Create Razorpay order for payment")
    public ResponseEntity<PaymentOrderResponse> createPaymentOrder(Authentication authentication,
                                                                   @Valid @RequestBody CreatePaymentOrderRequest request){
        UUID userId =  extractUserId(authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.createPaymentOrder(request, userId));
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify payment signature after checkout")
    public ResponseEntity<Void> verifyPayment(@Valid @RequestBody VerifyPaymentRequest request){
        paymentService.verifyPayment(request);
        return ResponseEntity.ok().build();
    }

    private UUID extractUserId(Authentication authentication){
        return UUID.fromString((String) authentication.getPrincipal());
    }

    private String extractEmail(Authentication authentication){
        return (String) authentication.getCredentials();
    }
}
