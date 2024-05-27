package com.ashapiro.catanserver.enums;

public enum EventType {
    RESPONSE_CONNECT_TO_LOBBY,
    REQUEST_CONNECT_TO_LOBBY,
    REQUEST_DISCONNECT_FROM_LOBBY,
    BROADCAST_USER_CONNECTION_TO_LOBBY,
    BROADCAST_USER_DISCONNECT_FROM_LOBBY,
    REQUEST_START_GAME, BROADCAST_START_GAME, BROADCAST_NEW_HOST_IN_LOBBY
}
