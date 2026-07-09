package com.ecommerce.paymentservice.controller;

import com.ecommerce.paymentservice.config.RazorpayProperties;
import com.ecommerce.paymentservice.service.PaymentService;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Webhook", description = "Razorpay webhook endpoint (server-to-server, no JWT)")
public class WebhookController {

    private final PaymentService paymentService;
    private final RazorpayProperties razorpayProperties;

    @PostMapping("/webhook")
    @Operation(summary = "Handle Razorpay webhook events")
    public ResponseEntity<Void> handleWebhook(@RequestBody String payload,
                                              @RequestHeader("X-Razorpay-Signature") String signature){

        boolean isValid;
        try{
            isValid = Utils.verifyWebhookSignature(payload,signature, razorpayProperties.getWebhookSecret());
        }catch (RazorpayException e){
            isValid = false;
        }

        if (!isValid){
            log.warn("Invalid webhook signature received");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        try{
            paymentService.handleWebHookEvent(payload);
        }catch (Exception e){
            log.error("Webhook processing failed: {}", e.getMessage());
        }

        return ResponseEntity.ok().build();
    }
}
