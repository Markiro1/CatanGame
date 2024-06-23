package com.ashapiro.catanserver.socketServer.payload;

import com.ashapiro.catanserver.enums.EventType;

public interface SocketBroadcastPayload {
    EventType getEventType();
}
