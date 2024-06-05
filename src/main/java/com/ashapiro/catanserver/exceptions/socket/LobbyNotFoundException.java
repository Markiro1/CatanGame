package com.ashapiro.catanserver.exceptions.socket;

public class LobbyNotFoundException extends RuntimeException{
    public LobbyNotFoundException(String address) {
        super(String.format("Lobby not found with socket: %s", address));
    }
}
