package com.ecommerce.productservice.exception;

import org.springframework.http.HttpStatus;

public class EmailAlreadyExistsException extends BaseException{
    public EmailAlreadyExistsException(String email){
        super("Email already in use: " + email, HttpStatus.CONFLICT);
    }
}
