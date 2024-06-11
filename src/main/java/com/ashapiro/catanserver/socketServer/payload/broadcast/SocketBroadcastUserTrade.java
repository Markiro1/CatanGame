package com.ashapiro.catanserver.socketServer.payload.broadcast;

import com.ashapiro.catanserver.enums.EventType;
import com.ashapiro.catanserver.game.enums.Resource;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SocketBroadcastUserTrade implements SocketBroadcastPayload {

    private EventType eventType;

    private Long userId;

    private Resource userIncomingResources;

    private Resource userOutgoingResources;

    private int requestedCountOfOutgoingResource;
}
