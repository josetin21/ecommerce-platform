package com.ecommerce.cartorderservice.controller;

import com.ecommerce.cartorderservice.dto.request.AddToCartRequest;
import com.ecommerce.cartorderservice.dto.request.UpdateCartItemRequest;
import com.ecommerce.cartorderservice.dto.response.CartResponse;
import com.ecommerce.cartorderservice.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Cart", description = "Cart management endpoints")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Get current user cart")
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal UserDetails userDetails){
        UUID userId = extractUserId(userDetails);
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @PostMapping("/items")
    @Operation(summary = "Add items to cart")
    public ResponseEntity<CartResponse> addToCart(@AuthenticationPrincipal UserDetails userDetails,
                                                  @Valid @RequestBody AddToCartRequest request){
        UUID userId = extractUserId(userDetails);
        return ResponseEntity.ok(cartService.addToCart(userId, request));
    }

    @PutMapping("/items/{productId}")
    @Operation(summary = "Update item quantity in cart")
    public ResponseEntity<CartResponse> updateCartItem(@AuthenticationPrincipal UserDetails userDetails,
                                                       @PathVariable UUID productId,
                                                       @Valid @RequestBody UpdateCartItemRequest request){
        UUID userId = extractUserId(userDetails);
        return ResponseEntity.ok(cartService.updateCartItem(userId, productId, request));
    }

    @DeleteMapping("/items/{productId}")
    @Operation(summary = "Remove items from cart")
    public ResponseEntity<CartResponse> removeFromCart(@AuthenticationPrincipal UserDetails userDetails,
                                                       @PathVariable UUID productId){
        UUID userId = extractUserId(userDetails);
        return ResponseEntity.ok(cartService.removeFromCart(userId, productId));
    }

    @DeleteMapping
    @Operation(summary = "Clear Cart")
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal UserDetails userDetails){
        UUID userId = extractUserId(userDetails);
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }


    private UUID extractUserId(UserDetails userDetails){
        return UUID.fromString(userDetails.getUsername());
    }
}
