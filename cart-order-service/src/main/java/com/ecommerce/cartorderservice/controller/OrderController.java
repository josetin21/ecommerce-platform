package com.ecommerce.cartorderservice.controller;

import com.ecommerce.cartorderservice.dto.request.PlaceOrderRequest;
import com.ecommerce.cartorderservice.dto.response.OrderResponse;
import com.ecommerce.cartorderservice.dto.response.PageResponse;
import com.ecommerce.cartorderservice.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Orders", description = "Order management endpoints")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Place order from cart")
    public ResponseEntity<OrderResponse> placeOrder(Authentication authentication,
                                                    @Valid @RequestBody PlaceOrderRequest request){
        UUID userId = extractUserId(authentication);
        String userEmail = extractEmail(authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.placeOrder(userId, userEmail, request));
    }

    @GetMapping
    @Operation(summary = "Get order history")
    public ResponseEntity<PageResponse<OrderResponse>> getOrderHistory(Authentication authentication,
                                                                       @RequestParam(defaultValue = "0") int page,
                                                                       @RequestParam(defaultValue = "10") int size){
        UUID userId = extractUserId(authentication);
        return ResponseEntity.ok(orderService.getOrderHistory(userId, page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Order by id")
    public ResponseEntity<OrderResponse> getOderById(Authentication authentication,
                                                     @PathVariable UUID id){
        UUID userId = extractUserId(authentication);
        return ResponseEntity.ok(orderService.getOrderById(userId, id));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel an order (triggers refund if already paid)")
    public ResponseEntity<OrderResponse> cancelOrder(Authentication authentication,
                                                     @PathVariable UUID id){
        UUID userId = extractUserId(authentication);
        return ResponseEntity.ok(orderService.cancelOrder(userId, id));
    }

    private UUID extractUserId(Authentication authentication){
        return UUID.fromString((String) authentication.getPrincipal());
    }

    private String extractEmail(Authentication authentication){
        return (String) authentication.getCredentials();
    }
}
