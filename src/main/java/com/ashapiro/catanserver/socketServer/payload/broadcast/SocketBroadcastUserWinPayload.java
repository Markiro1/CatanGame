package com.ashapiro.catanserver.socketServer.payload.broadcast;

import com.ashapiro.catanserver.enums.EventType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SocketBroadcastUserWinPayload implements SocketBroadcastPayload{
    private EventType eventType;

    private Long userId;
}
