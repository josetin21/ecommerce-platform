package com.ecommerce.cartorderservice.service;

import com.ecommerce.cartorderservice.client.ProductServiceClient;
import com.ecommerce.cartorderservice.config.RabbitMQConfig;
import com.ecommerce.cartorderservice.dto.client.StockAdjustmentItem;
import com.ecommerce.cartorderservice.dto.client.StockAdjustmentRequest;
import com.ecommerce.cartorderservice.dto.event.PaymentFailedEvent;
import com.ecommerce.cartorderservice.dto.event.PaymentSuccessEvent;
import com.ecommerce.cartorderservice.dto.event.RefundFailedEvent;
import com.ecommerce.cartorderservice.dto.event.RefundProcessedEvent;
import com.ecommerce.cartorderservice.dto.request.PlaceOrderRequest;
import com.ecommerce.cartorderservice.dto.response.OrderCancelledEvent;
import com.ecommerce.cartorderservice.dto.response.OrderPlacedEvent;
import com.ecommerce.cartorderservice.dto.response.OrderResponse;
import com.ecommerce.cartorderservice.dto.response.PageResponse;
import com.ecommerce.cartorderservice.entity.Order;
import com.ecommerce.cartorderservice.entity.OrderItem;
import com.ecommerce.cartorderservice.entity.OrderStatus;
import com.ecommerce.cartorderservice.exception.InvalidOrderStateException;
import com.ecommerce.cartorderservice.exception.OrderNotFoundException;
import com.ecommerce.cartorderservice.exception.ResourceNotFoundException;
import com.ecommerce.cartorderservice.mapper.OrderMapper;
import com.ecommerce.cartorderservice.model.Cart;
import com.ecommerce.cartorderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final OrderMapper orderMapper;
    private final RabbitTemplate rabbitTemplate;
    private final ProductServiceClient productServiceClient;

    private static final Set<OrderStatus> CANCELLABLE_STATUSES = EnumSet.of(OrderStatus.PENDING, OrderStatus.CONFIRMED, OrderStatus.PROCESSING);

    @Transactional
    public OrderResponse placeOrder(UUID userId, String userEmail, PlaceOrderRequest request){

        Cart cart = cartService.getRawCart(userId);

        if (cart.getItems().isEmpty()){
            throw new ResourceNotFoundException("Cart is empty");
        }

        List<OrderItem> orderItems = cart.getItems().stream()
                .map(cartItem -> OrderItem.builder()
                        .productId(cartItem.getProductId())
                        .productName(cartItem.getProductName())
                        .unitPrice(cartItem.getUnitPrice())
                        .quantity(cartItem.getQuantity())
                        .subtotal(cartItem.getSubtotal())
                        .build())
                .toList();

        productServiceClient.reserveStock(toStockAdjustmentRequest(orderItems));

        Order order = Order.builder()
                .userId(userId)
                .userEmail(userEmail)
                .shippingAddress(request.getShippingAddress())
                .totalAmount(cart.getTotalAmount())
                .build();

        orderItems.forEach(item -> item.setOrder(order));
        order.setItems(orderItems);

        Order savedOrder = orderRepository.save(order);
        cartService.clearCart(userId);

        publishOrderPlacedEvent(savedOrder);

        log.info("Order placed: {} for user: {}", savedOrder.getId(), userId);
        return orderMapper.toOrderResponse(savedOrder);
    }

    public PageResponse<OrderResponse> getOrderHistory(UUID userId, int page, int size){
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orders = orderRepository.findByUserIdOrderByPlacedAtDesc(userId, pageable);

        return PageResponse.<OrderResponse>builder()
                .content(orders.getContent().stream()
                        .map(orderMapper::toOrderResponse)
                        .toList())
                .page(orders.getNumber())
                .size(orders.getSize())
                .totalElements(orders.getTotalElements())
                .totalPages(orders.getTotalPages())
                .last(orders.isLast())
                .build();
    }

    public OrderResponse getOrderById(UUID userId, UUID orderId){
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new OrderNotFoundException(orderId.toString()));

        return orderMapper.toOrderResponse(order);
    }

    @Transactional
    public OrderResponse cancelOrder(UUID userId, UUID orderId){
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new OrderNotFoundException(orderId.toString()));

        if (!CANCELLABLE_STATUSES.contains(order.getStatus())){
            throw new InvalidOrderStateException(
                    "Order cannot be cancelled from status: " + order.getStatus());
        }

        if (order.getStatus() == OrderStatus.PENDING){
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
            productServiceClient.releaseStock(toStockAdjustmentRequest(order.getItems()));
            log.info("Order cancelled directly (was PENDING, no payment to refund): {}", orderId);
        } else {
          publishOrderCancelledEvent(order);
          log.info("Order cancellation initiated, refund pending: {}", orderId);
        }

        return orderMapper.toOrderResponse(order);
    }

    @Transactional
    public void handlePaymentSuccess(PaymentSuccessEvent event){
        Order order = orderRepository.findById(event.getOrderId()).orElse(null);

        if (order == null){
            log.warn("Order not found for orderId={}, skipping status update", event.getOrderId());
            return;
        }

        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);
        log.info("Order status updated to CONFIRMED for orderId={}", event.getOrderId());
    }

    @Transactional
    public void handlePaymentFailed(PaymentFailedEvent event){
        Order order = orderRepository.findById(event.getOrderId()).orElse(null);

        if (order == null){
            log.warn("Order not found for orderId={}, skipping status update", event.getOrderId());
            return;
        }

        order.setStatus(OrderStatus.PAYMENT_FAILED);
        orderRepository.save(order);
        productServiceClient.releaseStock(toStockAdjustmentRequest(order.getItems()));
        log.info("Order status updated to PAYMENT_FAILED for orderId={}", event.getOrderId());
    }

    @Transactional
    public void handleRefundProcessed(RefundProcessedEvent event){
        Order order = orderRepository.findById(event.getOrderId()).orElse(null);

        if (order == null){
            log.warn("Order not found for orderId={}, skipping refund status update", event.getOrderId());
            return;
        }
        order.setStatus(OrderStatus.REFUNDED);
        orderRepository.save(order);
        productServiceClient.releaseStock(toStockAdjustmentRequest(order.getItems()));
        log.info("Order status updated to REFUNDED for orderId={}", event.getOrderId());
    }

    @Transactional
    public void handleRefundFailed(RefundFailedEvent event){
        log.warn("Refund failed for orderId={}, reason={}, Order status left unchanged for manual follow-up",
                event.getOrderId(), event.getReason());
    }

    private StockAdjustmentRequest toStockAdjustmentRequest(List<OrderItem> items){
        List<StockAdjustmentItem> adjustmentItems = items.stream()
                .map(item -> StockAdjustmentItem.builder()
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .build())
                .toList();

        return StockAdjustmentRequest.builder().items(adjustmentItems).build();
    }

    private void publishOrderCancelledEvent(Order order){
        OrderCancelledEvent event = OrderCancelledEvent.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .userEmail(order.getUserEmail())
                .build();

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_EXCHANGE,
                RabbitMQConfig.ORDER_CANCELLED_ROUTING_KEY,
                event);

        log.info("Order cancelled event published for order: {}", order.getId());
    }

    private void publishOrderPlacedEvent(Order order){
        OrderPlacedEvent event = orderMapper.toOrderPlacedEvent(order);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_EXCHANGE,
                RabbitMQConfig.ORDER_PLACED_ROUTING_KEY,
                event);

        log.info("Order placed event published for order: {}", order.getId());
    }
}
