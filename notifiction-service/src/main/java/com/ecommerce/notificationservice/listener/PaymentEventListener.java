package com.ecommerce.notificationservice.listener;

import com.ecommerce.notificationservice.config.RabbitMQConfig;
import com.ecommerce.notificationservice.dto.event.PaymentFailedEvent;
import com.ecommerce.notificationservice.dto.event.PaymentSuccessEvent;
import com.ecommerce.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitMQConfig.PAYMENT_SUCCESS_QUEUE)
    public void handlePaymentSuccess(PaymentSuccessEvent event){
        log.info("Received PaymentSuccessEvent for paymentId={}", event.getPaymentId());
        notificationService.sendPaymentSuccessEmail(event);
    }

    @RabbitListener(queues = RabbitMQConfig.PAYMENT_FAILED_QUEUE)
    public void handlePaymentFailed(PaymentFailedEvent event){
        log.info("Received PaymentFailedEvent for paymentId={}", event.getPaymentId());
        notificationService.sendPaymentFailedEmail(event);
    }
}
