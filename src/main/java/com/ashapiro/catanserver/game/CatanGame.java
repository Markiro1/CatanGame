package com.ashapiro.catanserver.game;

import com.ashapiro.catanserver.enums.EventType;
import com.ashapiro.catanserver.enums.GameState;
import com.ashapiro.catanserver.game.model.Edge;
import com.ashapiro.catanserver.game.model.Hex;
import com.ashapiro.catanserver.game.model.User;
import com.ashapiro.catanserver.game.model.Vertex;
import com.ashapiro.catanserver.socketServer.payload.broadcast.SocketBroadcastPayload;
import com.ashapiro.catanserver.socketServer.payload.broadcast.SocketBroadcastUserTurnPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Slf4j
public class CatanGame {

    private GameState gameState;

    private List<User> users;

    private List<Hex> hexes;

    private List<Edge> edges;

    private List<Vertex> vertices;

    private static final int TURN_DURATION = 60000;

    public CatanGame(List<User> users, List<Hex> hexes, List<Edge> edges, List<Vertex> vertices) {
        this.users = new ArrayList<>(users);
        this.hexes = hexes;
        this.edges = edges;
        this.vertices = vertices;
        gameState = GameState.WAITING;

        GameManager gameManager = new GameManager();
        new Thread(gameManager).start();
    }

    private class GameManager implements Runnable {

        @Override
        @SneakyThrows
        public void run() {
            for (int i = 0; i < 15; i++) {
                if (checkUsersReady()) {
                    gameState = GameState.STARTED;
                    break;
                }
                System.out.println("check user ready " + i);
                Thread.sleep(1000);
            }

            if (!checkUsersReady()) {
                users.stream()
                        .filter(user -> !user.getIsReady())
                        .forEach(user -> {
                            try {
                                user.getSocket().close();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                users.removeIf(user -> !user.getIsReady());
            }

            do {
                for (User user : users) {
                    if (!user.getSocket().isClosed()) {
                        sendMessageUserTurn(user, EventType.BROADCAST_USER_TURN);
                    }
                    Thread.sleep(TURN_DURATION);
                }
            } while (true);
        }

        private void sendMessageUserTurn(User user, EventType eventType) throws IOException {
            for (User u : users) {
                SocketBroadcastPayload broadcast = new SocketBroadcastUserTurnPayload(
                        eventType,
                        user.getId()
                );
                ObjectMapper objectMapper = new ObjectMapper();
                String message = objectMapper.writeValueAsString(broadcast);
                PrintWriter printWriter = new PrintWriter(u.getSocket().getOutputStream());
                printWriter.println(message);
                printWriter.flush();
            }
        }

        private boolean checkUsersReady() {
            return users.stream().allMatch(User::getIsReady);
        }
    }
}
