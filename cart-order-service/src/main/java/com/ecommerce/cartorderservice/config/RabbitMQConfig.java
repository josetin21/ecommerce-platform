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
