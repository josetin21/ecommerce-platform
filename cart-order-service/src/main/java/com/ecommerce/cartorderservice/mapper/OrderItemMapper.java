package com.ecommerce.cartorderservice.mapper;

import com.ecommerce.cartorderservice.dto.response.OrderItemResponse;
import com.ecommerce.cartorderservice.entity.OrderItem;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {
    OrderItemResponse toOrderItemResponse(OrderItem orderItem);
}
