package com.ashapiro.catanserver.socketServer.payload.broadcast;

import com.ashapiro.catanserver.dto.user.SimpleUserDTO;
import com.ashapiro.catanserver.enums.EventType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@SuperBuilder
public class SocketBroadcastConnectToLobbyPayload implements SocketBroadcastPayload {

    private EventType eventType;

    private String message;

    private SimpleUserDTO connectedUser;
}
