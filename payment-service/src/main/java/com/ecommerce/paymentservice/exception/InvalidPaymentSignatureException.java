package com.ecommerce.paymentservice.exception;

import org.springframework.http.HttpStatus;

public class InvalidPaymentSignatureException extends BaseException{
    public InvalidPaymentSignatureException(String message){
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
