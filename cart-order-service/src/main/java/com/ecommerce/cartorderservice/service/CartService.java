package com.ecommerce.cartorderservice.service;

import com.ecommerce.cartorderservice.client.ProductServiceClient;
import com.ecommerce.cartorderservice.dto.client.ProductClientResponse;
import com.ecommerce.cartorderservice.dto.request.AddToCartRequest;
import com.ecommerce.cartorderservice.dto.request.UpdateCartItemRequest;
import com.ecommerce.cartorderservice.dto.response.CartResponse;
import com.ecommerce.cartorderservice.exception.InsufficientStockException;
import com.ecommerce.cartorderservice.exception.ResourceNotFoundException;
import com.ecommerce.cartorderservice.mapper.CartMapper;
import com.ecommerce.cartorderservice.model.Cart;
import com.ecommerce.cartorderservice.model.CartItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final RedisTemplate<String, Cart> cartRedisTemplate;
    private final CartMapper cartMapper;
    private final ProductServiceClient productServiceClient;

    private static final String CART_KEY_PREFIX = "cart:";
    private static final long CART_TTL_DAYS = 7;

    public CartResponse getCart(UUID userId){
        Cart cart = getOrCreateCart(userId);
        return cartMapper.toCartResponse(cart);
    }

    public CartResponse addToCart(UUID userId, AddToCartRequest request){
        ProductClientResponse product = productServiceClient.getProduct(request.getProductId());

        if (!product.isActive()){
            throw new ResourceNotFoundException("Product is not available: " + request.getProductId());
        }

        Cart cart = getOrCreateCart(userId);

        int existingQuantity = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(request.getProductId()))
                .mapToInt(CartItem::getQuantity)
                .findFirst()
                .orElse(0);

        int totalRequestQuantity = existingQuantity + request.getQuantity();

        if (totalRequestQuantity > product.getStockQuantity()){
            throw new InsufficientStockException("Only " + product.getStockQuantity() + " unit(s) available for: " + product.getName());
        }

        CartItem item = CartItem.builder()
                .productId(product.getId())
                .productName(product.getName())
                .unitPrice(product.getPrice())
                .quantity(request.getQuantity())
                .imageUrl(product.getPrimaryImageUrl())
                .build();

        cart.addItem(item);
        saveCart(userId, cart);

        log.info("Items added to cart for users: {}", userId);
        return cartMapper.toCartResponse(cart);
    }

    public CartResponse updateCartItem(UUID userId, UUID productId, UpdateCartItemRequest request){
        ProductClientResponse product = productServiceClient.getProduct(productId);

        if (request.getQuantity() > product.getStockQuantity()){
            throw new InsufficientStockException(
                    "Only " + product.getStockQuantity() + " unit(s) available for: " + product.getName());
        }

        Cart cart = getOrCreateCart(userId);
        cart.updateItemQuantity(productId, request.getQuantity());
        saveCart(userId, cart);

        log.info("Cart items updated for user: {}", userId);
        return cartMapper.toCartResponse(cart);
    }

    public CartResponse removeFromCart(UUID userId, UUID productId){
        Cart cart = getOrCreateCart(userId);
        cart.removeItem(productId);
        saveCart(userId, cart);

        log.info("Item removed from cart for user: {}", userId);
        return cartMapper.toCartResponse(cart);
    }

    public void clearCart(UUID userId){
        cartRedisTemplate.delete(cartKey(userId));
        log.info("Cart cleared for user: {}", userId);
    }

    public Cart getRawCart(UUID userId){
        return getOrCreateCart(userId);
    }

    private Cart getOrCreateCart(UUID userId){
        Cart cart = cartRedisTemplate.opsForValue().get(cartKey(userId));
        if (cart == null){
            cart = Cart.builder()
                    .userId(userId)
                    .build();
        }
        return cart;
    }

    private void saveCart(UUID userId, Cart cart){
        cartRedisTemplate.opsForValue().set(cartKey(userId), cart, CART_TTL_DAYS, TimeUnit.DAYS);
    }

    private String cartKey(UUID userId){
        return CART_KEY_PREFIX + userId.toString();
    }
}
