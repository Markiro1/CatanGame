package com.ashapiro.catanserver.socketServer.payload.broadcast;

import com.ashapiro.catanserver.enums.EventType;

public interface SocketBroadcastPayload {
    EventType getEventType();
}
