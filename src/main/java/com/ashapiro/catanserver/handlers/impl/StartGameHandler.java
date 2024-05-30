package com.ashapiro.catanserver.handlers.impl;

import com.ashapiro.catanserver.entity.Lobby;
import com.ashapiro.catanserver.entity.User;
import com.ashapiro.catanserver.enums.EventType;
import com.ashapiro.catanserver.game.dto.HexDto;
import com.ashapiro.catanserver.game.map.GameMap;
import com.ashapiro.catanserver.game.model.Player;
import com.ashapiro.catanserver.handlers.EventHandler;
import com.ashapiro.catanserver.service.UserService;
import com.ashapiro.catanserver.socketPayload.SocketMessagePayload;
import com.ashapiro.catanserver.socketPayload.game.SocketBroadcastStartGamePayload;
import com.ashapiro.catanserver.socketPayload.game.SocketRequestStartGamePayload;
import com.ashapiro.catanserver.util.GameSessionManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StartGameHandler implements EventHandler {

    private final Map<Socket, Optional<String>> socketMap;

    private final UserService userService;

    private final ObjectMapper objectMapper;

    private final GameSessionManager gameSessionManager;

    @Override
    public <T extends SocketMessagePayload> void handle(T message, Socket clientSocket) {
        if (message instanceof SocketRequestStartGamePayload) {
            SocketRequestStartGamePayload startGamePayload = (SocketRequestStartGamePayload) message;
            String token = socketMap.get(clientSocket)
                    .orElseThrow(() -> new NoSuchElementException("Token not found"));
            if (!token.isEmpty()) {
                User user = userService.findUserByToken(token)
                        .orElseThrow(() -> new NoSuchElementException("User not found with token: " + token));
                Lobby lobby = user.getUserToLobby().getLobby();
                List<String> allTokensInLobby = lobby.getAllTokenUsersInLobby();
                List<Player> players = getPlayersInLobby(allTokensInLobby);

                new Thread(() -> {
                    GameMap gameMap = new GameMap();
                    gameMap.generateMap(startGamePayload.getMap());
                    sendMapToAllUsers(allTokensInLobby, startGamePayload.getMap(), gameMap.getHexDtos());
                    gameSessionManager.createGameSession(lobby.getId(), gameMap.getHexes(), gameMap.getEdges(), gameMap.getVertices(), players);
                }).start();
            }
        }
    }

    private List<Player> getPlayersInLobby(List<String> allTokensInLobby) {
        List<Player> players = new ArrayList<>();
        socketMap.forEach((socket, token) -> {
            token.ifPresent(t -> {
                if (allTokensInLobby.contains(t)) {
                    User user = userService.findUserByToken(t).orElse(null);
                    if (user != null) {
                        players.add(initializePlayer(user, socket));
                    }
                }
            });
        });
        return players;
    }

    private Player initializePlayer(User user, Socket clientSocket) {
        Player player = Player.builder()
                .id(user.getId())
                .isReady(false)
                .socket(clientSocket)
                .build();
        return player;
    }

    private void sendMapToAllUsers(List<String> allTokensInLobby, List<Integer> map, List<HexDto> hexDtos) {
        socketMap.forEach((socket, t) -> {
            if (allTokensInLobby.contains(t.get())) {
                try {
                    SocketBroadcastStartGamePayload startGamePayload = SocketBroadcastStartGamePayload.builder()
                            .eventType(EventType.BROADCAST_START_GAME)
                            .map(map)
                            .hexes(hexDtos)
                            .build();

                    String response = objectMapper.writeValueAsString(startGamePayload);
                    PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
                    printWriter.println(response);
                    printWriter.flush();
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
