package com.ashapiro.catanserver.util;

import com.ashapiro.catanserver.game.model.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GameSessionManager {

    private final Map<Long, GameSession> gameSessions = new ConcurrentHashMap<>();

    public void createGameSession(Long lobbyId, List<Hex>hexes, List<Edge> edges, List<Vertex> vertices, List<Player> players) {
        GameSession gameSession = new GameSession(hexes, edges, vertices, players);
        gameSessions.put(lobbyId, gameSession);
    }

    public GameSession getGameSession(Long lobbyId) {
        return gameSessions.get(lobbyId);
    }

    public void removeGameSession(Long lobbyId) {
        gameSessions.remove(lobbyId);
    }
}
