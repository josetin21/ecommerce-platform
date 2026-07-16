package com.ecommerce.cartorderservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String ORDER_PLACED_QUEUE = "order.placed.queue";
    public static final String ORDER_PLACED_ROUTING_KEY = "order.placed";

    public static final String PAYMENT_EXCHANGE = "payment.exchange";
    public static final String PAYMENT_SUCCESS_QUEUE = "cart.payment.success.queue";
    public static final String PAYMENT_FAILED_QUEUE = "cart.payment.failed.queue";
    public static final String PAYMENT_SUCCESS_ROUTING_KEY = "payment.success";
    public static final String PAYMENT_FAILED_ROUTING_KEY = "payment.failed";

    public static final String REFUND_PROCESSED_QUEUE = "cart.refund.processed.queue";
    public static final String REFUND_FAILED_QUEUE = "cart.refund.failed.queue";
    public static final String REFUND_PROCESSED_ROUTING_KEY = "refund.processed";
    public static final String REFUND_FAILED_ROUTING_KEY = "refund.failed";

    public static final String ORDER_CANCELLED_ROUTING_KEY = "order.cancelled";

    @Bean
    public TopicExchange orderExchange(){
        return new TopicExchange(ORDER_EXCHANGE);
    }

    @Bean
    public Queue orderPlacedQueue(){
        return QueueBuilder.durable(ORDER_PLACED_QUEUE).build();
    }

    @Bean
    public Binding orderPlacedBinding(){
        return BindingBuilder
                .bind(orderPlacedQueue())
                .to(orderExchange())
                .with(ORDER_PLACED_ROUTING_KEY);
    }

    @Bean
    public TopicExchange paymentExchange(){
        return new TopicExchange(PAYMENT_EXCHANGE);
    }

    @Bean
    public Queue paymentSuccessQueue(){
        return QueueBuilder.durable(PAYMENT_SUCCESS_QUEUE).build();
    }

    @Bean
    public Queue paymentFailedQueue(){
        return QueueBuilder.durable(PAYMENT_FAILED_QUEUE).build();
    }

    @Bean
    public Binding paymentSuccessBinding(){
        return BindingBuilder
                .bind(paymentSuccessQueue())
                .to(paymentExchange())
                .with(PAYMENT_SUCCESS_ROUTING_KEY);
    }

    @Bean
    public Binding paymentFailedBinding(){
        return BindingBuilder
                .bind(paymentFailedQueue())
                .to(paymentExchange())
                .with(PAYMENT_FAILED_ROUTING_KEY);
    }

    @Bean
    public Queue refundProcessedQueue(){
        return QueueBuilder.durable(REFUND_PROCESSED_QUEUE).build();
    }

    @Bean
    public Queue refundFailedQueue(){
        return QueueBuilder.durable(REFUND_FAILED_QUEUE).build();
    }

    @Bean
    public Binding refundProcessedBinding(){
        return BindingBuilder
                .bind(refundProcessedQueue())
                .to(paymentExchange())
                .with(REFUND_PROCESSED_ROUTING_KEY);
    }

    @Bean
    public Binding refundFailedBinding(){
        return BindingBuilder
                .bind(refundFailedQueue())
                .to(paymentExchange())
                .with(REFUND_FAILED_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory){
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
