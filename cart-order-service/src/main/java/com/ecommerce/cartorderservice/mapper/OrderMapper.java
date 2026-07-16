package com.ecommerce.cartorderservice.mapper;

import com.ecommerce.cartorderservice.dto.response.OrderItemResponse;
import com.ecommerce.cartorderservice.dto.response.OrderPlacedEvent;
import com.ecommerce.cartorderservice.dto.response.OrderResponse;
import com.ecommerce.cartorderservice.entity.Order;
import com.ecommerce.cartorderservice.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {OrderItemMapper.class})
public interface OrderMapper {

    @Mapping(source = "status", target = "status")
    OrderResponse toOrderResponse(Order order);

    @Mapping(source = "id", target = "orderId")
    OrderPlacedEvent toOrderPlacedEvent(Order order);


}
