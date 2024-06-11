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
        registry.put(EventType.REQUEST_READY_AND_LOAD, SocketRequestReadyAndLoadPayload.class);
        registry.put(EventType.REQUEST_BUILD_SETTLEMENT, SocketRequestBuildPayload.class);
        registry.put(EventType.REQUEST_BUILD_ROAD, SocketRequestBuildPayload.class);
        registry.put(EventType.REQUEST_BUILD_CITY, SocketRequestBuildPayload.class);
        registry.put(EventType.REQUEST_USER_TURN_READY, SocketRequestUserTurnReadyPayload.class);
        registry.put(EventType.REQUEST_TRADE_RESOURCE, SocketRequestTradeResourcePayload.class);
    }

    public static <T extends SocketMessagePayload> Class<T> getMessageClass(EventType eventType) {
        return (Class<T>) registry.get(eventType);
    }
}
