package com.ashapiro.catanserver.socketServer.payload.request;

import com.ashapiro.catanserver.enums.EventType;
import com.ashapiro.catanserver.socketServer.payload.SocketMessagePayload;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class SocketRequestExchangePayload implements SocketMessagePayload {

    private EventType eventType;

    private Long exchangeId;

    private boolean accept;
}
