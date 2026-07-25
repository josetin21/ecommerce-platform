package com.ecommerce.notificationservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;


@Configuration
public class RabbitMQConfig {

    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String ORDER_PLACED_QUEUE = "order.placed.queue";
    public static final String ORDER_PLACED_ROUTING_KEY = "order.placed";

    public static final String PAYMENT_EXCHANGE = "payment.exchange";
    public static final String PAYMENT_SUCCESS_QUEUE = "notification.payment.success.queue";
    public static final String PAYMENT_SUCCESS_ROUTING_KEY = "payment.success";
    public static final String PAYMENT_FAILED_QUEUE = "notification.payment.failed.queue";
    public static final String PAYMENT_FAILED_ROUTING_KEY = "payment.failed";

    public static final String REFUND_PROCESSED_QUEUE = "notification.refund.processed.queue";
    public static final String REFUND_PROCESSED_ROUTING_KEY = "refund.processed";
    public static final String REFUND_FAILED_QUEUE = "notification.refund.failed.queue";
    public static final String REFUND_FAILED_ROUTING_KEY = "refund.failed";

    public static final String DLX_EXCHANGE = "notification.dlx";
    public static final String DLQ_QUEUE = "notification.dlq";
    public static final String DLQ_ROUTING_KEY = "notification.dlq";

    @Bean
    public TopicExchange orderExchange(){
        return new TopicExchange(ORDER_EXCHANGE);
    }

    @Bean
    public Queue orderPlacedQueue(){
        return new Queue(ORDER_PLACED_QUEUE);
    }

    @Bean
    public Binding orderplacedBinding(){
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
    public Binding refundFailedBinding(){
        return BindingBuilder
                .bind(refundFailedQueue())
                .to(paymentExchange())
                .with(REFUND_FAILED_ROUTING_KEY);
    }

    @Bean
    public Binding refundProcessedBinding(){
        return BindingBuilder
                .bind(refundProcessedQueue())
                .to(paymentExchange())
                .with(REFUND_PROCESSED_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public TopicExchange deadLetterExchange(){
        return new TopicExchange(DLX_EXCHANGE);
    }

    @Bean
    public Queue deadLetterQueue(){
        return QueueBuilder.durable(DLQ_QUEUE).build();
    }

    @Bean
    public Binding deadLetterBinding(){
        return BindingBuilder
                .bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(DLQ_ROUTING_KEY);
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter messageConverter,
            RabbitTemplate rabbitTemplate){

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);

        RetryOperationsInterceptor retryOperationsInterceptor = RetryInterceptorBuilder.stateless()
                .maxAttempts(3)
                .backOffOptions(1000, 2.0, 10000)
                .recoverer(new RepublishMessageRecoverer(rabbitTemplate, DLX_EXCHANGE, DLQ_ROUTING_KEY))
                .build();

        factory.setAdviceChain(retryOperationsInterceptor);
        return factory;
    }
}
