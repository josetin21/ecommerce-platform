package com.ecommerce.cartorderservice.exception;

import org.springframework.http.HttpStatus;

public class InsufficientStockException extends BaseException{

    public InsufficientStockException(String productName){
        super("Insufficient stock for product: " + productName, HttpStatus.BAD_REQUEST );
    }
}
