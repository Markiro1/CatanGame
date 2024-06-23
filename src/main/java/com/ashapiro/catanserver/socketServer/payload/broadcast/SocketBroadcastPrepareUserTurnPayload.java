package com.ashapiro.catanserver.socketServer.payload.broadcast;

import com.ashapiro.catanserver.enums.EventType;
import com.ashapiro.catanserver.socketServer.payload.SocketBroadcastPayload;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SocketBroadcastPrepareUserTurnPayload implements SocketBroadcastPayload {
    private EventType eventType;

   private Long userId;

   private Integer numOfTurn;
}
