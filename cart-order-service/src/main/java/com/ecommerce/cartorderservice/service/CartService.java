package com.ecommerce.cartorderservice.service;

import com.ecommerce.cartorderservice.dto.request.AddToCartRequest;
import com.ecommerce.cartorderservice.dto.request.UpdateCartItemRequest;
import com.ecommerce.cartorderservice.dto.response.CartResponse;
import com.ecommerce.cartorderservice.mapper.CartMapper;
import com.ecommerce.cartorderservice.model.Cart;
import com.ecommerce.cartorderservice.model.CartItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final RedisTemplate<String, Cart> cartRedisTemplate;
    private final CartMapper cartMapper;

    private static final String CART_KEY_PREFIX = "cart:";
    private static final long CART_TTL_DAYS = 7;

    public CartResponse getCart(UUID userId){
        Cart cart = getOrCreateCart(userId);
        return cartMapper.toCartResponse(cart);
    }

    public CartResponse addToCart(UUID userId, AddToCartRequest request){
        Cart cart = getOrCreateCart(userId);

        CartItem item = CartItem.builder()
                .productId(request.getProductId())
                .productName(request.getProductName())
                .unitPrice(request.getUnitPrice())
                .quantity(request.getQuantity())
                .imageUrl(request.getImageUrl())
                .build();

        cart.addItem(item);
        saveCart(userId, cart);

        log.info("Items added to cart for users: {}", userId);
        return cartMapper.toCartResponse(cart);
    }

    public CartResponse updateCartItem(UUID userId, UUID productId, UpdateCartItemRequest request){
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
