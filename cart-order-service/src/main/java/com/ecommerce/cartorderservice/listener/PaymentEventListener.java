package com.ecommerce.cartorderservice.listener;

import com.ecommerce.cartorderservice.config.RabbitMQConfig;
import com.ecommerce.cartorderservice.dto.event.PaymentFailedEvent;
import com.ecommerce.cartorderservice.dto.event.PaymentSuccessEvent;
import com.ecommerce.cartorderservice.entity.Order;
import com.ecommerce.cartorderservice.entity.OrderStatus;
import com.ecommerce.cartorderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final OrderRepository orderRepository;

    @RabbitListener(queues = RabbitMQConfig.PAYMENT_SUCCESS_QUEUE)
    public void handlePaymentSuccess(PaymentSuccessEvent event){
        log.info("Received PaymentSuccessEvent for orderId={}", event.getOrderId());

        Order order = orderRepository.findById(event.getOrderId())
                .orElse(null);

        if (order == null){
            log.warn("Order not found for orderId={}, skipping status update", event.getOrderId());
            return;
        }

        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);
        log.info("Order status updated to CONFIRMED for orderId={}", event.getOrderId());
    }

    @RabbitListener(queues = RabbitMQConfig.PAYMENT_FAILED_QUEUE)
    public void handlePaymentFailed(PaymentFailedEvent event){
        log.info("Received PaymentFailedEvent for orderId={}", event.getOrderId());

        Order order = orderRepository.findById(event.getOrderId())
                .orElse(null);

        if (order == null){
            log.warn("Order not found for orderId={}, skipping status update", event.getOrderId());
            return;
        }

        order.setStatus(OrderStatus.PAYMENT_FAILED);
        orderRepository.save(order);
        log.info("Order status updated to PAYMENT_FAILED for orderId={}", event.getOrderId());
    }

}
