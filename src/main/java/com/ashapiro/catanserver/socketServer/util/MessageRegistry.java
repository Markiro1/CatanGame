package com.ashapiro.catanserver.socketServer.util;

import com.ashapiro.catanserver.enums.EventType;
import com.ashapiro.catanserver.socketServer.payload.SocketMessagePayload;
import com.ashapiro.catanserver.socketServer.payload.request.SocketRequestBuildHousePayload;
import com.ashapiro.catanserver.socketServer.payload.request.SocketRequestReadyAndLoadPayload;
import com.ashapiro.catanserver.socketServer.payload.request.SocketRequestStartGamePayload;
import com.ashapiro.catanserver.socketServer.payload.request.SocketRequestConnectPayload;

import java.util.HashMap;
import java.util.Map;

public class MessageRegistry {
    private static final Map<EventType, Class<? extends SocketMessagePayload>> registry = new HashMap<>();

    static {
        registry.put(EventType.REQUEST_CONNECT, SocketRequestConnectPayload.class);
        registry.put(EventType.REQUEST_START_GAME, SocketRequestStartGamePayload.class);
        registry.put(EventType.REQUEST_READY_AND_LOAD, SocketRequestReadyAndLoadPayload.class);
        registry.put(EventType.REQUEST_BUILD_HOUSE, SocketRequestBuildHousePayload.class);
    }

    public static <T extends SocketMessagePayload> Class<T> getMessageClass(EventType eventType) {
        return (Class<T>) registry.get(eventType);
    }
}
