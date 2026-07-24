package com.ecommerce.cartorderservice.exception;

import org.springframework.http.HttpStatus;

public class InvalidOrderStateException extends BaseException{
    public InvalidOrderStateException(String message){
        super(message, HttpStatus.CONFLICT);
    }
}
