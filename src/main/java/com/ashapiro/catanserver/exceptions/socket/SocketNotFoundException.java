package com.ashapiro.catanserver.exceptions.socket;

public class SocketNotFoundException extends RuntimeException {
    public SocketNotFoundException(String address) {
        super(String.format("Socket does not exist: %s", address));
    }
}
