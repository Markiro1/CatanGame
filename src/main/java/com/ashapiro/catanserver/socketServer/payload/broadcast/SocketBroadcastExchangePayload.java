package com.ashapiro.catanserver.socketServer.payload.broadcast;

import com.ashapiro.catanserver.enums.EventType;
import com.ashapiro.catanserver.game.enums.Resource;
import com.ashapiro.catanserver.socketServer.payload.SocketBroadcastPayload;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SocketBroadcastExchangePayload implements SocketBroadcastPayload {

    private EventType eventType;

    private Long initiatorUserId;

    private Long targetUserId;

    private int targetAmountOfResource;

    private int initiatorAmountOfResource;

    private Resource initiatorResource;

    private Resource targetResource;
}
