package com.ashapiro.catanserver.handlers;

import com.ashapiro.catanserver.enums.EventType;
import com.ashapiro.catanserver.handlers.impl.ConnectToLobbyHandler;
import com.ashapiro.catanserver.handlers.impl.DisconnectFromLobbyHandler;
import com.ashapiro.catanserver.handlers.impl.StartGameHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class EventHandlerFactory {

    private final ApplicationContext applicationContext;

    public EventHandler createHandler(EventType eventType) {
        switch (eventType) {
            case REQUEST_CONNECT_TO_LOBBY -> {
                return applicationContext.getBean(ConnectToLobbyHandler.class);
            }
            case REQUEST_DISCONNECT_FROM_LOBBY -> {
                return applicationContext.getBean(DisconnectFromLobbyHandler.class);
            }
            case REQUEST_START_GAME -> {
                return applicationContext.getBean(StartGameHandler.class);
            }
            default -> throw new IllegalArgumentException("Unsupported event type: " + eventType);
        }
    }
}
