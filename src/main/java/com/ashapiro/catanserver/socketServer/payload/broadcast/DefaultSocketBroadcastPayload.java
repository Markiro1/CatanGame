package com.ashapiro.catanserver.socketServer.payload.broadcast;

import com.ashapiro.catanserver.enums.EventType;
import com.ashapiro.catanserver.socketServer.payload.SocketBroadcastPayload;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class DefaultSocketBroadcastPayload implements SocketBroadcastPayload {

    private EventType eventType;
}
