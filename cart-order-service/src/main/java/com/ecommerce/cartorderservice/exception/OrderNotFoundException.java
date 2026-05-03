package com.ecommerce.cartorderservice.exception;

import org.springframework.http.HttpStatus;

public class OrderNotFoundException extends BaseException{

    public OrderNotFoundException(String id){
        super("Order not found with id: " + id, HttpStatus.NOT_FOUND);
    }
}
