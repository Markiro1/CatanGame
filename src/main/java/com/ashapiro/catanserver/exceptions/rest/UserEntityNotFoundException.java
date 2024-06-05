package com.ashapiro.catanserver.exceptions.rest;

public class UserEntityNotFoundException extends RuntimeException{
    public UserEntityNotFoundException(Long id) {
        super(String.format("UserEntity not found with id: %d", id));
    }

    public UserEntityNotFoundException(String token) {
        super(String.format("UserEntity not found with token: %s", token));
    }
}
