package com.ecommerce.notificationservice.service;

import com.ecommerce.notificationservice.dto.event.OrderItemResponse;
import com.ecommerce.notificationservice.dto.event.OrderPlacedEvent;
import com.ecommerce.notificationservice.dto.event.PaymentFailedEvent;
import com.ecommerce.notificationservice.dto.event.PaymentSuccessEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    public void sendOrderConfirmationEmail(OrderPlacedEvent event){
        StringBuilder body = new StringBuilder();
        body.append("Hi,\n\nYour order has been placed successfully.\n\n");
        body.append("Order ID: ").append(event.getOrderId()).append("\n");
        body.append("Shipping Address: ").append(event.getShippingAddress()).append("\n\n");
        body.append("Items:\n");
        for (OrderItemResponse item : event.getItems()){
            body.append("- ").append(item.getProductName())
                    .append(" X").append(item.getQuantity())
                    .append(" (").append(item.getSubtotal()).append(")\n");
        }
        body.append("\nTotal: ").append(event.getTotalAmount());
        body.append("\n\nThanks you for shopping with us!");

        sendEmail(event.getUserEmail(), "Order Confirmation - " + event.getOrderId(), body.toString());
        log.info("Order confirmation email sent for orderId={}", event.getOrderId());
    }

    public void sendPaymentSuccessEmail(PaymentSuccessEvent event){
        String body = "Hi,\n\nYour payment was successful.\n\n"
                + "Order ID: " + event.getOrderId() + "\n"
                + "Payment ID: " + event.getPaymentId() + "\n"
                + "Amount: " + event.getAmount() + "\n\n"
                + "Thanks you for shopping with us!";

        sendEmail(event.getUserEmail(), "Payment Successful - " + event.getOrderId(), body);
        log.info("Payment success email sent for paymentId={}", event.getPaymentId());
    }

    public void sendPaymentFailedEmail(PaymentFailedEvent event){
        String body = "Hi,\n\nUnfortunately your payment could not be processed.\n\n"
                + "Order ID: " + event.getOrderId() + "\n"
                + "Payment ID: " + event.getPaymentId() + "\n"
                + "Reason: " + event.getReason() + "\n\n"
                + "Please try again or contact support.";

        sendEmail(event.getUserEmail(), "Payment Failed - " + event.getOrderId(), body);
        log.info("Payment failed email sent for paymentId={}", event.getPaymentId());
    }

    private void sendEmail(String to, String subject, String body){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
}
