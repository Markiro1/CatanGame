package com.ashapiro.catanserver.socketServer.payload.broadcast;

import com.ashapiro.catanserver.enums.EventType;
import com.ashapiro.catanserver.socketServer.dto.UserDTO;
import com.ashapiro.catanserver.socketServer.payload.SocketBroadcastPayload;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class SocketBroadcastUserReadyAndLoadPayload implements SocketBroadcastPayload {
    private EventType eventType;

    private UserDTO userDto;

    private String message;
}
