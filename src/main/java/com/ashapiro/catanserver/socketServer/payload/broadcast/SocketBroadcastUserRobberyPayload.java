package com.ashapiro.catanserver.socketServer.payload.broadcast;

import com.ashapiro.catanserver.enums.EventType;
import com.ashapiro.catanserver.game.enums.Resource;
import com.ashapiro.catanserver.socketServer.payload.SocketBroadcastPayload;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SocketBroadcastUserRobberyPayload implements SocketBroadcastPayload {

    private EventType eventType;

    private Long robberUserId;

    private Long victimUserId;

    private int hexId;

    private Resource resource;
}
