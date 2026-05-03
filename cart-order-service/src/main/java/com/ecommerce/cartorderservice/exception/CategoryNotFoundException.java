package com.ecommerce.cartorderservice.exception;

import org.springframework.http.HttpStatus;

public class CategoryNotFoundException extends BaseException{
    public CategoryNotFoundException(String id){
        super("Category not found with id: " + id, HttpStatus.NOT_FOUND);
    }
}
