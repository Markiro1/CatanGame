package com.ashapiro.catanserver.handlers.impl;

import com.ashapiro.catanserver.game.model.GameSession;
import com.ashapiro.catanserver.handlers.EventHandler;
import com.ashapiro.catanserver.service.UserToLobbyService;
import com.ashapiro.catanserver.socketPayload.SocketMessagePayload;
import com.ashapiro.catanserver.socketPayload.game.RequestGameEventPayload;
import com.ashapiro.catanserver.util.GameSessionManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.net.Socket;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GameEventHandler implements EventHandler {
    
    private final GameSessionManager gameSessionManager;
    
    private final ObjectMapper objectMapper;

    private final Map<Socket, Optional<String>> socketMap;

    private final UserToLobbyService userToLobbyService;

    @Override
    public <T extends SocketMessagePayload> void handle(T message, Socket clientSocket) {
        if (message instanceof RequestGameEventPayload) {
            String token = socketMap.get(clientSocket)
                    .orElseThrow(() -> new NoSuchElementException("Token not found"));
            Long lobbyId = userToLobbyService.findLobbyIdByToken(token);
            RequestGameEventPayload gameEventPayload = (RequestGameEventPayload) message;
            GameSession gameSession = gameSessionManager.getGameSession(lobbyId);
            if (gameSession != null && gameSession.allPlayersIsReady()) {
                processGameEvent(gameSession, gameEventPayload);
            }
        }
    }

    private void processGameEvent(GameSession gameSession, RequestGameEventPayload gameEventPayload) {
        System.out.println("TEST PROCESS GAME EVENT");
    }

}
