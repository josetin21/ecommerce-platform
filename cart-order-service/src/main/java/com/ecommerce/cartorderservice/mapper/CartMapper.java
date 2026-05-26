package com.ecommerce.cartorderservice.mapper;

import com.ecommerce.cartorderservice.dto.response.CartItemResponse;
import com.ecommerce.cartorderservice.dto.response.CartResponse;
import com.ecommerce.cartorderservice.model.Cart;
import com.ecommerce.cartorderservice.model.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartMapper {

    @Mapping(source = "subtotal", target = "subtotal")
    CartItemResponse toCartItemResponse(CartItem cartItem);

    @Mapping(source = "totalAmount", target = "totalAmount")
    @Mapping(source = "totalItems", target = "totalItems")
    CartResponse toCartResponse(Cart cart);
}
