package com.ecommerce.cartorderservice.exception;

import org.springframework.http.HttpStatus;

public class ProductNotFoundException extends BaseException{
    public ProductNotFoundException(String id){
        super("Product not found with id: " + id, HttpStatus.NOT_FOUND);
    }
}
