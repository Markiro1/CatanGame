package com.ashapiro.catanserver.socketServer.payload;

import com.ashapiro.catanserver.enums.EventType;

public interface SocketMessagePayload {
    EventType getEventType();
}
