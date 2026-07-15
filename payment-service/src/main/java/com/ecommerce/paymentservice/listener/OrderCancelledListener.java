package com.ecommerce.paymentservice.listener;

import com.ecommerce.paymentservice.config.RabbitMQConfig;
import com.ecommerce.paymentservice.dto.event.OrderCancelledEvent;
import com.ecommerce.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCancelledListener {

    private final PaymentService paymentService;

    @RabbitListener(queues = RabbitMQConfig.ORDER_CANCELLED_QUEUE)
    public void handleOrderCancelled(OrderCancelledEvent event){
        log.info("Received OrderCancelledEvent for orderId={}", event.getOrderId());
        paymentService.processRefund(event.getOrderId());
    }
}
