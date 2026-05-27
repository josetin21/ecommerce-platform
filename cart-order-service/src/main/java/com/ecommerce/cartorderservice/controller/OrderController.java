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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
    public ResponseEntity<OrderResponse> placeOrder(@AuthenticationPrincipal UserDetails userDetails,
                                                    @Valid @RequestBody PlaceOrderRequest request){
        UUID userId = extractUserId(userDetails);
        String userEmail = userDetails.getUsername();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.placeOder(userId, userEmail, request));
    }

    @GetMapping
    @Operation(summary = "Get order history")
    public ResponseEntity<PageResponse<OrderResponse>> getOrderHistory(@AuthenticationPrincipal UserDetails userDetails,
                                                                       @RequestParam(defaultValue = "0") int page,
                                                                       @RequestParam(defaultValue = "10") int size){
        UUID userId = extractUserId(userDetails);
        return ResponseEntity.ok(orderService.getOrderHistory(userId, page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Order by id")
    public ResponseEntity<OrderResponse> getOderById(@AuthenticationPrincipal UserDetails userDetails,
                                                     @PathVariable UUID id){
        UUID userId = extractUserId(userDetails);
        return ResponseEntity.ok(orderService.getOrderById(userId, id));
    }

    private UUID extractUserId(UserDetails userDetails){
        return UUID.fromString(userDetails.getUsername());
    }
}
