package com.ashapiro.catanserver.exceptions.socket;

public class UserNotFoundException extends RuntimeException{
    public UserNotFoundException(String address) {
        super(String.format("User not found with socket: %s", address));
    }
}
