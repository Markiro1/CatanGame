package com.ashapiro.catanserver.socketServer.util;

import com.ashapiro.catanserver.enums.EventType;
import com.ashapiro.catanserver.socketServer.payload.SocketMessagePayload;
import com.ashapiro.catanserver.socketServer.payload.request.*;

import java.util.HashMap;
import java.util.Map;

public class MessageRegistry {
    private static final Map<EventType, Class<? extends SocketMessagePayload>> registry = new HashMap<>();

    static {
        registry.put(EventType.REQUEST_CONNECT, SocketRequestConnectPayload.class);
        registry.put(EventType.REQUEST_START_GAME, SocketRequestStartGamePayload.class);
        registry.put(EventType.REQUEST_BUILD_SETTLEMENT, SocketRequestBuildPayload.class);
        registry.put(EventType.REQUEST_BUILD_ROAD, SocketRequestBuildPayload.class);
        registry.put(EventType.REQUEST_BUILD_CITY, SocketRequestBuildPayload.class);
        registry.put(EventType.REQUEST_TRADE_RESOURCE, SocketRequestTradeResourcePayload.class);
        registry.put(EventType.REQUEST_USER_ROBBERY, SocketRequestRobberyPayload.class);
        registry.put(EventType.REQUEST_USE_KNIGHT_CARD, SocketRequestUseKnightCardPayload.class);
        registry.put(EventType.REQUEST_USE_MONOPOLY_CARD, SocketRequestUseMonopolyCardPayload.class);
        registry.put(EventType.REQUEST_USE_YEAR_OF_PLENTY_CARD, SocketRequestUseYearOfPlentyCardPayload.class);
        registry.put(EventType.REQUEST_READY_AND_LOAD, DefaultSocketMessagePayload.class);
        registry.put(EventType.REQUEST_USER_TURN_READY, DefaultSocketMessagePayload.class);
        registry.put(EventType.REQUEST_BUY_CARD, DefaultSocketMessagePayload.class);
        registry.put(EventType.REQUEST_USE_ROAD_BUILDING_CARD, DefaultSocketMessagePayload.class);
        registry.put(EventType.REQUEST_EXCHANGE_OFFER, SocketRequestExchangeResourcesOfferPayload.class);
        registry.put(EventType.REQUEST_EXCHANGE, SocketRequestExchangePayload.class);
    }

    public static Class<SocketMessagePayload> getMessageClass(EventType eventType) {
        return (Class<SocketMessagePayload>) registry.get(eventType);
    }
}
