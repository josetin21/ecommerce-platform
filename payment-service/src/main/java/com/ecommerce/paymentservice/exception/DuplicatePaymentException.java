package com.ecommerce.paymentservice.exception;

import org.springframework.http.HttpStatus;

import java.util.Locale;

public class DuplicatePaymentException extends BaseException{
    public DuplicatePaymentException(String message){
        super(message, HttpStatus.CONFLICT);
    }
}
