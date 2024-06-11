package com.ashapiro.catanserver.socketServer.payload.broadcast;

import com.ashapiro.catanserver.enums.EventType;
import com.ashapiro.catanserver.game.enums.Resource;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class SocketBroadcastUserGetResourcesPayload implements SocketBroadcastPayload {

    private EventType eventType;

    private Long userId;

    private List<Resource> resources;
}
