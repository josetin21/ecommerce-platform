package com.ecommerce.paymentservice.service;

import com.ecommerce.paymentservice.config.RazorpayProperties;
import com.ecommerce.paymentservice.dto.event.PaymentFailedEvent;
import com.ecommerce.paymentservice.dto.event.PaymentSuccessEvent;
import com.ecommerce.paymentservice.dto.event.RefundFailedEvent;
import com.ecommerce.paymentservice.dto.event.RefundProcessedEvent;
import com.ecommerce.paymentservice.dto.request.CreatePaymentOrderRequest;
import com.ecommerce.paymentservice.dto.request.VerifyPaymentRequest;
import com.ecommerce.paymentservice.dto.response.PaymentOrderResponse;
import com.ecommerce.paymentservice.entity.Payment;
import com.ecommerce.paymentservice.entity.PaymentStatus;
import com.ecommerce.paymentservice.exception.DuplicatePaymentException;
import com.ecommerce.paymentservice.exception.InvalidPaymentSignatureException;
import com.ecommerce.paymentservice.exception.PaymentNotFoundException;
import com.ecommerce.paymentservice.repository.PaymentRepository;
import com.razorpay.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.ecommerce.paymentservice.config.RabbitMQConfig.*;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RazorpayClient razorpayClient;
    private final RazorpayProperties razorpayProperties;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public PaymentOrderResponse createPaymentOrder(CreatePaymentOrderRequest request, UUID userId, String userEmail){

        paymentRepository.findByOrderIdAndStatus(request.getOrderId(), PaymentStatus.SUCCESS)
                .ifPresent(existing ->{
                    throw new DuplicatePaymentException("Payment already completed for this order");
                });

        Optional<Payment> pendingPayment = paymentRepository.findByOrderIdAndStatus(request.getOrderId(), PaymentStatus.CREATED);

        if (pendingPayment.isPresent()){
            Payment existing = pendingPayment.get();
            return PaymentOrderResponse.builder()
                    .paymentId(existing.getId())
                    .razorpayOrderId(existing.getRazorpayOrderId())
                    .razorpayKeyId(razorpayProperties.getKeyId())
                    .amount(existing.getAmount())
                    .currency(existing.getCurrency())
                    .status(existing.getStatus().name())
                    .build();
        }

        int amountInPaise = request.getAmount().multiply(BigDecimal.valueOf(100)).intValue();

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amountInPaise);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", request.getOrderId().toString());
        orderRequest.put("payment_capture", 1);

        Order razorpayOrder;
        try{
            razorpayOrder = razorpayClient.orders.create(orderRequest);
        }catch (Exception e){
            log.error("Razorpay order creation failed: {}", e.getMessage());
            throw new RuntimeException("Failed to create Razorpay order");
        }

        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .userId(userId)
                .userEmail(userEmail)
                .razorpayOrderId(razorpayOrder.get("id"))
                .amount(request.getAmount())
                .currency("INR")
                .status(PaymentStatus.CREATED)
                .build();

        Payment saved = paymentRepository.save(payment);

        return PaymentOrderResponse.builder()
                .paymentId(saved.getId())
                .razorpayOrderId(saved.getRazorpayOrderId())
                .razorpayKeyId(razorpayProperties.getKeyId())
                .amount(saved.getAmount())
                .currency(saved.getCurrency())
                .status(saved.getStatus().name())
                .build();
    }

    @Transactional
    public void processRefund(UUID orderId){
        Payment payment = paymentRepository.findByOrderIdAndStatus(orderId, PaymentStatus.SUCCESS)
                .orElse(null);

        if (payment == null){
            log.warn("No successful payment found for orderId={}", orderId);
            return;
        }

        try{
            JSONObject refundRequest = new JSONObject();
            refundRequest.put("amount", payment.getAmount().multiply(BigDecimal.valueOf(100)).intValue());

            Refund refund = razorpayClient.payments.refund(payment.getRazorpayPaymentId(), refundRequest);

            payment.setStatus(PaymentStatus.REFUNDED);
            payment.setRazorpayRefundId(refund.get("id"));
            paymentRepository.save(payment);

            rabbitTemplate.convertAndSend(PAYMENT_EXCHANGE, REFUND_PROCESSED_ROUTING_KEY,
                    RefundProcessedEvent.builder()
                            .orderId(payment.getOrderId())
                            .paymentId(payment.getId())
                            .razorpayRefundId(refund.get("id"))
                            .amount(payment.getAmount())
                            .userEmail(payment.getUserEmail())
                            .build());

            log.info("Refund processed for orderId={}", orderId);
        } catch (Exception e){
            log.error("Refund failed for orderId={} : {}",orderId, e.getMessage());

            payment.setStatus(PaymentStatus.REFUND_FAILED);
            paymentRepository.save(payment);

            rabbitTemplate.convertAndSend(PAYMENT_EXCHANGE, REFUND_FAILED_ROUTING_KEY,
                    RefundFailedEvent.builder()
                            .orderId(payment.getOrderId())
                            .paymentId(payment.getId())
                            .userEmail(payment.getUserEmail())
                            .amount(payment.getAmount())
                            .reason(e.getMessage())
                            .build());
        }
    }

    @Transactional
    public void verifyPayment(VerifyPaymentRequest request){

        Payment payment = paymentRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for this order"));

        JSONObject attributes = new JSONObject();
        attributes.put("razorpay_order_id", request.getRazorpayOrderId());
        attributes.put("razorpay_payment_id", request.getRazorpayPaymentId());
        attributes.put("razorpay_signature", request.getRazorpaySignature());

        boolean isValid;
        try{
            isValid = Utils.verifyPaymentSignature(attributes, razorpayProperties.getKeySecret());
        }catch (RazorpayException e){
            isValid = false;
        }

        if (!isValid){
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);

            rabbitTemplate.convertAndSend(PAYMENT_EXCHANGE, PAYMENT_FAILED_ROUTING_KEY,
                    PaymentFailedEvent.builder()
                            .orderId(payment.getOrderId())
                            .paymentId(payment.getId())
                            .userEmail(payment.getUserEmail())
                            .reason("Signature verification failed")
                            .build());

            throw new InvalidPaymentSignatureException("Payment signature verification failed");
        }

        payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
        payment.setRazorpaySignature(request.getRazorpaySignature());
        payment.setStatus(PaymentStatus.SUCCESS);
        paymentRepository.save(payment);

        rabbitTemplate.convertAndSend(PAYMENT_EXCHANGE, PAYMENT_SUCCESS_ROUTING_KEY,
                PaymentSuccessEvent.builder()
                        .orderId(payment.getOrderId())
                        .paymentId(payment.getId())
                        .userEmail(payment.getUserEmail())
                        .razorpayPaymentId(payment.getRazorpayPaymentId())
                        .amount(payment.getAmount())
                        .build());
    }

    public void handleWebHookEvent (String payload) throws org.json.JSONException{
        JSONObject json = new JSONObject(payload);
        String event = json.getString("event");

        JSONObject paymentEntity = json.getJSONObject("payload")
                .getJSONObject("payment")
                .getJSONObject("entity");

        String razorpayOrderId = paymentEntity.getString("order_id");
        String razorpayPaymentId = paymentEntity.getString("id");

        Payment payment = paymentRepository.findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not  found for webhook event"));

        if (payment.getStatus() == PaymentStatus.SUCCESS || payment.getStatus() == PaymentStatus.FAILED){
            log.info("Webhook event for already processed payment {}, skipping", payment.getId());
            return;
        }

        if ("payment.captured".equals(event)){
            payment.setRazorpayPaymentId(razorpayPaymentId);
            payment.setStatus(PaymentStatus.SUCCESS);
            paymentRepository.save(payment);

            rabbitTemplate.convertAndSend(PAYMENT_EXCHANGE, PAYMENT_SUCCESS_ROUTING_KEY,
                    PaymentSuccessEvent.builder()
                            .orderId(payment.getOrderId())
                            .paymentId(payment.getId())
                            .razorpayPaymentId(razorpayPaymentId)
                            .amount(payment.getAmount())
                            .userEmail(payment.getUserEmail())
                            .build());
        } else if ("payment.failed".equals(event)) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);

            rabbitTemplate.convertAndSend(PAYMENT_EXCHANGE, PAYMENT_FAILED_ROUTING_KEY,
                    PaymentFailedEvent.builder()
                            .orderId(payment.getOrderId())
                            .paymentId(payment.getId())
                            .userEmail(payment.getUserEmail())
                            .reason("Payment failed per Razorpay webhook")
                            .build());

        }
    }
}
