package com.ashapiro.catanserver.socketServer.payload.broadcast;

import com.ashapiro.catanserver.dto.user.SimpleUserDto;
import com.ashapiro.catanserver.enums.EventType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class SocketBroadcastDisconnectFromLobbyPayload implements SocketBroadcastPayload {

    private EventType eventType;

    private String message;

    private SimpleUserDto disconnectedUser;
}
