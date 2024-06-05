package com.ashapiro.catanserver.exceptions.socket;

public class StartGameException extends RuntimeException {
    public StartGameException(String username) {
        super(String.format("User %s cannot start game, because he is not host"));
    }
}
