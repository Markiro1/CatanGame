package com.ashapiro.catanserver.socketServer.payload.broadcast;

import com.ashapiro.catanserver.enums.EventType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SocketBroadcastBuildPayload implements SocketBroadcastPayload{
    private EventType eventType;
    private String message;
    private Long userId;
    private Integer fieldId;
}
