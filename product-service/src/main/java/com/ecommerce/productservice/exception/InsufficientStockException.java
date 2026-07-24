package com.ecommerce.productservice.exception;

import org.springframework.http.HttpStatus;

public class InsufficientStockException extends BaseException{
    public InsufficientStockException(String message){
        super(message, HttpStatus.CONFLICT);
    }
}
