package com.ecommerce.cartorderservice.listener;

import com.ecommerce.cartorderservice.config.RabbitMQConfig;
import com.ecommerce.cartorderservice.dto.event.PaymentFailedEvent;
import com.ecommerce.cartorderservice.dto.event.PaymentSuccessEvent;
import com.ecommerce.cartorderservice.entity.Order;
import com.ecommerce.cartorderservice.entity.OrderStatus;
import com.ecommerce.cartorderservice.repository.OrderRepository;
import com.ecommerce.cartorderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final OrderService orderService;

    @RabbitListener(queues = RabbitMQConfig.PAYMENT_SUCCESS_QUEUE)
    public void handlePaymentSuccess(PaymentSuccessEvent event){
        log.info("Received PaymentSuccessEvent for orderId={}", event.getOrderId());
        orderService.handlePaymentSuccess(event);
    }

    @RabbitListener(queues = RabbitMQConfig.PAYMENT_FAILED_QUEUE)
    public void handlePaymentFailed(PaymentFailedEvent event){
        log.info("Received PaymentFailedEvent for orderId={}", event.getOrderId());
        orderService.handlePaymentFailed(event);
    }

}
