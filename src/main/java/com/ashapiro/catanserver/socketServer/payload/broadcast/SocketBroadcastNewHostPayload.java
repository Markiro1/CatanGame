package com.ashapiro.catanserver.socketServer.payload.broadcast;

import com.ashapiro.catanserver.dto.user.SimpleUserDTO;
import com.ashapiro.catanserver.enums.EventType;
import com.ashapiro.catanserver.socketServer.payload.SocketBroadcastPayload;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class SocketBroadcastNewHostPayload implements SocketBroadcastPayload {
    private EventType eventType;

    private String message;

    private SimpleUserDTO hostUser;
}
