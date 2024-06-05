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
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Slf4j
public class CatanGame extends Thread{

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

        start();
    }

    @Override
    public void run() {
        try {
            waitForUsersReady();
            if (gameState == GameState.STARTED) {
                gameLoop();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Game manager interrupted");
        }
    }

    private void gameLoop() {
        do {
            for (User user : users) {
                if (!user.getSocket().isClosed()) {
                    sendMessageUserTurn(user, EventType.BROADCAST_USER_TURN);
                }
            }
        } while (true);
    }

    private void waitForUsersReady() throws InterruptedException {
        for (int i = 0; i < 15; i++) {
            if (checkUsersReady()) {
                gameState = GameState.STARTED;
                return;
            } else {
                cleanUpUnreadyUsers();
            }
            log.info("Checking if users are ready (attempt {})", i + 1);
            Thread.sleep(1000);
        }
    }

    private boolean checkUsersReady() {
        return users.stream().allMatch(User::getIsReady);
    }

    private void cleanUpUnreadyUsers() {
        users.removeIf(user -> {
            if (!user.getIsReady()) {
                try {
                    user.getSocket().close();
                } catch (IOException e) {
                    log.error("Error closing socket for unready user: {}", user.getId(), e);
                }
                return true;
            }
            return false;
        });
    }

    private void sendMessageUserTurn(User user, EventType eventType) {
        for (User u : users) {
            SocketBroadcastPayload broadcast = new SocketBroadcastUserTurnPayload(eventType, user.getId());
            try {
                String message = new ObjectMapper().writeValueAsString(broadcast);
                PrintWriter printWriter = new PrintWriter(u.getSocket().getOutputStream());
                printWriter.println(message);
                printWriter.flush();
            } catch (IOException e) {
                log.error("Error sending message to user: {}", u.getId(), e);
            }
        }
    }
}
