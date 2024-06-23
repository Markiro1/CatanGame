package com.ashapiro.catanserver.socketServer.payload.request;

import com.ashapiro.catanserver.enums.EventType;
import com.ashapiro.catanserver.game.enums.Resource;
import com.ashapiro.catanserver.socketServer.payload.SocketMessagePayload;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class SocketRequestExchangeResourcesOfferPayload implements SocketMessagePayload {

    private EventType eventType;

    private Long targetUserId;

    private int targetAmountOfResource;

    private int initiatorAmountOfResource;

    private Resource initiatorResource;

    private Resource targetResource;
}
