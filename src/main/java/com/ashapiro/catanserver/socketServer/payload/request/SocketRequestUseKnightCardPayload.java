package com.ashapiro.catanserver.socketServer.payload.request;

import com.ashapiro.catanserver.enums.EventType;
import com.ashapiro.catanserver.socketServer.payload.SocketMessagePayload;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class SocketRequestUseKnightCardPayload implements SocketMessagePayload {

    private EventType eventType;

    private int hexId;
}
