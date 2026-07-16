package com.ecommerce.cartorderservice.listener;

import com.ecommerce.cartorderservice.config.RabbitMQConfig;
import com.ecommerce.cartorderservice.dto.event.RefundFailedEvent;
import com.ecommerce.cartorderservice.dto.event.RefundProcessedEvent;
import com.ecommerce.cartorderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefundEventListener {

    private final OrderService orderService;

    @RabbitListener(queues = RabbitMQConfig.REFUND_PROCESSED_QUEUE)
    public void handleRefundProcessed(RefundProcessedEvent event){
        log.info("Received RefundProcessedEvent for orderId={}", event.getOrderId());
        orderService.handleRefundProcessed(event);
    }

    @RabbitListener(queues = RabbitMQConfig.REFUND_FAILED_QUEUE)
    public void handleRefundFailed(RefundFailedEvent event){
        log.info("Received RefundFailedEvent for orderId={}", event.getOrderId());
        orderService.handleRefundFailed(event);
    }
}
