package com.ashapiro.catanserver.socketServer.payload.broadcast;

import com.ashapiro.catanserver.enums.EventType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class SocketBroadcastDiceThrowPayload implements SocketBroadcastPayload{

    private EventType eventType;

    private Long userId;

    private int firstDiceNum;

    private int secondDiceNum;
}
