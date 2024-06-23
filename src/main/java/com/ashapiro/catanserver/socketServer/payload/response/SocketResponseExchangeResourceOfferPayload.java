package com.ashapiro.catanserver.socketServer.payload.response;

import com.ashapiro.catanserver.enums.EventType;
import com.ashapiro.catanserver.game.enums.Resource;
import com.ashapiro.catanserver.socketServer.payload.SocketMessagePayload;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class SocketResponseExchangeResourceOfferPayload implements SocketMessagePayload {

    private EventType eventType;

    private Long initiatorUserId;

    private int targetAmountOfResource;

    private int initiatorAmountOfResource;

    private Resource initiatorResource;

    private Resource targetResource;

    private Long exchangeId;
}
