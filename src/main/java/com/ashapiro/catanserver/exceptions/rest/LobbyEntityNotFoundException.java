package com.ashapiro.catanserver.exceptions.rest;

import com.ashapiro.catanserver.entity.UserEntity;

public class LobbyEntityNotFoundException extends RuntimeException{
    public LobbyEntityNotFoundException(Long id) {
        super(String.format("Lobby not found with id: %d", id));
    }

    public LobbyEntityNotFoundException(UserEntity user) {
        super(String.format("Lobby not found with user: %s", user));
    }
}
